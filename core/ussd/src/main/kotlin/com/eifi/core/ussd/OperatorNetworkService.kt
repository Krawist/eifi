package com.eifi.core.ussd

import com.eifi.core.payload.Operator

/**
 * Recognises the mobile money operator of a SIM from its network code (MCC + MNC). Codes from the ITU E.212
 * list: Cameroon is MCC 624, MTN Cameroon MNC 01, Orange Cameroun MNC 02.
 */
object OperatorNetworkService {
    private val operators = mapOf(
        "62401" to Operator.MtnMomo,
        "62402" to Operator.OrangeMoney,
    )

    /** The operator of the network [mcc] + [mnc], `null` when Eifi does not support it. */
    fun operatorOf(mcc: String, mnc: String): Operator? = operators[mcc + mnc]
}
