package com.eifi.feature.receive.amount

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class AmountInputTest {
    @Nested
    inner class Append {
        @ParameterizedTest
        @CsvSource("0, 2, 2", "2, 5, 25", "25, 000, 25000", "0, 000, 0", "0, 0, 0")
        fun `should add the digits at the end`(amount: Long, digits: String, expected: Long) {
            // when
            val next = AmountInput.append(amount, digits)

            // then
            next shouldBe expected
        }

        @ParameterizedTest
        @CsvSource("2000000, 0", "300000, 000", "200001, 0")
        fun `should ignore a key that goes past 2 000 000`(amount: Long, digits: String) {
            // when
            val next = AmountInput.append(amount, digits)

            // then
            next shouldBe amount
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should remove the last digit`() {
            // when
            val next = AmountInput.delete(2_500)

            // then
            next shouldBe 250
        }
    }

    @Nested
    inner class IsCollectable {
        @ParameterizedTest
        @ValueSource(longs = [100, 2_500, 2_000_000])
        fun `should accept an amount from 100 to 2 000 000`(amount: Long) {
            // when & then
            AmountInput.isCollectable(amount).shouldBeTrue()
        }

        @ParameterizedTest
        @ValueSource(longs = [0, 99])
        fun `should refuse an amount below 100`(amount: Long) {
            // when & then
            AmountInput.isCollectable(amount).shouldBeFalse()
        }
    }
}
