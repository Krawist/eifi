package com.eifi.core.data.database

import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.payload.Operator
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** On a real in-memory Room database, with the bundled SQLite of the JVM. */
internal class RoomPaymentRepositoryTest {
    private val database = Room.inMemoryDatabaseBuilder(ContextWrapper(null), EifiDatabase::class.java)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private var now = Instant.parse("2026-10-27T10:00:00Z")

    private val clock = object : Clock() {
        override fun instant(): Instant = now

        override fun getZone(): ZoneId = ZoneId.of("Africa/Douala")

        override fun withZone(zone: ZoneId): Clock = this
    }

    private val repository =
        RoomPaymentRepository(database = database, paymentService = PaymentService(), clock = clock)

    @AfterEach
    fun tearDown() {
        database.close()
    }

    private suspend fun stored(id: String): Payment = repository.getByIdAsFlow(id).first().shouldNotBeNull()

    @Nested
    inner class OpenReceived {
        @Test
        fun `should store a pending received payment of 2 minutes`() = runTest {
            // when
            val payment = repository.openReceived(amount = 2_500, label = "Pain")

            // then
            stored(payment.id) shouldBe Payment(
                id = payment.id,
                type = TransactionType.Received,
                amount = 2_500,
                label = "Pain",
                createdAt = now,
                expiresAt = now.plus(Duration.ofMinutes(2)),
            )
        }

        @Test
        fun `should use the id format of the payment URL`() = runTest {
            // when
            val payment = repository.openReceived(amount = 2_500, label = null)

            // then
            payment.id shouldMatch "[0-9a-f]{12}"
        }

        @Test
        fun `should reopen the payment already pending for the same amount`() = runTest {
            // given
            val first = repository.openReceived(amount = 2_500, label = "Pain")
            now = now.plusSeconds(30)

            // when
            val second = repository.openReceived(amount = 2_500, label = "Lait")

            // then
            second shouldBe first
        }

        @Test
        fun `should expire the pending payment before opening a new one`() = runTest {
            // given
            val first = repository.openReceived(amount = 2_500, label = null)
            now = now.plus(Duration.ofMinutes(2))

            // when
            repository.openReceived(amount = 2_500, label = null)

            // then
            stored(first.id).status shouldBe PaymentStatus.Expired
        }

        @Test
        fun `should open separate payments for different amounts`() = runTest {
            // given
            val first = repository.openReceived(amount = 2_500, label = null)

            // when
            val second = repository.openReceived(amount = 1_000, label = null)

            // then
            (second.id == first.id) shouldBe false
        }
    }

    @Nested
    inner class OpenSent {
        @Test
        fun `should store the sent payment with its payee and operator`() = runTest {
            // given
            val payment = Payment(
                id = "0a1b2c3d4e5f",
                type = TransactionType.Sent,
                amount = 2_500,
                label = null,
                createdAt = now,
                expiresAt = now.plusSeconds(120),
                counterpartyName = "Kiosque Ndé",
                operator = Operator.OrangeMoney,
            )

            // when
            val opened = repository.openSent(payment)

            // then
            opened shouldBe true
            stored("0a1b2c3d4e5f") shouldBe payment
        }

        @Test
        fun `should refuse a payment already sent`() = runTest {
            // given
            val payment = Payment(
                id = "0a1b2c3d4e5f",
                type = TransactionType.Sent,
                amount = 2_500,
                label = null,
                createdAt = now,
                expiresAt = now.plusSeconds(120),
            )
            repository.openSent(payment)

            // when
            val opened = repository.openSent(payment.copy(createdAt = now.plusSeconds(10)))

            // then
            opened shouldBe false
            stored("0a1b2c3d4e5f") shouldBe payment
        }
    }

    @Nested
    inner class Cancel {
        @Test
        fun `should store the cancellation`() = runTest {
            // given
            val payment = repository.openReceived(amount = 2_500, label = null)

            // when
            repository.cancel(payment.id)

            // then
            stored(payment.id).status shouldBe PaymentStatus.Cancelled
        }
    }

    @Nested
    inner class Confirm {
        @Test
        fun `should store the confirmation and its source`() = runTest {
            // given
            val payment = repository.openReceived(amount = 2_500, label = null)

            // when
            repository.confirm(payment.id, ConfirmationSource.Manual)

            // then
            stored(payment.id).confirmationSource shouldBe ConfirmationSource.Manual
        }
    }

    @Nested
    inner class ExpireOverdue {
        @Test
        fun `should expire a payment left while the app was in the background`() = runTest {
            // given
            val payment = repository.openReceived(amount = 2_500, label = null)
            now = now.plus(Duration.ofMinutes(5))

            // when
            repository.expireOverdue()

            // then
            stored(payment.id) shouldBe payment.copy(status = PaymentStatus.Expired, endedAt = payment.expiresAt)
        }
    }
}
