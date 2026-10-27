package com.eifi.core.data

import com.eifi.core.payload.Operator
import java.time.Instant

/**
 * A payment received (Receive mode) or sent (Pay mode).
 *
 * @property id the id of the payment.
 * @property direction received or sent, which sets the sign and the icon of the amount.
 * @property counterpart masked phone number of the payer, or name of the payee.
 * @property operator operator through which the money went.
 * @property amount amount in FCFA.
 * @property at when the payment was made.
 */
data class ActivityItem(
    val id: String,
    val direction: PaymentDirection,
    val counterpart: String,
    val operator: Operator,
    val amount: Long,
    val at: Instant,
)
