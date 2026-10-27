package com.eifi.core.payload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ReceivingMethodTest {
    @Nested
    inner class PhoneNumber {
        @ParameterizedTest
        @ValueSource(strings = ["+237690123456", "+33612345678", "+12"])
        fun `should accept a number in E164 format`(value: String) {
            // when
            val phoneNumber = ReceivingMethod.PhoneNumber(value)

            // then
            phoneNumber.value shouldBe value
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "690123456", "+0690123456", "+237 690123456", "+1234567890123456", "+"])
        fun `should reject a number that is not in E164 format`(value: String) {
            // when & then
            shouldThrow<IllegalArgumentException> {
                ReceivingMethod.PhoneNumber(value)
            } shouldHaveMessage "A phone number must be in E.164 format, e.g. +237690123456"
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
