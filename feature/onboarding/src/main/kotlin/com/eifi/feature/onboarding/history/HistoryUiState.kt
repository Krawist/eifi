package com.eifi.feature.onboarding.history

import com.eifi.core.data.payment.TransactionType
import com.eifi.core.payload.Operator
import java.time.Instant
import java.time.LocalDate

/**
 * The payments received and sent, grouped by day, latest first.
 *
 * @property days the days with payments, latest first; empty when there is none.
 */
internal data class HistoryUiState(val days: List<Day> = emptyList()) {
    /**
     * The payments of one day.
     *
     * @property date the day, in the phone's time zone.
     * @property entries its payments, latest first.
     */
    data class Day(val date: LocalDate, val entries: List<Entry>)

    /**
     * One payment.
     *
     * @property id the id of the payment, which opens the receipt of a sent payment.
     * @property type received or sent.
     * @property counterpart masked number of the payer or name of the payee, `null` when unknown.
     * @property label the note of a received payment, `null` without one.
     * @property operator the operator of the payment, `null` while unknown (received and not confirmed).
     * @property amount amount in FCFA.
     * @property at when the payment was opened or sent.
     * @property status the status at the time the history was read.
     */
    data class Entry(
        val id: String,
        val type: TransactionType,
        val counterpart: String?,
        val label: String?,
        val operator: Operator?,
        val amount: Long,
        val at: Instant,
        val status: HistoryStatus,
    )
}
