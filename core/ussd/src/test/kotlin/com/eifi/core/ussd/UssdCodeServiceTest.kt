package com.eifi.core.ussd

import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class UssdCodeServiceTest {
    @Nested
    inner class Code {
        @Test
        fun `should fill the merchant payment code with the merchant code and the amount`() {
            // given
            val config = OperatorUssdConfig(
                operator = Operator.OrangeMoney,
                merchantPayment = "#150*47*{code}*{amount}#",
                transfer = "#150*1*{code}*{amount}#",
            )

            // when
            val code = UssdCodeService.code(
                config = config,
                method = ReceivingMethod.MerchantCode("482913"),
                amount = 2_500,
            )

            // then
            code shouldBe "#150*47*482913*2500#"
        }

        @Test
        fun `should fill the transfer code with the number and the amount`() {
            // given
            val config = OperatorUssdConfig(
                operator = Operator.OrangeMoney,
                merchantPayment = "#150*47*{code}*{amount}#",
                transfer = "#150*1*{code}*{amount}#",
            )

            // when
            val code = UssdCodeService.code(
                config = config,
                method = ReceivingMethod.PhoneNumber("+237690123456"),
                amount = 2_500,
            )

            // then
            code shouldBe "#150*1*+237690123456*2500#"
        }

        @Test
        fun `should give no code for an operation the config does not support`() {
            // given
            val config = OperatorUssdConfig(
                operator = Operator.MtnMomo,
                merchantPayment = "*126*2*{code}*{amount}#",
                transfer = null,
            )

            // when
            val code = UssdCodeService.code(
                config = config,
                method = ReceivingMethod.PhoneNumber("+237670123456"),
                amount = 2_500,
            )

            // then
            code.shouldBeNull()
        }
    }
}
