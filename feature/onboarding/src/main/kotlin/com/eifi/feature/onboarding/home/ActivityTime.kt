package com.eifi.feature.onboarding.home

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/** When a payment happened, as shown in the activity list. */
internal sealed interface ActivityTime {
    data class Today(val time: LocalTime) : ActivityTime
    data object Yesterday : ActivityTime
    data class Earlier(val date: LocalDate) : ActivityTime
}

internal fun activityTime(at: Instant, now: ZonedDateTime): ActivityTime {
    val dateTime = at.atZone(now.zone)
    val today = now.toLocalDate()
    return when (dateTime.toLocalDate()) {
        today -> ActivityTime.Today(dateTime.toLocalTime())
        today.minusDays(1) -> ActivityTime.Yesterday
        else -> ActivityTime.Earlier(dateTime.toLocalDate())
    }
}
