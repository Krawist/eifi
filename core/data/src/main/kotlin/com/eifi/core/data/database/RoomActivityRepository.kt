package com.eifi.core.data.database

import com.eifi.core.data.ActivityItem
import com.eifi.core.data.ActivityRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [ActivityRepository] on the payments in Room: those sent, and those received and confirmed. */
internal class RoomActivityRepository @Inject constructor(database: EifiDatabase) : ActivityRepository {
    override val recentActivities: Flow<List<ActivityItem>> =
        database.paymentDao().getRecentActivityAsFlow(RECENT_LIMIT).map { rows ->
            rows.mapNotNull { row ->
                row.operator?.let { operator ->
                    ActivityItem(
                        id = row.id,
                        type = row.type,
                        counterpart = row.counterpartyName.orEmpty(),
                        operator = operator,
                        amount = row.amount,
                        at = Instant.ofEpochMilli(row.createdAt),
                    )
                }
            }
        }

    private companion object {
        /** As many as the home screen shows. */
        const val RECENT_LIMIT = 3
    }
}
