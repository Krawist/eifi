package com.eifi.core.permission

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class DeviceBrandTest {
    @Nested
    inner class Of {
        @ParameterizedTest
        @CsvSource(
            "TECNO MOBILE LIMITED, TECNO, Tecno",
            "INFINIX MOBILITY LIMITED, Infinix, Infinix",
            "ITEL, itel, Itel",
            "unknown, itel, Itel",
        )
        fun `should recognise the brand from the manufacturer or the brand`(
            manufacturer: String,
            brand: String,
            expected: DeviceBrand,
        ) {
            // when
            val detected = DeviceBrand.of(manufacturer = manufacturer, brand = brand)

            // then
            detected shouldBe expected
        }

        @Test
        fun `should not give a guide for another brand`() {
            // when
            val detected = DeviceBrand.of(manufacturer = "samsung", brand = "samsung")

            // then
            detected.shouldBeNull()
        }
    }
}
