package com.eifi.feature.receive.payment

import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.nfc.NfcStatus

/**
 * A received payment waiting to be paid.
 *
 * @property amount amount in FCFA.
 * @property label optional note typed by the payee.
 * @property status status at the last tick, which already takes the expiry time into account.
 * @property remainingSeconds seconds left before the expiry, 0 once it has passed.
 * @property url payment URL shown as a QR code and emitted over NFC, `null` while the receiving profile loads.
 * @property nfcStatus whether NFC can carry the URL, `null` while it loads.
 * @property restartedPaymentId set once an expired payment was opened again with the same amount, so that the
 *   screen shows the new one.
 */
internal data class PaymentUiState(
    val amount: Long,
    val label: String?,
    val status: PaymentStatus,
    val remainingSeconds: Long,
    val url: String? = null,
    val nfcStatus: NfcStatus? = null,
    val restartedPaymentId: String? = null,
)
