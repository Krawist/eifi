package com.eifi.core.design.format

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class FcfaTest {
    @Nested
    inner class FormatFcfa {
        @ParameterizedTest
        @CsvSource("0, 0", "500, 500", "999, 999")
        fun `should not separate amounts below a thousand`(amount: Long, expected: String) {
            // when
            val formatted = formatFcfa(amount)

            // then
            formatted shouldBe expected
        }

        /** `|` stands for the thin space, invisible in the source. */
        @ParameterizedTest
        @CsvSource("1000, 1|000", "2500, 2|500", "2000000, 2|000|000")
        fun `should separate thousands with a thin space`(amount: Long, expected: String) {
            // when
            val formatted = formatFcfa(amount)

            // then
            formatted shouldBe expected.replace('|', THIN_SPACE)
        }

        @Test
        fun `should keep the sign of a negative amount`() {
            // when
            val formatted = formatFcfa(-1_000)

            // then
            formatted shouldBe "-1${THIN_SPACE}000"
        }
    }
}
