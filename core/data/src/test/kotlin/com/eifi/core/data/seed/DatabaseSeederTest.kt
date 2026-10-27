package com.eifi.core.data.seed

import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.eifi.core.data.database.EifiDatabase
import com.eifi.core.data.database.PaymentEntity
import com.eifi.core.data.database.RoomUssdConfigRepository
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.payload.Operator
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** On a real in-memory Room database, with the bundled SQLite of the JVM. */
internal class DatabaseSeederTest {
    private val database = Room.inMemoryDatabaseBuilder(ContextWrapper(null), EifiDatabase::class.java)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private val now = Instant.parse("2026-10-27T10:00:00Z")

    private val seeder = DatabaseSeeder(database = database, clock = Clock.fixed(now, ZoneOffset.UTC))

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Seed {
        @Test
        fun `should preload the USSD codes of both operators`() = runTest {
            // when
            seeder.seed()

            // then
            RoomUssdConfigRepository(database).configs.first().map { it.operator }
                .shouldContainExactlyInAnyOrder(Operator.OrangeMoney, Operator.MtnMomo)
        }

        @Test
        fun `should preload the sample payments in an empty database`() = runTest {
            // when
            seeder.seed()

            // then
            database.paymentDao().getAllAsFlow().first() shouldHaveSize SeedData.payments(now).size
        }

        @Test
        fun `should leave the payments of a database already used`() = runTest {
            // given
            val payment = Payment(
                id = "0a1b2c3d4e5f",
                type = TransactionType.Sent,
                amount = 2_500,
                label = null,
                createdAt = now,
                expiresAt = now.plusSeconds(120),
            )
            database.paymentDao().insert(PaymentEntity.of(payment))

            // when
            seeder.seed()

            // then
            database.paymentDao().getAllAsFlow().first().map { it.toPayment() } shouldBe listOf(payment)
        }

        @Test
        fun `should not preload twice`() = runTest {
            // given
            seeder.seed()

            // when
            seeder.seed()

            // then
            RoomUssdConfigRepository(database).configs.first() shouldHaveSize 2
        }
    }
}
