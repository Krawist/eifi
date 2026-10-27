package com.eifi.feature.receive.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.data.payment.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The payment waiting to be paid (EIFI-102), until the QR code and NFC of EIFI-103: a countdown refreshed
 * every second, which stores the expiry when it reaches zero.
 */
@HiltViewModel
internal class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val paymentService: PaymentService,
    private val clock: Clock,
) : ViewModel() {

    private val paymentId: String = checkNotNull(savedStateHandle[PAYMENT_ID]) { "No payment id" }

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MS)
        }
    }

    /** `null` while the payment loads. */
    val uiState: StateFlow<PaymentUiState?> = combine(
        paymentRepository.getByIdAsFlow(paymentId).filterNotNull(),
        ticks,
    ) { payment, now ->
        PaymentUiState(
            amount = payment.amount,
            label = payment.label,
            status = paymentService.statusAt(payment = payment, now = now),
            remainingSeconds = Duration.between(now, payment.expiresAt).seconds.coerceAtLeast(0),
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initialValue = null)

    init {
        viewModelScope.launch {
            val payment = paymentRepository.getByIdAsFlow(paymentId).filterNotNull().first()
            delay(Duration.between(clock.instant(), payment.expiresAt).toMillis().coerceAtLeast(0))
            paymentRepository.expireOverdue()
        }
    }

    /** Cancels the payment while it is still pending; once expired or confirmed, there is nothing to cancel. */
    fun cancel() {
        if (uiState.value?.status != PaymentStatus.Pending) return
        viewModelScope.launch { paymentRepository.cancel(paymentId) }
    }

    companion object {
        /** Name of the route argument, see `PaymentRoute`. */
        const val PAYMENT_ID = "paymentId"

        private const val TICK_MS = 1_000L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
