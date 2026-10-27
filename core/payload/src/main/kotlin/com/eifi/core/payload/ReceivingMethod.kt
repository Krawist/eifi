package com.eifi.core.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How the payee receives money with one operator. Serializable for the receiving profile stored locally. */
@Serializable
sealed interface ReceivingMethod {
    val value: String

    /** Merchant payment, free for the payer. The exact code format is only known after EIFI-001. */
    @Serializable
    @SerialName("merchantCode")
    data class MerchantCode(override val value: String) : ReceivingMethod {
        init {
            require(value.isNotBlank() && value.length <= MAX_VALUE_LENGTH) {
                "A merchant code must have 1 to $MAX_VALUE_LENGTH characters"
            }
        }
    }

    /**
     * Person-to-person transfer to a phone number in E.164 format ("+237690123456"), whatever the country.
     * Fees are paid by the payer.
     */
    @Serializable
    @SerialName("phoneNumber")
    data class PhoneNumber(override val value: String) : ReceivingMethod {
        init {
            require(E164.matches(value)) { "A phone number must be in E.164 format, e.g. +237690123456" }
        }
    }

    private companion object {
        const val MAX_VALUE_LENGTH = 32

        /** "+", then the country code and the national number: 15 digits at most, never starting with 0. */
        val E164 = Regex("\\+[1-9]\\d{1,14}")
    }
}
