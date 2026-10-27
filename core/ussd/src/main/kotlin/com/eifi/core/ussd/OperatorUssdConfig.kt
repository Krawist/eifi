package com.eifi.core.ussd

import com.eifi.core.payload.Operator

/**
 * The USSD codes of an operator, one template per operation, filled by [UssdCodeService]. Placeholders: `{code}` for
 * the merchant code or the number, `{amount}` for the amount in FCFA.
 *
 * @property operator the mobile money operator.
 * @property merchantPayment payment to a merchant code, e.g. `#150*47*{code}*{amount}#`; `null` when not supported.
 * @property transfer transfer to a phone number; `null` when not supported.
 */
data class OperatorUssdConfig(val operator: Operator, val merchantPayment: String?, val transfer: String?)
