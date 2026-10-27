package com.eifi.feature.pay.receipt

import com.eifi.core.payload.Operator
import java.time.Instant

/**
 * The payer's receipt of a sent payment.
 *
 * @property payeeName name of the payee, `null` when unknown.
 * @property amount amount paid, in FCFA.
 * @property operator the operator of the payer's account, `null` when unknown.
 * @property sentAt when the USSD code was launched.
 * @property debitConfirmed whether the operator's debit SMS confirmed the payment (EIFI-206); until then the receipt
 * only says the request was sent.
 */
internal data class ReceiptUiState(
    val payeeName: String?,
    val amount: Long,
    val operator: Operator?,
    val sentAt: Instant,
    val debitConfirmed: Boolean,
)
