package com.eifi.core.payload

import java.time.Instant

/** Result of decoding a payment URL. Only [Valid] may lead to a payment. */
sealed interface PaymentUrlDecodingResult {
    /** @property request the payment to make, not expired yet. */
    data class Valid(val request: PaymentRequest) : PaymentUrlDecodingResult

    /** @property expiredAt when the request stopped being payable. */
    data class Expired(val expiredAt: Instant) : PaymentUrlDecodingResult

    /**
     * The URL comes from a newer (or unknown) version of Eifi: the payer should update the app.
     *
     * @property version the version found in the URL, e.g. "v2".
     */
    data class UnknownVersion(val version: String) : PaymentUrlDecodingResult

    /** @property reason why the URL could not be read. */
    data class Malformed(val reason: MalformedReason) : PaymentUrlDecodingResult
}
