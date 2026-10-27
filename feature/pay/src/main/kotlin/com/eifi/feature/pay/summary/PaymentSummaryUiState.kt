package com.eifi.feature.pay.summary

import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod

/** What the payment URL gave: the payment to make, or why there is none. */
internal sealed interface PaymentSummaryUiState {
    /**
     * A payment that can be made, until its countdown ends.
     *
     * @property displayName name of the payee.
     * @property amount amount to pay, in FCFA.
     * @property operators the operators the payee accepts, among which the payer chooses their account.
     * @property operator the payer's account: the chosen one, else the last one used if the payee accepts it, else
     * the first one the payee accepts.
     * @property receivingMethod where the money goes with [operator]: decides between "Payer" and "Envoyer".
     * @property remainingSeconds time left before the payee's code expires.
     * @property confirmed whether the payer has confirmed; the biometric prompt comes with EIFI-203.
     */
    data class Ready(
        val displayName: String,
        val amount: Long,
        val operators: List<Operator>,
        val operator: Operator,
        val receivingMethod: ReceivingMethod,
        val remainingSeconds: Long,
        val confirmed: Boolean = false,
    ) : PaymentSummaryUiState

    /** The payee's code has expired, before or while the summary shows: they must show a new one. */
    data object Expired : PaymentSummaryUiState

    /** The URL comes from a newer version of Eifi: the payer should update the app. */
    data object UpdateNeeded : PaymentSummaryUiState

    /** Not an Eifi payment, or damaged. */
    data object Invalid : PaymentSummaryUiState
}
