package com.eifi.feature.onboarding.home

import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ActivityTimeTest {
    private val now = ZonedDateTime.of(2026, 10, 27, 10, 0, 0, 0, ZoneId.of("Africa/Douala"))

    @Nested
    inner class ActivityTimeOf {
        @Test
        fun `should give the time of a payment made today`() {
            // when
            val time = activityTime(Instant.parse("2026-10-27T07:32:00Z"), now)

            // then
            time shouldBe ActivityTime.Today(LocalTime.of(8, 32))
        }

        @Test
        fun `should use the local day, not the UTC day`() {
            // when
            val time = activityTime(Instant.parse("2026-10-26T23:30:00Z"), now)

            // then
            time shouldBe ActivityTime.Today(LocalTime.of(0, 30))
        }

        @Test
        fun `should say yesterday for a payment of the day before`() {
            // when
            val time = activityTime(Instant.parse("2026-10-26T12:00:00Z"), now)

            // then
            time shouldBe ActivityTime.Yesterday
        }

        @Test
        fun `should give the date of an older payment`() {
            // when
            val time = activityTime(Instant.parse("2026-10-20T12:00:00Z"), now)

            // then
            time shouldBe ActivityTime.Earlier(LocalDate.of(2026, 10, 20))
        }
    }
}
