package com.eifi.feature.receive.amount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.payload.Operator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** "Saisie du montant": the amount typed on the keypad, with the name and operators of the receiving profile. */
@HiltViewModel
internal class AmountEntryViewModel @Inject constructor(
    receivingProfileRepository: ReceivingProfileRepository,
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmountEntryUiState())
    val uiState: StateFlow<AmountEntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            receivingProfileRepository.profile.first()?.let { profile ->
                _uiState.update {
                    it.copy(
                        displayName = profile.displayName,
                        acceptedOperators = Operator.entries.filter { operator ->
                            operator in profile.receivingMethods
                        },
                    )
                }
            }
        }
    }

    /** [digits] is "0" to "9" or "000", as sent by the keypad; a key going past the maximum is ignored. */
    fun onDigits(digits: String) {
        _uiState.update {
            if (AmountInput.exceedsMax(it.amount, digits)) {
                it.copy(maximumReached = true)
            } else {
                it.copy(amount = AmountInput.append(it.amount, digits), maximumReached = false)
            }
        }
    }

    fun onDelete() {
        _uiState.update { it.copy(amount = AmountInput.delete(it.amount), maximumReached = false) }
    }

    /** Long press on the delete key: clears the whole amount (EIFI-014). */
    fun onClear() {
        _uiState.update { it.copy(amount = 0, maximumReached = false) }
    }

    fun showLabel() {
        _uiState.update { it.copy(labelShown = true) }
    }

    fun onLabelChange(label: String) {
        _uiState.update { it.copy(label = label.take(MAX_LABEL_LENGTH)) }
    }

    /** Opens a new payment of the amount; the one still pending for it is cancelled (EIFI-112). */
    fun collect() {
        val state = _uiState.value
        if (!state.canCollect) return
        viewModelScope.launch {
            val payment = paymentRepository.openReceived(
                amount = state.amount,
                label = state.label.trim().ifEmpty { null },
            )
            _uiState.update { it.copy(openedPaymentId = payment.id) }
        }
    }

    /** The payment screen is shown: coming back to this screen must not open it again. */
    fun onPaymentShown() {
        _uiState.update { it.copy(openedPaymentId = null) }
    }

    private companion object {
        const val MAX_LABEL_LENGTH = 30
    }
}
