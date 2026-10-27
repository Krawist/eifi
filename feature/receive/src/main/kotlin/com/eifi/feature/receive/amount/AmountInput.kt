package com.eifi.feature.receive.amount

/** Typing an amount in FCFA on the keypad: an integer from [MIN] to [MAX]. */
internal object AmountInput {
    const val MIN = 100L
    const val MAX = 2_000_000L

    private const val DIGIT_BASE = 10L

    /** [amount] followed by [digits] ("0" to "9" or "000"), unchanged when it would go past [MAX]. */
    fun append(amount: Long, digits: String): Long =
        if (exceedsMax(amount, digits)) amount else appended(amount, digits)

    /** Whether typing [digits] after [amount] would go past [MAX]. */
    fun exceedsMax(amount: Long, digits: String): Boolean = appended(amount, digits) > MAX

    /** [amount] without its last digit. */
    fun delete(amount: Long): Long = amount / DIGIT_BASE

    fun isCollectable(amount: Long): Boolean = amount in MIN..MAX

    private fun appended(amount: Long, digits: String): Long =
        digits.fold(amount) { value, digit -> value * DIGIT_BASE + digit.digitToInt() }
}
