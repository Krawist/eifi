package com.eifi.feature.pay.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.PayingAccountRepository
import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.PaymentUrlCodec
import com.eifi.core.payload.PaymentUrlDecodingResult
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
import kotlinx.coroutines.launch

/**
 * Summary of the payment URL read by NFC, the scanner or an App Link, decoded once when the summary opens: the
 * payer's account, a countdown refreshed every second, and the confirmation, refused once the code has expired.
 */
@HiltViewModel
internal class PaymentSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    paymentUrlCodec: PaymentUrlCodec,
    private val payingAccountRepository: PayingAccountRepository,
    private val clock: Clock,
) : ViewModel() {

    private val decoded = paymentUrlCodec.decode(checkNotNull(savedStateHandle[PAYMENT_URL]) { "No payment URL" })

    /** The account picked with "Changer", `null` until then. */
    private val chosenOperator = MutableStateFlow<Operator?>(null)

    private val confirmed = MutableStateFlow(false)

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MS)
        }
    }

    /** `null` while the last account used loads. */
    val uiState: StateFlow<PaymentSummaryUiState?> = when (decoded) {
        is PaymentUrlDecodingResult.Valid -> combine(
            chosenOperator,
            payingAccountRepository.lastOperator,
            confirmed,
            ticks,
        ) { chosenOperator, lastOperator, confirmed, now ->
            ready(
                request = decoded.request,
                operator = chosenOperator ?: lastOperator,
                confirmed = confirmed,
                now = now,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = null,
        )
        is PaymentUrlDecodingResult.Expired -> MutableStateFlow(PaymentSummaryUiState.Expired)
        is PaymentUrlDecodingResult.UnknownVersion -> MutableStateFlow(PaymentSummaryUiState.UpdateNeeded)
        is PaymentUrlDecodingResult.Malformed -> MutableStateFlow(PaymentSummaryUiState.Invalid)
    }

    /** Pays with the account of [operator], one of those the payee accepts. */
    fun selectOperator(operator: Operator) {
        chosenOperator.value = operator
    }

    /** Confirms the payment while the code is valid, and remembers the account for the next one. */
    fun confirm() {
        val state = uiState.value as? PaymentSummaryUiState.Ready ?: return
        confirmed.value = true
        // TODO(EIFI-203): biometric prompt, then the USSD code (EIFI-204).
        viewModelScope.launch { payingAccountRepository.saveLastOperator(state.operator) }
    }

    /** [operator] if the payee accepts it, else their first operator; [PaymentSummaryUiState.Expired] once expired. */
    private fun ready(request: PaymentRequest, operator: Operator?, confirmed: Boolean, now: Instant) =
        if (now.isBefore(request.expiresAt)) {
            val payingOperator =
                operator?.takeIf { it in request.receivingMethods } ?: request.receivingMethods.keys.first()
            PaymentSummaryUiState.Ready(
                displayName = request.displayName,
                amount = request.amount,
                operators = request.receivingMethods.keys.toList(),
                operator = payingOperator,
                receivingMethod = request.receivingMethods.getValue(payingOperator),
                remainingSeconds = Duration.between(now, request.expiresAt).seconds,
                confirmed = confirmed,
            )
        } else {
            PaymentSummaryUiState.Expired
        }

    companion object {
        /** Name of the route argument, see `PaymentSummaryRoute`. */
        const val PAYMENT_URL = "url"

        private const val TICK_MS = 1_000L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
