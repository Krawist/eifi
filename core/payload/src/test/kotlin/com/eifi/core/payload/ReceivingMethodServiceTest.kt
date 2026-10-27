package com.eifi.core.payload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ReceivingMethodServiceTest {
    @Nested
    inner class IsValid {
        @ParameterizedTest
        @ValueSource(strings = ["+237690123456", "+33612345678", "+12"])
        fun `should accept a number in E164 format`(value: String) {
            // when
            val valid = ReceivingMethodService.isValid(ReceivingMethod.PhoneNumber(value))

            // then
            valid.shouldBeTrue()
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "690123456", "+0690123456", "+237 690123456", "+1234567890123456", "+"])
        fun `should reject a number that is not in E164 format`(value: String) {
            // when
            val valid = ReceivingMethodService.isValid(ReceivingMethod.PhoneNumber(value))

            // then
            valid.shouldBeFalse()
        }

        @ParameterizedTest
        @ValueSource(strings = ["", " ", "123456789012345678901234567890123"])
        fun `should reject a blank merchant code or one longer than 32 characters`(value: String) {
            // when
            val valid = ReceivingMethodService.isValid(ReceivingMethod.MerchantCode(value))

            // then
            valid.shouldBeFalse()
        }
    }

    @Nested
    inner class Validate {
        @Test
        fun `should explain why a phone number is rejected`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                ReceivingMethodService.validate(ReceivingMethod.PhoneNumber("690123456"))
            } shouldHaveMessage "A phone number must be in E.164 format, e.g. +237690123456"
        }

        @Test
        fun `should explain why a merchant code is rejected`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                ReceivingMethodService.validate(ReceivingMethod.MerchantCode(" "))
            } shouldHaveMessage "A merchant code must have 1 to 32 characters"
        }
    }
}
