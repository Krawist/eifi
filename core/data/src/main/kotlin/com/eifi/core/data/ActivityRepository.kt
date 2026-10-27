package com.eifi.core.data

import kotlinx.coroutines.flow.Flow

/** Payments received and sent, mixed. */
interface ActivityRepository {
    /** Latest first. */
    val recentActivities: Flow<List<ActivityItem>>
}
