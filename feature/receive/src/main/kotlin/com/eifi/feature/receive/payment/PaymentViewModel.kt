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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The received payment waiting to be paid: its payment URL, shown as a QR code (EIFI-103) and emitted over NFC
 * while the payment is pending (EIFI-104), and a countdown refreshed every second, which stores the expiry when it
 * reaches zero.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val paymentService: PaymentService,
    private val paymentChannelService: PaymentChannelService,
    private val clock: Clock,
) : ViewModel() {

    private val paymentId: String = checkNotNull(savedStateHandle[PAYMENT_ID]) { "No payment id" }

    private val payment = paymentRepository.getByIdAsFlow(paymentId).filterNotNull()

    /** Encoded once per change of the payment or the profile, not at each tick. */
    private val url = payment.flatMapLatest { paymentChannelService.url(it) }

    /** The URL emitted over NFC, as long as it is stored as pending. */
    private var emittedUrl: String? = null

    private val ticks = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MS)
        }
    }

    private val restartedPaymentId = MutableStateFlow<String?>(null)

    /** `null` while the payment loads. */
    val uiState: StateFlow<PaymentUiState?> = combine(
        payment,
        url,
        ticks,
        paymentChannelService.nfcStatus,
        restartedPaymentId,
    ) { payment, url, now, nfcStatus, restartedPaymentId ->
        PaymentUiState(
            amount = payment.amount,
            label = payment.label,
            status = paymentService.statusAt(payment = payment, now = now),
            remainingSeconds = Duration.between(now, payment.expiresAt).seconds.coerceAtLeast(0),
            url = url,
            nfcStatus = nfcStatus,
            restartedPaymentId = restartedPaymentId,
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initialValue = null)

    init {
        viewModelScope.launch {
            val payment = payment.first()
            delay(Duration.between(clock.instant(), payment.expiresAt).toMillis().coerceAtLeast(0))
            paymentRepository.expireOverdue()
        }
        viewModelScope.launch {
            combine(payment, url) { payment, url -> url.takeIf { payment.status == PaymentStatus.Pending } }
                .distinctUntilChanged()
                .collect(::emit)
        }
    }

    override fun onCleared() {
        emit(null)
    }

    /** Cancels the payment while it is still pending; once expired or confirmed, there is nothing to cancel. */
    fun cancel() {
        if (uiState.value?.status != PaymentStatus.Pending) return
        viewModelScope.launch { paymentRepository.cancel(paymentId) }
    }

    /** Opens a new payment with the same amount, label and operators, once this one has expired. */
    fun restart() {
        val state = uiState.value ?: return
        if (state.status != PaymentStatus.Expired) return
        viewModelScope.launch {
            val accepted = payment.first().acceptedOperators
            restartedPaymentId.value =
                paymentRepository.openReceived(
                    amount = state.amount,
                    label = state.label,
                    acceptedOperators = accepted,
                ).id
        }
    }

    /** Emits [url] over NFC instead of the previous one; `null` stops the emission. */
    private fun emit(url: String?) {
        url?.let(paymentChannelService::emit)
        emittedUrl?.takeIf { it != url }?.let(paymentChannelService::stopEmitting)
        emittedUrl = url
    }

    companion object {
        /** Name of the route argument, see `PaymentRoute`. */
        const val PAYMENT_ID = "paymentId"

        private const val TICK_MS = 1_000L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
