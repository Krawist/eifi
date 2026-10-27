package com.eifi.core.ussd

import com.eifi.core.payload.ReceivingMethod

/** Fills the USSD code of a payment from the [OperatorUssdConfig] of its operator. */
object UssdCodeService {
    private const val CODE = "{code}"
    private const val AMOUNT = "{amount}"

    /** The code paying [amount] FCFA to [method] with [config], `null` when the config has no code for [method]. */
    fun code(config: OperatorUssdConfig, method: ReceivingMethod, amount: Long): String? {
        val template = when (method) {
            is ReceivingMethod.MerchantCode -> config.merchantPayment
            is ReceivingMethod.PhoneNumber -> config.transfer
        }
        return template
            ?.replace(CODE, method.value)
            ?.replace(AMOUNT, amount.toString())
    }
}
