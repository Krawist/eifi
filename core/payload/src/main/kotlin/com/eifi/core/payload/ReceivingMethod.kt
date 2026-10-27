package com.eifi.core.payload

/** How the payee receives money with one operator. */
sealed interface ReceivingMethod {
    val value: String

    /** Merchant payment, free for the payer. The exact code format is only known after EIFI-001. */
    data class MerchantCode(override val value: String) : ReceivingMethod {
        init {
            require(value.isNotBlank() && value.length <= MAX_VALUE_LENGTH) {
                "A merchant code must have 1 to $MAX_VALUE_LENGTH characters"
            }
        }
    }

    /** Person-to-person transfer to a phone number, digits only. Fees are paid by the payer. */
    data class PhoneNumber(override val value: String) : ReceivingMethod {
        init {
            require(value.length in 1..MAX_PHONE_DIGITS && value.all { it in '0'..'9' }) {
                "A phone number must have 1 to $MAX_PHONE_DIGITS digits"
            }
        }
    }

    private companion object {
        const val MAX_VALUE_LENGTH = 32

        /** E.164 limit. */
        const val MAX_PHONE_DIGITS = 15
    }
}
