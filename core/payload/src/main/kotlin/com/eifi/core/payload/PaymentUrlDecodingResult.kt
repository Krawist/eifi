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

enum class MalformedReason {
    /** Not a URL of the Eifi payment page. */
    NotAPaymentUrl,

    /** The fragment is not `v{n}.{base64url}`. */
    InvalidEncoding,

    /** The data stops before its end. */
    Truncated,

    /** The data was altered (bad scan, typing error or tampering). */
    ChecksumMismatch,

    /** The data is well formed but its content is not valid (unknown operator, name too long...). */
    InvalidContent,
}
