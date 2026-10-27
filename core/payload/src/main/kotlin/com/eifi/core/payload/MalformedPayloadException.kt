package com.eifi.core.payload

/** Internal signal of [PayloadV1.read], turned into [PaymentUrlDecodingResult.Malformed] by [PaymentUrlCodec]. */
internal class MalformedPayloadException(val reason: MalformedReason, cause: Throwable? = null) :
    Exception(reason.name, cause)
