package com.eifi.core.payload

import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Base64
import java.util.zip.CRC32
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PaymentUrlCodecTest {
    private val now = Instant.parse("2026-10-27T10:00:00Z")

    private val codec = PaymentUrlCodec(baseUrl = BASE_URL, clock = Clock.fixed(now, ZoneOffset.UTC))

    @Nested
    inner class Encode {
        @Test
        fun `should put the version and the data in the fragment of the payment page URL`() {
            // given
            val request = request()

            // when
            val url = codec.encode(request)

            // then
            url shouldStartWith "$BASE_URL/p#v1."
        }

        @Test
        fun `should fit a typical request in a version 8 QR code with error correction Q`() {
            // given
            val request = request(
                displayName = "Boutique Mama Ngono",
                receivingMethods = mapOf(
                    Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"),
                    Operator.MtnMomo to ReceivingMethod.MerchantCode("730154"),
                ),
            )

            // when
            val url = codec.encode(request)

            // then
            url.encodeToByteArray().size shouldBeLessThanOrEqual QR_VERSION_8_Q_BYTES
        }
    }

    @Nested
    inner class Decode {
        @ParameterizedTest(name = "{0}")
        @MethodSource("com.eifi.core.payload.PaymentUrlCodecTest#roundTripRequests")
        fun `should decode the request that was encoded`(request: PaymentRequest) {
            // given
            val url = codec.encode(request)

            // when
            val result = codec.decode(url)

            // then
            result shouldBe PaymentUrlDecodingResult.Valid(request)
        }

        @Test
        fun `should reject a request that expires now`() {
            // given
            val url = codec.encode(request(expiresAt = now))

            // when
            val result = codec.decode(url)

            // then
            result shouldBe PaymentUrlDecodingResult.Expired(expiredAt = now)
        }

        @Test
        fun `should reject an unknown version`() {
            // given
            val url = codec.encode(request()).replace("#v1.", "#v2.")

            // when
            val result = codec.decode(url)

            // then
            result shouldBe PaymentUrlDecodingResult.UnknownVersion(version = "v2")
        }

        @Test
        fun `should reject the URL of another site`() {
            // given
            val url = codec.encode(request()).replace(BASE_URL, "https://pay.example.org")

            // when
            val result = codec.decode(url)

            // then
            result shouldBe PaymentUrlDecodingResult.Malformed(MalformedReason.NotAPaymentUrl)
        }

        @Test
        fun `should reject a fragment that is not base64url`() {
            // given
            val url = "$BASE_URL/p#v1.not+base64url"

            // when
            val result = codec.decode(url)

            // then
            result shouldBe PaymentUrlDecodingResult.Malformed(MalformedReason.InvalidEncoding)
        }

        @Test
        fun `should reject altered data`() {
            // given
            val url = codec.encode(request())
            val position = url.indexOf("#v1.") + 10
            val altered = url.replaceRange(position, position + 1, if (url[position] == 'A') "B" else "A")

            // when
            val result = codec.decode(altered)

            // then
            result shouldBe PaymentUrlDecodingResult.Malformed(MalformedReason.ChecksumMismatch)
        }

        @Test
        fun `should reject data that stops before its end`() {
            // given
            val body = body(request())
            val url = urlWithChecksum(body.copyOf(body.size - 3))

            // when
            val result = codec.decode(url)

            // then
            result shouldBe PaymentUrlDecodingResult.Malformed(MalformedReason.Truncated)
        }

        @Test
        fun `should reject an unknown operator`() {
            // given
            val body = body(request(displayName = "Shop"))
            body[FIRST_METHOD_HEADER_WITH_SHOP_NAME] = 0xF1.toByte()
            val url = urlWithChecksum(body)

            // when
            val result = codec.decode(url)

            // then
            result shouldBe PaymentUrlDecodingResult.Malformed(MalformedReason.InvalidContent)
        }

        @Test
        fun `should reject an unknown receiving method`() {
            // given
            val body = body(request(displayName = "Shop"))
            body[FIRST_METHOD_HEADER_WITH_SHOP_NAME] = 0x1F.toByte()
            val url = urlWithChecksum(body)

            // when
            val result = codec.decode(url)

            // then
            result shouldBe PaymentUrlDecodingResult.Malformed(MalformedReason.InvalidContent)
        }
    }

    /** The encoded bytes without their checksum. */
    private fun body(request: PaymentRequest): ByteArray = PayloadV1.write(request).let { it.copyOf(it.size - 4) }

    private fun urlWithChecksum(body: ByteArray): String {
        val crc = CRC32().apply { update(body) }.value
        val bytes = body + byteArrayOf((crc shr 24).toByte(), (crc shr 16).toByte(), (crc shr 8).toByte(), crc.toByte())
        return "$BASE_URL/p#v1.${Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)}"
    }

    private fun request(
        displayName: String = "Boutique",
        receivingMethods: Map<Operator, ReceivingMethod> = mapOf(
            Operator.OrangeMoney to ReceivingMethod.MerchantCode("123456"),
        ),
        expiresAt: Instant = now.plusSeconds(120),
    ) = PaymentRequest(
        id = "0a1b2c3d4e5f",
        displayName = displayName,
        amount = 2_500,
        receivingMethods = receivingMethods,
        expiresAt = expiresAt,
    )

    private companion object {
        const val BASE_URL = "https://pay.eifi.app"

        /** Byte mode capacity of a version 8 QR code with error correction Q, needed by the centred logo. */
        const val QR_VERSION_8_Q_BYTES = 108

        /** id 6 + expiry 4 + amount 2 + name length 1 + "Shop" 4 + method count 1. */
        const val FIRST_METHOD_HEADER_WITH_SHOP_NAME = 18

        private val expiresAt = Instant.parse("2026-10-27T10:02:00Z")

        @JvmStatic
        fun roundTripRequests() = listOf(
            named(
                "accented name",
                PaymentRequest(
                    id = "000000000001",
                    displayName = "Épicerie Ndé & Fils",
                    amount = 2_500,
                    receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
                    expiresAt = expiresAt,
                ),
            ),
            named(
                "single MTN operator",
                PaymentRequest(
                    id = "ffffffffffff",
                    displayName = "Kiosque",
                    amount = 100,
                    receivingMethods = mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("730154")),
                    expiresAt = expiresAt,
                ),
            ),
            named(
                "phone number instead of a merchant code",
                PaymentRequest(
                    id = "a1b2c3d4e5f6",
                    displayName = "Paul",
                    amount = 15_000,
                    receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.PhoneNumber("690123456")),
                    expiresAt = expiresAt,
                ),
            ),
            named(
                "both operators, phone number and merchant code",
                PaymentRequest(
                    id = "123456abcdef",
                    displayName = "Boutique Mama Ngono",
                    amount = 2_000_000,
                    receivingMethods = mapOf(
                        Operator.OrangeMoney to ReceivingMethod.PhoneNumber("690123456"),
                        Operator.MtnMomo to ReceivingMethod.MerchantCode("730154"),
                    ),
                    expiresAt = expiresAt,
                ),
            ),
            named(
                "merchant code with letters and leading zero",
                PaymentRequest(
                    id = "abcdefabcdef",
                    displayName = "Ma Boutique 😀",
                    amount = 1,
                    receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("0A-12")),
                    expiresAt = expiresAt,
                ),
            ),
        )

        private fun named(name: String, request: PaymentRequest): Arguments = Arguments.of(Named.of(name, request))
    }
}
