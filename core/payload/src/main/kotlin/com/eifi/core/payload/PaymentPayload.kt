package com.eifi.core.payload

/**
 * Payment URL format: `https://pay.<domain>/p#v1.{base64url data}`.
 *
 * TODO(EIFI-020): full model (identifier, display name ≤ 30 characters, amount,
 * receiving method per operator, expiry, checksum), encoder and decoder.
 */
object PaymentPayload {
    const val VERSION_PREFIX = "v1."
    const val MAX_DISPLAY_NAME_LENGTH = 30
}
