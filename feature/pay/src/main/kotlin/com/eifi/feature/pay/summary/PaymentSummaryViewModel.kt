package com.eifi.feature.pay.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.AppPermission
import com.eifi.core.data.PayingAccountChoice
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.PaymentUrlCodec
import com.eifi.core.payload.PaymentUrlDecodingResult
import com.eifi.core.ussd.OperatorUssdConfig
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
 * refreshed every second, the confirmation through the biometric prompt, refused once the code has expired, without
 * compatible account or without known USSD code, then the launch of the USSD code, once per session.
 */
@HiltViewModel
internal class PaymentSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    paymentUrlCodec: PaymentUrlCodec,
    private val payingAccountService: PayingAccountService,
    private val paymentLaunchService: PaymentLaunchService,
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
     * @property launchOutcome what launching the USSD code did, `null` until it is launched.
     * @property permissionChecked whether the phone calls permission was checked, so that the summary does not show
     * before its permission step.
     */
    private data class Interaction(
        val chosenAccount: PayingAccount? = null,
        val activePermission: AppPermission? = null,
        val choosingAccount: Boolean = false,
        val rememberChoice: Boolean = true,
        val confirmationStep: ConfirmationStep = ConfirmationStep.None,
        val launchOutcome: LaunchOutcome? = null,
        val permissionChecked: Boolean = false,
    )

    /**
     * What the payer's accounts and their USSD codes come from.
     *
     * @property choice the remembered account, `null` until one is.
     * @property sims the payer's SIMs, `null` while unknown.
     * @property ussdConfigs the USSD codes of the supported operators.
     */
    private data class Payer(
        val choice: PayingAccountChoice?,
        val sims: List<SimCard>?,
        val ussdConfigs: List<OperatorUssdConfig>,
    )

    private val decoded = paymentUrlCodec.decode(checkNotNull(savedStateHandle[PAYMENT_URL]) { "No payment URL" })

    private val interaction = MutableStateFlow(Interaction())

    /** The payer's SIMs, `null` while unknown. */
    private val sims = MutableStateFlow(payingAccountService.simCards())

    /** What the accounts and their USSD codes come from. */
    private val payer = combine(payingAccountService.choice, sims, paymentLaunchService.ussdConfigs, ::Payer)

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MS)
        }
    }

    /** `null` while the phone calls permission is checked and the remembered account loads. */
    val uiState: StateFlow<PaymentSummaryUiState?> = when (decoded) {
        is PaymentUrlDecodingResult.Valid -> combine(interaction, payer, ticks) { interaction, payer, now ->
            if (interaction.permissionChecked) {
                ready(request = decoded.request, interaction = interaction, payer = payer, now = now)
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
                val ask = payingAccountService.shouldAskPhoneCalls()
                interaction.update {
                    it.copy(activePermission = AppPermission.PhoneCalls.takeIf { ask }, permissionChecked = true)
                }
            }
        }
    }

    /** The phone calls permission was explained and asked: lists the SIMs if granted. */
    fun onPermissionDone(granted: Boolean) {
        viewModelScope.launch {
            payingAccountService.onPhoneCallsAsked()
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

    /** Asks for the biometric prompt while the code is valid, an account is compatible and its USSD code known. */
    fun confirm() {
        val state = uiState.value as? PaymentSummaryUiState.Ready ?: return
        if (state.account == null || !state.ussdAvailable || state.launchOutcome != null) return
        interaction.update { it.copy(confirmationStep = ConfirmationStep.Authenticating) }
    }

    /** The payer is authenticated: launches the USSD code, and remembers the account unless they opted out. */
    fun onAuthenticated() {
        val state = (uiState.value as? PaymentSummaryUiState.Ready)
            ?.takeIf { it.confirmationStep == ConfirmationStep.Authenticating } ?: return
        val account = state.account ?: return
        val request = (decoded as PaymentUrlDecodingResult.Valid).request
        interaction.update { it.copy(confirmationStep = ConfirmationStep.Authenticated) }
        viewModelScope.launch {
            if (state.rememberChoice) payingAccountService.remember(account)
            val outcome = paymentLaunchService.launch(request = request, account = account)
            interaction.update { it.copy(launchOutcome = outcome) }
        }
    }

    /** The prompt was cancelled or failed: back to the summary, nothing launched. */
    fun onAuthenticationCancelled() {
        interaction.update {
            val cancelled = it.confirmationStep == ConfirmationStep.Authenticating
            if (cancelled) it.copy(confirmationStep = ConfirmationStep.None) else it
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
        payer: Payer,
        now: Instant,
    ): PaymentSummaryUiState {
        if (!now.isBefore(request.expiresAt)) return PaymentSummaryUiState.Expired
        val accounts = payingAccountService.accounts(payeeOperators = request.receivingMethods.keys, sims = payer.sims)
        val account = interaction.chosenAccount?.takeIf { it in accounts }
            ?: payingAccountService.defaultAccount(accounts = accounts, choice = payer.choice)
        return PaymentSummaryUiState.Ready(
            paymentId = request.id,
            displayName = request.displayName,
            amount = request.amount,
            payeeOperators = request.receivingMethods.keys.toList(),
            accounts = accounts,
            account = account,
            receivingMethod = request.receivingMethods.getValue(
                account?.operator ?: request.receivingMethods.keys.first(),
            ),
            ussdAvailable = account != null &&
                paymentLaunchService.isAvailable(request = request, account = account, configs = payer.ussdConfigs),
            remainingSeconds = Duration.between(now, request.expiresAt).seconds,
            activePermission = interaction.activePermission,
            choosingAccount = interaction.choosingAccount,
            rememberChoice = interaction.rememberChoice,
            confirmationStep = interaction.confirmationStep,
            launchOutcome = interaction.launchOutcome,
        )
    }

    companion object {
        /** Name of the route argument, see `PaymentSummaryRoute`. */
        const val PAYMENT_URL = "url"

        private const val TICK_MS = 1_000L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
