package com.eifi.core.payload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ReceivingMethodTest {
    @Nested
    inner class PhoneNumber {
        @ParameterizedTest
        @ValueSource(strings = ["", "+237690123456", "690 12 34 56", "1234567890123456"])
        fun `should reject a value that is not 1 to 15 digits`(value: String) {
            // when & then
            shouldThrow<IllegalArgumentException> {
                ReceivingMethod.PhoneNumber(value)
            } shouldHaveMessage "A phone number must have 1 to 15 digits"
        }
    }

    @Nested
    inner class MerchantCode {
        @Test
        fun `should reject a blank code`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                ReceivingMethod.MerchantCode(" ")
            } shouldHaveMessage "A merchant code must have 1 to 32 characters"
        }
    }
}
