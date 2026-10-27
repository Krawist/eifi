package com.eifi.core.data

import com.eifi.core.payload.Operator
import java.time.Instant

/**
 * A payment received (Receive mode) or sent (Pay mode).
 *
 * @property counterpart masked phone number of the payer, or name of the payee.
 */
data class ActivityItem(
    val id: String,
    val direction: PaymentDirection,
    val counterpart: String,
    val operator: Operator,
    val amount: Long,
    val at: Instant,
)
