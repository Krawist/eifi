package com.eifi.feature.receive.payment

import com.eifi.core.data.payment.PaymentStatus

/**
 * A received payment waiting to be paid.
 *
 * @property amount amount in FCFA.
 * @property label optional note typed by the payee.
 * @property status status at the last tick, which already takes the expiry time into account.
 * @property remainingSeconds seconds left before the expiry, 0 once it has passed.
 */
internal data class PaymentUiState(
    val amount: Long,
    val label: String?,
    val status: PaymentStatus,
    val remainingSeconds: Long,
)
