package com.eifi.core.payload

import java.time.Instant

/** Result of decoding a payment URL. Only [Valid] may lead to a payment. */
sealed interface PaymentUrlDecodingResult {
    data class Valid(val request: PaymentRequest) : PaymentUrlDecodingResult

    data class Expired(val expiredAt: Instant) : PaymentUrlDecodingResult

    /** The URL comes from a newer (or unknown) version of Eifi: the payer should update the app. */
    data class UnknownVersion(val version: String) : PaymentUrlDecodingResult

    data class Malformed(val reason: MalformedReason) : PaymentUrlDecodingResult
}
