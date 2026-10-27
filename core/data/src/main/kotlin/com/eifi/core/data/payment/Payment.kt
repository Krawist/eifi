package com.eifi.core.data.payment

import com.eifi.core.payload.Operator
import java.time.Instant

/**
 * A payment, received or sent ([type]), PENDING until it is confirmed, expires or is cancelled. Its lifecycle rules
 * are in [PaymentService].
 *
 * @property id the id of the payment URL ([com.eifi.core.payload.PaymentRequest.id]).
 * @property type received (Receive mode) or sent (Pay mode).
 * @property amount amount in FCFA.
 * @property label optional note typed by the payee, shown on the waiting screen.
 * @property createdAt when the payment was opened.
 * @property expiresAt when a PENDING payment stops waiting.
 * @property status status as stored; see [PaymentService.statusAt] for the status at a given instant.
 * @property confirmationSource how the payment was confirmed, `null` until it is CONFIRMED.
 * @property endedAt when the payment left PENDING.
 * @property counterpartyName the other party: the payee of a sent payment; `null` when unknown.
 * @property operator the operator the money goes through; `null` when unknown.
 * @property acceptedOperators for a received payment, the operators offered to the payer (EIFI-111); `null` for all
 * those of the receiving profile.
 */
data class Payment(
    val id: String,
    val type: TransactionType,
    val amount: Long,
    val label: String?,
    val createdAt: Instant,
    val expiresAt: Instant,
    val status: PaymentStatus = PaymentStatus.Pending,
    val confirmationSource: ConfirmationSource? = null,
    val endedAt: Instant? = null,
    val counterpartyName: String? = null,
    val operator: Operator? = null,
    val acceptedOperators: List<Operator>? = null,
)
