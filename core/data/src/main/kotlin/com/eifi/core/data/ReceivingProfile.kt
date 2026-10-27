package com.eifi.core.data

import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.ReceivingMethod
import kotlinx.serialization.Serializable

/**
 * Receiving profile of the Receive mode: the name shown to payers and, for each operator, a merchant code or a
 * phone number. Validated by [ReceivingProfileService].
 * Stored as JSON (see [JsonSerializer]): a new property needs a default value.
 *
 * @property displayName name shown to payers, carried by the payment URL ([PaymentRequest.displayName]).
 * @property receivingMethods for each accepted operator, where the money arrives.
 */
@Serializable
data class ReceivingProfile(val displayName: String, val receivingMethods: Map<Operator, ReceivingMethod>)
