package com.eifi.core.payload

import java.security.SecureRandom
import java.time.temporal.ChronoUnit

/** Rules of a [PaymentRequest]: its id and what the payment URL can carry. */
object PaymentRequestService {
    const val MAX_DISPLAY_NAME_LENGTH = 30
    internal const val ID_BYTES = 6
    private val ID_PATTERN = Regex("[0-9a-f]{${ID_BYTES * 2}}")

    /** Returns a new random [PaymentRequest.id]. */
    fun newId(random: SecureRandom = SecureRandom()): String =
        ByteArray(ID_BYTES).also(random::nextBytes).joinToString(separator = "") { "%02x".format(it) }

    /** Whether [displayName] can be shown to payers: 1 to [MAX_DISPLAY_NAME_LENGTH] characters, not blank. */
    fun isValidDisplayName(displayName: String): Boolean =
        displayName.isNotBlank() && displayName.codePointCount(0, displayName.length) <= MAX_DISPLAY_NAME_LENGTH

    /** @throws IllegalArgumentException when [request] cannot be carried by a payment URL. */
    fun validate(request: PaymentRequest) {
        require(ID_PATTERN.matches(request.id)) { "The id must be 12 lowercase hexadecimal characters" }
        require(isValidDisplayName(request.displayName)) {
            "The display name must have 1 to $MAX_DISPLAY_NAME_LENGTH characters"
        }
        require(request.amount > 0) { "The amount must be positive" }
        require(request.receivingMethods.isNotEmpty()) { "At least one receiving method is required" }
        request.receivingMethods.values.forEach(ReceivingMethodService::validate)
        require(request.expiresAt == request.expiresAt.truncatedTo(ChronoUnit.SECONDS)) {
            "The expiry must be in whole seconds"
        }
    }
}
