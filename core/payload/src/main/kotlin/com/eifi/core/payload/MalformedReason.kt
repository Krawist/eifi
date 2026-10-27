package com.eifi.core.payload

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
