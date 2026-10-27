package com.eifi.core.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * How the payee receives money with one operator. Serializable for the receiving profile stored locally.
 * Validated by [ReceivingMethodService].
 */
@Serializable
sealed interface ReceivingMethod {
    /** The merchant code, or the phone number in E.164 format, typed in the operator's USSD code. */
    val value: String

    /** Merchant payment, free for the payer. The exact code format is only known after EIFI-001. */
    @Serializable
    @SerialName("merchantCode")
    data class MerchantCode(override val value: String) : ReceivingMethod

    /**
     * Person-to-person transfer to a phone number in E.164 format ("+237690123456"), whatever the country.
     * Fees are paid by the payer.
     */
    @Serializable
    @SerialName("phoneNumber")
    data class PhoneNumber(override val value: String) : ReceivingMethod
}
