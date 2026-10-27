package com.eifi.feature.receive.profile

import com.eifi.feature.receive.profile.phone.CameroonPhoneCountry
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class OperatorFormTest {
    @Nested
    inner class Error {
        @ParameterizedTest
        @CsvSource(
            "PhoneNumber, '', PhoneRequired",
            "PhoneNumber, 6901, PhoneInvalid",
            "MerchantCode, '', MerchantCodeRequired",
            "MerchantCode, ' ', MerchantCodeRequired",
        )
        fun `should explain why the value cannot be saved`(
            kind: ReceivingMethodKind,
            value: String,
            expected: OperatorFormError,
        ) {
            // given
            val form =
                OperatorForm(country = CameroonPhoneCountry, kind = kind, phoneNumber = value, merchantCode = value)

            // when
            val error = form.error

            // then
            error shouldBe expected
        }

        @Test
        fun `should have no error for a valid number`() {
            // given
            val form = OperatorForm(country = CameroonPhoneCountry, phoneNumber = "690123456")

            // when
            val error = form.error

            // then
            error.shouldBeNull()
        }
    }
}
