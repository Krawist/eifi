package com.eifi.core.data.payment

import com.eifi.core.data.ReceivingProfile
import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.ReceivingMethod
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class PaymentServiceTest {
    private val service = PaymentService()

    private val createdAt = Instant.parse("2026-10-27T10:00:00Z")
    private val expiresAt = Instant.parse("2026-10-27T10:02:00Z")

    private fun payment(status: PaymentStatus = PaymentStatus.Pending) = Payment(
        id = "0a1b2c3d4e5f",
        type = TransactionType.Received,
        amount = 2_500,
        label = null,
        createdAt = createdAt,
        expiresAt = expiresAt,
        status = status,
    )

    @Nested
    inner class NewReceived {
        @Test
        fun `should open a pending received payment of 2 minutes`() {
            // when
            val payment = service.newReceived(amount = 2_500, label = "Pain", at = createdAt)

            // then
            payment shouldBe Payment(
                id = payment.id,
                type = TransactionType.Received,
                amount = 2_500,
                label = "Pain",
                createdAt = createdAt,
                expiresAt = createdAt.plusSeconds(120),
            )
        }

        @Test
        fun `should give a payment URL id`() {
            // when
            val payment = service.newReceived(amount = 2_500, label = null, at = createdAt)

            // then
            payment.id shouldMatch "[0-9a-f]{12}"
        }

        @Test
        fun `should reject an amount that is not positive`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                service.newReceived(amount = 0, label = null, at = createdAt)
            } shouldHaveMessage "The amount must be positive"
        }
    }

    @Nested
    inner class NewSent {
        @Test
        fun `should open a pending sent payment with the id and expiry of the request`() {
            // given
            val request = PaymentRequest(
                id = "0a1b2c3d4e5f",
                displayName = "Kiosque Ndé",
                amount = 2_500,
                receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
                expiresAt = expiresAt,
            )

            // when
            val payment = service.newSent(request = request, at = createdAt)

            // then
            payment shouldBe Payment(
                id = "0a1b2c3d4e5f",
                type = TransactionType.Sent,
                amount = 2_500,
                label = null,
                createdAt = createdAt,
                expiresAt = expiresAt,
            )
        }
    }

    @Nested
    inner class ToPaymentRequest {
        @Test
        fun `should carry the payment and the receiving profile`() {
            // given
            val profile = ReceivingProfile(
                displayName = "Kiosque Ndé",
                receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
            )

            // when
            val request = service.toPaymentRequest(payment = payment(), profile = profile)

            // then
            request shouldBe PaymentRequest(
                id = "0a1b2c3d4e5f",
                displayName = "Kiosque Ndé",
                amount = 2_500,
                receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
                expiresAt = expiresAt,
            )
        }

        @Test
        fun `should round the expiry down to the second`() {
            // given
            val profile = ReceivingProfile(
                displayName = "Kiosque Ndé",
                receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
            )
            val payment = payment().copy(expiresAt = expiresAt.plusMillis(999))

            // when
            val request = service.toPaymentRequest(payment = payment, profile = profile)

            // then
            request.expiresAt shouldBe expiresAt
        }
    }

    @Nested
    inner class StatusAt {
        @Test
        fun `should be pending before the expiry`() {
            // when
            val status = service.statusAt(payment = payment(), now = expiresAt.minusSeconds(1))

            // then
            status shouldBe PaymentStatus.Pending
        }

        @Test
        fun `should be expired from the expiry on, even before it is stored`() {
            // when
            val status = service.statusAt(payment = payment(), now = expiresAt)

            // then
            status shouldBe PaymentStatus.Expired
        }
    }

    @Nested
    inner class Confirm {
        @ParameterizedTest
        @EnumSource(ConfirmationSource::class)
        fun `should confirm a pending payment`(source: ConfirmationSource) {
            // given
            val at = createdAt.plusSeconds(30)

            // when
            val confirmed = service.confirm(payment = payment(), source = source, at = at)

            // then
            confirmed shouldBe
                payment().copy(status = PaymentStatus.Confirmed, confirmationSource = source, endedAt = at)
        }

        @Test
        fun `should confirm an expired payment by an SMS arriving less than 10 minutes later`() {
            // given
            val at = expiresAt.plusSeconds(9 * 60)

            // when
            val confirmed = service.confirm(
                payment = payment(PaymentStatus.Expired),
                source = ConfirmationSource.Sms,
                at = at,
            )

            // then
            confirmed.status shouldBe PaymentStatus.Confirmed
        }

        @Test
        fun `should refuse an SMS arriving 10 minutes after the expiry`() {
            // when & then
            shouldThrow<IllegalStateException> {
                service.confirm(
                    payment = payment(PaymentStatus.Expired),
                    source = ConfirmationSource.Sms,
                    at = expiresAt.plusSeconds(
                        10 * 60,
                    ),
                )
            } shouldHaveMessage "A Expired payment cannot be confirmed by Sms"
        }

        @Test
        fun `should refuse a manual confirmation of an expired payment`() {
            // when & then
            shouldThrow<IllegalStateException> {
                service.confirm(payment = payment(), source = ConfirmationSource.Manual, at = expiresAt)
            } shouldHaveMessage "A Expired payment cannot be confirmed by Manual"
        }

        @Test
        fun `should refuse to confirm a cancelled payment`() {
            // when & then
            shouldThrow<IllegalStateException> {
                service.confirm(
                    payment = payment(PaymentStatus.Cancelled),
                    source = ConfirmationSource.Sms,
                    at = createdAt.plusSeconds(30),
                )
            } shouldHaveMessage "A Cancelled payment cannot be confirmed by Sms"
        }

        @Test
        fun `should keep the first confirmation when confirmed again`() {
            // given
            val confirmed = service.confirm(
                payment = payment(),
                source = ConfirmationSource.Sms,
                at = createdAt.plusSeconds(30),
            )

            // when
            val again = service.confirm(
                payment = confirmed,
                source = ConfirmationSource.Manual,
                at = createdAt.plusSeconds(60),
            )

            // then
            again shouldBe confirmed
        }
    }

    @Nested
    inner class Cancel {
        @Test
        fun `should cancel a pending payment`() {
            // given
            val at = createdAt.plusSeconds(30)

            // when
            val cancelled = service.cancel(payment = payment(), at = at)

            // then
            cancelled shouldBe payment().copy(status = PaymentStatus.Cancelled, endedAt = at)
        }

        @Test
        fun `should change nothing when cancelled again`() {
            // given
            val cancelled = service.cancel(payment = payment(), at = createdAt.plusSeconds(30))

            // when
            val again = service.cancel(payment = cancelled, at = createdAt.plusSeconds(60))

            // then
            again shouldBe cancelled
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus::class, names = ["Confirmed", "Expired"])
        fun `should refuse to cancel a confirmed or expired payment`(status: PaymentStatus) {
            // when & then
            shouldThrow<IllegalStateException> {
                service.cancel(payment = payment(status), at = createdAt.plusSeconds(30))
            } shouldHaveMessage "A $status payment cannot be cancelled"
        }
    }

    @Nested
    inner class Expire {
        @Test
        fun `should end at the expiry, not when it is noticed`() {
            // when
            val expired = service.expire(payment = payment(), at = expiresAt.plusSeconds(300))

            // then
            expired shouldBe payment().copy(status = PaymentStatus.Expired, endedAt = expiresAt)
        }

        @Test
        fun `should refuse to expire before the expiry`() {
            // when & then
            shouldThrow<IllegalStateException> {
                service.expire(payment = payment(), at = expiresAt.minusSeconds(1))
            } shouldHaveMessage "A Pending payment cannot expire at 2026-10-27T10:01:59Z"
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus::class, names = ["Confirmed", "Cancelled"])
        fun `should keep a confirmed or cancelled payment`(status: PaymentStatus) {
            // when & then
            shouldThrow<IllegalStateException> {
                service.expire(payment = payment(status), at = expiresAt.plusSeconds(1))
            } shouldHaveMessage "A $status payment cannot expire at 2026-10-27T10:02:01Z"
        }
    }
}
