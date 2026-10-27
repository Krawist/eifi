package com.eifi.core.payload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.throwable.shouldHaveMessage
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PaymentRequestTest {
    @Nested
    inner class Init {
        @Test
        fun `should reject a display name longer than 30 characters`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                request(displayName = "B".repeat(31))
            } shouldHaveMessage "The display name must have 1 to 30 characters"
        }

        @Test
        fun `should accept 30 accented characters`() {
            // when & then
            request(displayName = "é".repeat(30))
        }

        @Test
        fun `should reject an amount that is not positive`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                request(amount = 0)
            } shouldHaveMessage "The amount must be positive"
        }

        @Test
        fun `should reject a request without receiving method`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                request(receivingMethods = emptyMap())
            } shouldHaveMessage "At least one receiving method is required"
        }

        @Test
        fun `should reject an expiry with fractions of a second`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                request(expiresAt = Instant.parse("2026-10-27T10:02:00.500Z"))
            } shouldHaveMessage "The expiry must be in whole seconds"
        }

        @Test
        fun `should reject an id that is not 12 lowercase hexadecimal characters`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                request(id = "0A1B2C3D4E5F")
            } shouldHaveMessage "The id must be 12 lowercase hexadecimal characters"
        }
    }

    @Nested
    inner class NewId {
        @Test
        fun `should generate 12 lowercase hexadecimal characters`() {
            // when
            val id = PaymentRequest.newId()

            // then
            id shouldMatch "[0-9a-f]{12}"
        }
    }

    private fun request(
        id: String = "0a1b2c3d4e5f",
        displayName: String = "Boutique",
        amount: Long = 2_500,
        receivingMethods: Map<Operator, ReceivingMethod> = mapOf(
            Operator.OrangeMoney to ReceivingMethod.MerchantCode("123456"),
        ),
        expiresAt: Instant = Instant.parse("2026-10-27T10:02:00Z"),
    ) = PaymentRequest(
        id = id,
        displayName = displayName,
        amount = amount,
        receivingMethods = receivingMethods,
        expiresAt = expiresAt,
    )
}
