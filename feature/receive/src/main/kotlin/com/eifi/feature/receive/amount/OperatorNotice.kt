package com.eifi.feature.receive.amount

import com.eifi.core.payload.Operator

/** What turning an operator on or off did for this payment, shown once as a short message (EIFI-111). */
internal sealed interface OperatorNotice {
    /** The operator concerned. */
    val operator: Operator

    /** @property operator the payer can pay with it again. */
    data class TurnedOn(override val operator: Operator) : OperatorNotice

    /** @property operator the payer cannot pay with it for this payment. */
    data class TurnedOff(override val operator: Operator) : OperatorNotice

    /** @property operator the last operator selected, which stays on: a payment needs at least one. */
    data class LastKept(override val operator: Operator) : OperatorNotice
}
