package com.eifi.core.data.payment

import com.eifi.core.payload.Operator
import kotlinx.coroutines.flow.Flow

/** Payments received and sent, stored on the phone (EIFI-102). */
interface PaymentRepository {
    /**
     * A new PENDING received payment of [amount] (see [PaymentService.newReceived]); the one still pending for this
     * amount is cancelled, so that only one payment per amount is PENDING (EIFI-112).
     */
    suspend fun openReceived(amount: Long, label: String?, acceptedOperators: List<Operator>?): Payment

    /**
     * Stores the sent [payment] (see [PaymentService.newSent]) before its USSD code is launched; `false` when a payment
     * with its id is already stored, so that a session is never paid twice.
     */
    suspend fun openSent(payment: Payment): Boolean

    fun getByIdAsFlow(id: String): Flow<Payment?>

    suspend fun cancel(id: String)

    suspend fun confirm(id: String, source: ConfirmationSource)

    /** Stores as EXPIRED the payments whose expiry has passed, e.g. when their countdown ends. */
    suspend fun expireOverdue()
}
