package com.eifi.feature.pay.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The receipt of a sent payment, from the local history. */
@HiltViewModel
internal class ReceiptViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    paymentRepository: PaymentRepository,
) : ViewModel() {

    private val paymentId: String = checkNotNull(savedStateHandle[PAYMENT_ID]) { "No payment id" }

    /** `null` while the payment loads. */
    val uiState: StateFlow<ReceiptUiState?> = paymentRepository.getByIdAsFlow(paymentId)
        .filterNotNull()
        .map {
            ReceiptUiState(
                payeeName = it.counterpartyName,
                amount = it.amount,
                operator = it.operator,
                sentAt = it.createdAt,
                debitConfirmed = it.status == PaymentStatus.Confirmed,
            )
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), initialValue = null)

    companion object {
        /** Name of the route argument, see `ReceiptRoute`. */
        const val PAYMENT_ID = "paymentId"

        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
