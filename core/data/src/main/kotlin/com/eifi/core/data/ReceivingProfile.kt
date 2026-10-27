package com.eifi.core.data

import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.ReceivingMethod
import kotlinx.serialization.Serializable

/**
 * Receiving profile of the Receive mode: the name shown to payers and, for each operator, a merchant code or a
 * phone number. Stored as JSON (see [JsonSerializer]): a new property needs a default value.
 */
@Serializable
data class ReceivingProfile(val displayName: String, val receivingMethods: Map<Operator, ReceivingMethod>) {
    init {
        require(displayName.isNotBlank() && displayName.length <= PaymentRequest.MAX_DISPLAY_NAME_LENGTH) {
            "The display name must have 1 to ${PaymentRequest.MAX_DISPLAY_NAME_LENGTH} characters"
        }
        require(receivingMethods.isNotEmpty()) { "At least one operator is required" }
    }
}
