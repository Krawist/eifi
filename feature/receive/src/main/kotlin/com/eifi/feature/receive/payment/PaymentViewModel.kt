package com.eifi.feature.receive.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.payload.PaymentUrlCodec
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The received payment waiting to be paid (EIFI-103): its payment URL, and a countdown refreshed every second,
 * which stores the expiry when it reaches zero.
 */
@HiltViewModel
internal class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    receivingProfileRepository: ReceivingProfileRepository,
    private val paymentService: PaymentService,
    paymentUrlCodec: PaymentUrlCodec,
    private val clock: Clock,
) : ViewModel() {

    private val paymentId: String = checkNotNull(savedStateHandle[PAYMENT_ID]) { "No payment id" }

    private val payment = paymentRepository.getByIdAsFlow(paymentId).filterNotNull()

    /** Encoded once per change of the payment or the profile, not at each tick. */
    private val url = combine(payment, receivingProfileRepository.profile) { payment, profile ->
        profile?.let { paymentUrlCodec.encode(paymentService.toPaymentRequest(payment = payment, profile = it)) }
    }

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MS)
        }
    }

    private val restartedPaymentId = MutableStateFlow<String?>(null)

    /** `null` while the payment loads. */
    val uiState: StateFlow<PaymentUiState?> = combine(payment, url, ticks, restartedPaymentId) {
            payment,
            url,
            now,
            restartedPaymentId,
        ->
        PaymentUiState(
            amount = payment.amount,
            label = payment.label,
            status = paymentService.statusAt(payment = payment, now = now),
            remainingSeconds = Duration.between(now, payment.expiresAt).seconds.coerceAtLeast(0),
            url = url,
            restartedPaymentId = restartedPaymentId,
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initialValue = null)

    init {
        viewModelScope.launch {
            val payment = payment.first()
            delay(Duration.between(clock.instant(), payment.expiresAt).toMillis().coerceAtLeast(0))
            paymentRepository.expireOverdue()
        }
    }

    /** Cancels the payment while it is still pending; once expired or confirmed, there is nothing to cancel. */
    fun cancel() {
        if (uiState.value?.status != PaymentStatus.Pending) return
        viewModelScope.launch { paymentRepository.cancel(paymentId) }
    }

    /** Opens a new payment with the same amount and label, once this one has expired. */
    fun restart() {
        val state = uiState.value ?: return
        if (state.status != PaymentStatus.Expired) return
        viewModelScope.launch {
            restartedPaymentId.value = paymentRepository.openReceived(amount = state.amount, label = state.label).id
        }
    }

    companion object {
        /** Name of the route argument, see `PaymentRoute`. */
        const val PAYMENT_ID = "paymentId"

        private const val TICK_MS = 1_000L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
