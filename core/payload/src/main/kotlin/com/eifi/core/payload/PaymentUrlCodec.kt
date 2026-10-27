package com.eifi.core.payload

import java.time.Clock
import java.util.Base64

/**
 * Encodes and decodes payment URLs: `{baseUrl}/p#v1.{data}`, data being [PayloadV1] in base64url without padding.
 * The whole session is in the fragment, which browsers never send to the server.
 *
 * @param baseUrl scheme and host of the payment page, without trailing slash (domain chosen in EIFI-021).
 */
class PaymentUrlCodec(baseUrl: String, private val clock: Clock) {
    init {
        require(baseUrl.startsWith("https://") && !baseUrl.endsWith("/")) {
            "The base URL must use https and have no trailing slash"
        }
    }

    private val prefix = "$baseUrl/p#"

    fun encode(request: PaymentRequest): String =
        "$prefix$VERSION.${Base64.getUrlEncoder().withoutPadding().encodeToString(PayloadV1.write(request))}"

    /** Decodes [url]; an expired request is never returned as [PaymentUrlDecodingResult.Valid]. */
    fun decode(url: String): PaymentUrlDecodingResult {
        val fragment = FRAGMENT.matchEntire(url.removePrefix(prefix))
        return when {
            !url.startsWith(prefix) -> PaymentUrlDecodingResult.Malformed(MalformedReason.NotAPaymentUrl)
            fragment == null -> PaymentUrlDecodingResult.Malformed(MalformedReason.InvalidEncoding)
            fragment.groupValues[1] != VERSION -> PaymentUrlDecodingResult.UnknownVersion(fragment.groupValues[1])
            else -> decodeV1(fragment.groupValues[2])
        }
    }

    private fun decodeV1(data: String): PaymentUrlDecodingResult {
        val bytes = try {
            Base64.getUrlDecoder().decode(data)
        } catch (_: IllegalArgumentException) {
            return PaymentUrlDecodingResult.Malformed(MalformedReason.InvalidEncoding)
        }
        return try {
            val request = PayloadV1.read(bytes)
            if (clock.instant().isBefore(request.expiresAt)) {
                PaymentUrlDecodingResult.Valid(request)
            } else {
                PaymentUrlDecodingResult.Expired(request.expiresAt)
            }
        } catch (e: MalformedPayloadException) {
            PaymentUrlDecodingResult.Malformed(e.reason)
        }
    }

    private companion object {
        const val VERSION = "v1"

        /** Base64url without padding only, so that the decoder never meets an unexpected character. */
        val FRAGMENT = Regex("(v\\d+)\\.([A-Za-z0-9_-]*)")
    }
}
