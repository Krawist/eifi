package com.eifi.feature.pay.summary

/** What the payment URL gave: the payment to make, or why there is none. */
internal sealed interface PaymentSummaryUiState {
    /**
     * A payment that can be made.
     *
     * @property displayName name of the payee.
     * @property amount amount to pay, in FCFA.
     */
    data class Ready(val displayName: String, val amount: Long) : PaymentSummaryUiState

    /** The payee's code has expired: they must show a new one. */
    data object Expired : PaymentSummaryUiState

    /** The URL comes from a newer version of Eifi: the payer should update the app. */
    data object UpdateNeeded : PaymentSummaryUiState

    /** Not an Eifi payment, or damaged. */
    data object Invalid : PaymentSummaryUiState
}
