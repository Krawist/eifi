package com.eifi.core.data.fake

import com.eifi.core.data.ActivityItem
import com.eifi.core.data.ActivityRepository
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.payload.Operator
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Sample activity, as on the home mockup. TODO(EIFI-102, EIFI-207): replace with the sessions and receipts in Room. */
internal class FakeActivityRepository @Inject constructor(clock: Clock) : ActivityRepository {
    override val recentActivities: Flow<List<ActivityItem>> = flowOf(
        listOf(
            ActivityItem(
                id = "fake-received",
                type = TransactionType.Received,
                counterpart = "+237 6XX XX XX 41",
                operator = Operator.OrangeMoney,
                amount = 2_500,
                at = clock.instant().minus(Duration.ofMinutes(25)),
            ),
            ActivityItem(
                id = "fake-sent",
                type = TransactionType.Sent,
                counterpart = "Boutique Mama Ngono",
                operator = Operator.MtnMomo,
                amount = 1_000,
                at = clock.instant().minus(Duration.ofDays(1)),
            ),
        ),
    )
}
