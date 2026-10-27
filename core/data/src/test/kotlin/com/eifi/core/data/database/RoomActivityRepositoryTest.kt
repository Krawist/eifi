package com.eifi.core.data.database

import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.payload.Operator
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** On a real in-memory Room database, with the bundled SQLite of the JVM. */
internal class RoomActivityRepositoryTest {
    private val database = Room.inMemoryDatabaseBuilder(ContextWrapper(null), EifiDatabase::class.java)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private val repository = RoomActivityRepository(database)

    private val now = Instant.parse("2026-10-27T10:00:00Z")

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class RecentActivities {
        @Test
        fun `should list the payments sent and those received and confirmed, latest first`() = runTest {
            // given
            val dao = database.paymentDao()
            dao.insert(
                PaymentEntity.of(
                    Payment(
                        id = "000000000001",
                        type = TransactionType.Received,
                        amount = 2_500,
                        label = null,
                        createdAt = now,
                        expiresAt = now.plusSeconds(120),
                        status = PaymentStatus.Confirmed,
                        counterpartyName = "+237 6XX XX XX 41",
                        operator = Operator.OrangeMoney,
                    ),
                ),
            )
            dao.insert(
                PaymentEntity.of(
                    Payment(
                        id = "000000000002",
                        type = TransactionType.Received,
                        amount = 1_000,
                        label = null,
                        createdAt = now.plusSeconds(60),
                        expiresAt = now.plusSeconds(180),
                    ),
                ),
            )
            dao.insert(
                PaymentEntity.of(
                    Payment(
                        id = "000000000003",
                        type = TransactionType.Sent,
                        amount = 500,
                        label = null,
                        createdAt = now.plusSeconds(120),
                        expiresAt = now.plusSeconds(240),
                        counterpartyName = "Kiosque Ndé",
                        operator = Operator.MtnMomo,
                    ),
                ),
            )

            // when
            val activities = repository.recentActivities.first()

            // then
            activities.map { it.id } shouldBe listOf("000000000003", "000000000001")
        }
    }
}
