package com.eifi.core.design.format

/** Narrow no-break space, thousands separator of amounts. */
internal const val THIN_SPACE: Char = ' '

private const val GROUP_SIZE = 3

/**
 * Formats an integer FCFA amount with a thin space as thousands separator: 2500 → "2 500".
 * The "FCFA" suffix is displayed separately, in a smaller style.
 */
fun formatFcfa(amount: Long): String {
    val digits = kotlin.math.abs(amount).toString()
    val grouped = digits.reversed().chunked(GROUP_SIZE).joinToString(THIN_SPACE.toString()).reversed()
    return if (amount < 0) "-$grouped" else grouped
}
