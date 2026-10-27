package com.eifi.feature.pay.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.AppPermission
import com.eifi.core.data.PayingAccountChoice
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.PaymentUrlCodec
import com.eifi.core.payload.PaymentUrlDecodingResult
import com.eifi.core.permission.PermissionChecker
import com.eifi.core.ussd.SimCard
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Summary of the payment URL read by NFC, the scanner or an App Link, decoded once when the summary opens: the
 * payer's account among their SIMs (the phone calls permission is asked once, at the first summary), a countdown
 * refreshed every second, and the confirmation through the biometric prompt, refused once the code has expired or
 * without compatible account.
 */
@HiltViewModel
internal class PaymentSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    paymentUrlCodec: PaymentUrlCodec,
    private val payingAccountService: PayingAccountService,
    private val permissionChecker: PermissionChecker,
    private val permissionRequestHistory: PermissionRequestHistory,
    private val clock: Clock,
) : ViewModel() {

    /**
     * What the payer did on the summary.
     *
     * @property chosenAccount the account chosen on "Choisissez votre compte", `null` until then.
     * @property activePermission the permission being explained, `null` when none is.
     * @property choosingAccount whether "Choisissez votre compte" shows.
     * @property rememberChoice whether the account is offered first on the next payments.
     * @property confirmationStep where the confirmation stands.
     * @property permissionChecked whether the phone calls permission was checked, so that the summary does not show
     * before its permission step.
     */
    private data class Interaction(
        val chosenAccount: PayingAccount? = null,
        val activePermission: AppPermission? = null,
        val choosingAccount: Boolean = false,
        val rememberChoice: Boolean = true,
        val confirmationStep: ConfirmationStep = ConfirmationStep.None,
        val permissionChecked: Boolean = false,
    )

    private val decoded = paymentUrlCodec.decode(checkNotNull(savedStateHandle[PAYMENT_URL]) { "No payment URL" })

    private val interaction = MutableStateFlow(Interaction())

    /** The payer's SIMs, `null` while unknown. */
    private val sims = MutableStateFlow(payingAccountService.simCards())

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MS)
        }
    }

    /** `null` while the phone calls permission is checked and the remembered account loads. */
    val uiState: StateFlow<PaymentSummaryUiState?> = when (decoded) {
        is PaymentUrlDecodingResult.Valid -> combine(
            interaction,
            payingAccountService.choice,
            sims,
            ticks,
        ) { interaction, choice, sims, now ->
            if (interaction.permissionChecked) {
                ready(request = decoded.request, interaction = interaction, choice = choice, sims = sims, now = now)
            } else {
                null
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = null,
        )
        is PaymentUrlDecodingResult.Expired -> MutableStateFlow(PaymentSummaryUiState.Expired)
        is PaymentUrlDecodingResult.UnknownVersion -> MutableStateFlow(PaymentSummaryUiState.UpdateNeeded)
        is PaymentUrlDecodingResult.Malformed -> MutableStateFlow(PaymentSummaryUiState.Invalid)
    }

    init {
        if (decoded is PaymentUrlDecodingResult.Valid) {
            viewModelScope.launch {
                val permission = AppPermission.PhoneCalls
                val ask = !permissionChecker.isGranted(permission) && !permissionRequestHistory.wasRequested(permission)
                interaction.update { it.copy(activePermission = permission.takeIf { ask }, permissionChecked = true) }
            }
        }
    }

    /** The phone calls permission was explained and asked: lists the SIMs if granted. */
    fun onPermissionDone(granted: Boolean) {
        viewModelScope.launch {
            permissionRequestHistory.markRequested(AppPermission.PhoneCalls)
            if (granted) sims.value = payingAccountService.simCards()
            interaction.update { it.copy(activePermission = null) }
        }
    }

    /** Opens the "Choisissez votre compte" screen. */
    fun openAccountChoice() {
        interaction.update { it.copy(choosingAccount = true) }
    }

    /** Back to the summary without changing the account. */
    fun closeAccountChoice() {
        interaction.update { it.copy(choosingAccount = false) }
    }

    /** Pays with [account]; with [remember], it is also offered first on the next payments. */
    fun chooseAccount(account: PayingAccount, remember: Boolean) {
        interaction.update { it.copy(chosenAccount = account, rememberChoice = remember, choosingAccount = false) }
        if (remember) viewModelScope.launch { payingAccountService.remember(account) }
    }

    /** Asks for the biometric prompt while the code is valid and an account is compatible. */
    fun confirm() {
        val state = uiState.value as? PaymentSummaryUiState.Ready ?: return
        if (state.account == null) return
        interaction.update { it.copy(confirmationStep = ConfirmationStep.Authenticating) }
    }

    /** The payer is authenticated: remembers the account unless they opted out. */
    fun onAuthenticated() {
        val state = (uiState.value as? PaymentSummaryUiState.Ready)
            ?.takeIf { it.confirmationStep == ConfirmationStep.Authenticating } ?: return
        val account = state.account ?: return
        interaction.update { it.copy(confirmationStep = ConfirmationStep.Authenticated) }
        // TODO(EIFI-204): the USSD code on the SIM of the account.
        if (state.rememberChoice) viewModelScope.launch { payingAccountService.remember(account) }
    }

    /** The prompt was cancelled or failed: back to the summary, nothing launched. */
    fun onAuthenticationCancelled() {
        interaction.update {
            if (it.confirmationStep ==
                ConfirmationStep.Authenticating
            ) {
                it.copy(confirmationStep = ConfirmationStep.None)
            } else {
                it
            }
        }
    }

    /** The phone has no screen lock: explains how to set one up. */
    fun onScreenLockMissing() {
        interaction.update { it.copy(confirmationStep = ConfirmationStep.ScreenLockNeeded) }
    }

    /** Back to the summary from the screen lock explanation. */
    fun closeScreenLockNeeded() {
        interaction.update { it.copy(confirmationStep = ConfirmationStep.None) }
    }

    /** [PaymentSummaryUiState.Ready] while the code is valid, [PaymentSummaryUiState.Expired] after. */
    private fun ready(
        request: PaymentRequest,
        interaction: Interaction,
        choice: PayingAccountChoice?,
        sims: List<SimCard>?,
        now: Instant,
    ): PaymentSummaryUiState {
        if (!now.isBefore(request.expiresAt)) return PaymentSummaryUiState.Expired
        val accounts = payingAccountService.accounts(payeeOperators = request.receivingMethods.keys, sims = sims)
        val account = interaction.chosenAccount?.takeIf { it in accounts }
            ?: payingAccountService.defaultAccount(accounts = accounts, choice = choice)
        return PaymentSummaryUiState.Ready(
            displayName = request.displayName,
            amount = request.amount,
            payeeOperators = request.receivingMethods.keys.toList(),
            accounts = accounts,
            account = account,
            receivingMethod = request.receivingMethods.getValue(
                account?.operator ?: request.receivingMethods.keys.first(),
            ),
            remainingSeconds = Duration.between(now, request.expiresAt).seconds,
            activePermission = interaction.activePermission,
            choosingAccount = interaction.choosingAccount,
            rememberChoice = interaction.rememberChoice,
            confirmationStep = interaction.confirmationStep,
        )
    }

    companion object {
        /** Name of the route argument, see `PaymentSummaryRoute`. */
        const val PAYMENT_URL = "url"

        private const val TICK_MS = 1_000L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
