package com.eifi.core.design.format

/** Espace fine insécable, séparateur des milliers des montants. */
const val THIN_SPACE: Char = ' '

/**
 * Formate un montant entier en FCFA avec une espace fine pour les milliers : 2500 → « 2 500 ».
 * Le suffixe « FCFA » est affiché à part, dans un style plus petit.
 */
fun formatFcfa(amount: Long): String {
    val digits = kotlin.math.abs(amount).toString()
    val grouped = digits.reversed().chunked(3).joinToString(THIN_SPACE.toString()).reversed()
    return if (amount < 0) "-$grouped" else grouped
}
