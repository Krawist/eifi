package com.eifi.core.payload

import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Payment session created by the payee and carried by the payment URL.
 *
 * @property id random identifier, 12 lowercase hexadecimal characters (see [newId]).
 * @property expiresAt whole seconds only, as the URL does not carry fractions of a second.
 */
data class PaymentRequest(
    val id: String,
    val displayName: String,
    val amount: Long,
    val receivingMethods: Map<Operator, ReceivingMethod>,
    val expiresAt: Instant,
) {
    init {
        require(ID_PATTERN.matches(id)) { "The id must be 12 lowercase hexadecimal characters" }
        require(
            displayName.isNotBlank() && displayName.codePointCount(0, displayName.length) <= MAX_DISPLAY_NAME_LENGTH,
        ) {
            "The display name must have 1 to $MAX_DISPLAY_NAME_LENGTH characters"
        }
        require(amount > 0) { "The amount must be positive" }
        require(receivingMethods.isNotEmpty()) { "At least one receiving method is required" }
        require(expiresAt == expiresAt.truncatedTo(ChronoUnit.SECONDS)) { "The expiry must be in whole seconds" }
    }

    companion object {
        const val MAX_DISPLAY_NAME_LENGTH = 30
        internal const val ID_BYTES = 6
        private val ID_PATTERN = Regex("[0-9a-f]{${ID_BYTES * 2}}")

        /** Returns a new random [id]. */
        fun newId(random: SecureRandom = SecureRandom()): String =
            ByteArray(ID_BYTES).also(random::nextBytes).joinToString(separator = "") { "%02x".format(it) }
    }
}
