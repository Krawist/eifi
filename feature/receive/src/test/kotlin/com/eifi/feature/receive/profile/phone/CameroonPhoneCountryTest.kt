package com.eifi.feature.receive.profile.phone

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class CameroonPhoneCountryTest {
    @Nested
    inner class Flag {
        @Test
        fun `should be the Cameroonian flag`() {
            // when
            val flag = CameroonPhoneCountry.flag

            // then
            flag shouldBe "🇨🇲"
        }
    }

    @Nested
    inner class IsValid {
        @ParameterizedTest
        @ValueSource(strings = ["690123456", "655123456", "677123456", "650123456"])
        fun `should accept a Cameroonian mobile number`(number: String) {
            // when & then
            CameroonPhoneCountry.isValid(number).shouldBeTrue()
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "69012345", "6901234567", "222234567", "69012345a", "+237690123456"])
        fun `should reject a number that is not a national Cameroonian mobile number`(number: String) {
            // when & then
            CameroonPhoneCountry.isValid(number).shouldBeFalse()
        }
    }

    @Nested
    inner class ToE164 {
        @Test
        fun `should add the country code to a national number`() {
            // when
            val e164 = CameroonPhoneCountry.toE164("690123456")

            // then
            e164 shouldBe "+237690123456"
        }
    }

    @Nested
    inner class NationalNumberOf {
        @Test
        fun `should give the national number of a Cameroonian number`() {
            // when
            val national = CameroonPhoneCountry.nationalNumberOf("+237690123456")

            // then
            national shouldBe "690123456"
        }

        @ParameterizedTest
        @ValueSource(strings = ["+33612345678", "+237222234567", "690123456"])
        fun `should give nothing for another country or a landline`(e164: String) {
            // when
            val national = CameroonPhoneCountry.nationalNumberOf(e164)

            // then
            national.shouldBeNull()
        }
    }

    @Nested
    inner class Normalize {
        @ParameterizedTest
        @CsvSource(
            "690123456, 690123456",
            "'6 90 12 34 56', 690123456",
            "'+237 6 90 12 34 56', 690123456",
            "'00237690123456', 690123456",
            "6901234567, 690123456",
        )
        fun `should keep the national digits`(input: String, expected: String) {
            // when
            val normalized = CameroonPhoneCountry.normalize(input)

            // then
            normalized shouldBe expected
        }
    }
}
