package com.eifi.feature.onboarding.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** "Historique": every payment received and sent, grouped by day (EIFI-207). */
@HiltViewModel
internal class HistoryViewModel @Inject constructor(
    paymentRepository: PaymentRepository,
    private val paymentService: PaymentService,
    private val clock: Clock,
) : ViewModel() {

    /** `null` while the payments load. */
    val uiState: StateFlow<HistoryUiState?> = paymentRepository.getAllAsFlow()
        .map { payments ->
            HistoryUiState(
                days = payments.groupBy { it.createdAt.atZone(clock.zone).toLocalDate() }
                    .map { (date, ofDay) -> HistoryUiState.Day(date = date, entries = ofDay.map(::entry)) },
            )
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initialValue = null)

    private fun entry(payment: Payment) = HistoryUiState.Entry(
        id = payment.id,
        type = payment.type,
        counterpart = payment.counterpartyName,
        label = payment.label,
        operator = payment.operator,
        amount = payment.amount,
        at = payment.createdAt,
        status = status(payment),
    )

    private fun status(payment: Payment): HistoryStatus {
        val status = paymentService.statusAt(payment = payment, now = clock.instant())
        return when (payment.type) {
            TransactionType.Sent ->
                if (status == PaymentStatus.Confirmed) HistoryStatus.DebitConfirmed else HistoryStatus.RequestSent
            TransactionType.Received -> when (status) {
                PaymentStatus.Pending -> HistoryStatus.Waiting
                PaymentStatus.Confirmed ->
                    if (payment.confirmationSource ==
                        ConfirmationSource.Manual
                    ) {
                        HistoryStatus.Manual
                    } else {
                        HistoryStatus.Confirmed
                    }
                PaymentStatus.Expired -> HistoryStatus.Expired
                PaymentStatus.Cancelled -> HistoryStatus.Cancelled
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
