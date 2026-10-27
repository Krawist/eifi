package com.eifi.feature.receive.amount

import com.eifi.core.payload.Operator

/**
 * [labelShown] once the user asked to add a label, which is optional and stays on the phone.
 * [maximumReached] when the last key was ignored because the amount would go past [AmountInput.MAX].
 */
internal data class AmountEntryUiState(
    val displayName: String = "",
    val acceptedOperators: List<Operator> = emptyList(),
    val amount: Long = 0,
    val label: String = "",
    val labelShown: Boolean = false,
    val maximumReached: Boolean = false,
) {
    /** Below [AmountInput.MIN], which explains why the amount cannot be collected yet. */
    val belowMinimum: Boolean
        get() = amount in 1 until AmountInput.MIN

    val canCollect: Boolean
        get() = AmountInput.isCollectable(amount)
}
