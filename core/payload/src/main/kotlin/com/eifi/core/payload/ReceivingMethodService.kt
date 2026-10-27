package com.eifi.core.payload

/** Rules of a [ReceivingMethod]. */
object ReceivingMethodService {
    const val MAX_MERCHANT_CODE_LENGTH = 32

    /** "+", then the country code and the national number: 15 digits at most, never starting with 0. */
    private val E164 = Regex("\\+[1-9]\\d{1,14}")

    fun isValid(method: ReceivingMethod): Boolean = when (method) {
        is ReceivingMethod.MerchantCode ->
            method.value.isNotBlank() && method.value.length <= MAX_MERCHANT_CODE_LENGTH
        is ReceivingMethod.PhoneNumber -> E164.matches(method.value)
    }

    /** @throws IllegalArgumentException when [method] is not valid. */
    fun validate(method: ReceivingMethod) {
        require(isValid(method)) {
            when (method) {
                is ReceivingMethod.MerchantCode ->
                    "A merchant code must have 1 to $MAX_MERCHANT_CODE_LENGTH characters"
                is ReceivingMethod.PhoneNumber -> "A phone number must be in E.164 format, e.g. +237690123456"
            }
        }
    }
}
