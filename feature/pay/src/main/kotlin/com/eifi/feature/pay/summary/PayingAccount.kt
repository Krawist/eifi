package com.eifi.feature.pay.summary

import com.eifi.core.payload.Operator
import com.eifi.core.ussd.SimCard

/**
 * An account the payer can pay with: an operator the payee accepts, on one of the payer's SIMs.
 *
 * @property operator the mobile money operator.
 * @property sim the SIM of that operator, `null` when the SIMs are not known (phone calls permission refused).
 */
internal data class PayingAccount(val operator: Operator, val sim: SimCard?)
