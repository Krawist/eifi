package com.eifi.core.ussd

import com.eifi.core.payload.Operator
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class OperatorNetworkServiceTest {
    @Nested
    inner class OperatorOf {
        @ParameterizedTest
        @CsvSource("01, MtnMomo", "02, OrangeMoney")
        fun `should recognise the Cameroonian networks of the operators`(mnc: String, operator: Operator) {
            // when
            val result = OperatorNetworkService.operatorOf(mcc = "624", mnc = mnc)

            // then
            result shouldBe operator
        }

        @ParameterizedTest
        @CsvSource("624, 04", "208, 01", "208, 02")
        fun `should not recognise other networks`(mcc: String, mnc: String) {
            // when
            val result = OperatorNetworkService.operatorOf(mcc = mcc, mnc = mnc)

            // then
            result.shouldBeNull()
        }
    }
}
