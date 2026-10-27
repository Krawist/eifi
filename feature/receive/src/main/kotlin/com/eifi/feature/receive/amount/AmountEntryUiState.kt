package com.eifi.feature.receive.amount

import com.eifi.core.payload.Operator

/**
 * State of the "Saisie du montant" screen.
 *
 * @property displayName name of the receiving profile, shown above the amount.
 * @property acceptedOperators operators of the receiving profile, shown as pills.
 * @property amount amount typed on the keypad, in FCFA; 0 while nothing is typed.
 * @property label optional note of the payment, which stays on the phone.
 * @property labelShown whether the user asked to add a label, which shows its field.
 * @property maximumReached whether the last key was ignored because the amount would go past [AmountInput.MAX].
 * @property openedPaymentId set once "Encaisser" has opened the payment, so that the screen shows it.
 */
internal data class AmountEntryUiState(
    val displayName: String = "",
    val acceptedOperators: List<Operator> = emptyList(),
    val amount: Long = 0,
    val label: String = "",
    val labelShown: Boolean = false,
    val maximumReached: Boolean = false,
    val openedPaymentId: String? = null,
) {
    /** Below [AmountInput.MIN], which explains why the amount cannot be collected yet. */
    val belowMinimum: Boolean
        get() = amount in 1 until AmountInput.MIN

    val canCollect: Boolean
        get() = AmountInput.isCollectable(amount)
}
