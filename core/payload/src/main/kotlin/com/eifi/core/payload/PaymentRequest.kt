package com.eifi.core.payload

import java.time.Instant

/**
 * Payment session created by the payee and carried by the payment URL. Validated by [PaymentRequestService].
 *
 * @property id random identifier, 12 lowercase hexadecimal characters (see [PaymentRequestService.newId]).
 * @property displayName name of the payee shown to the payer, 30 characters at most.
 * @property amount amount to pay, in FCFA.
 * @property receivingMethods for each operator the payee accepts, where the money must be sent.
 * @property expiresAt whole seconds only, as the URL does not carry fractions of a second.
 */
data class PaymentRequest(
    val id: String,
    val displayName: String,
    val amount: Long,
    val receivingMethods: Map<Operator, ReceivingMethod>,
    val expiresAt: Instant,
)
