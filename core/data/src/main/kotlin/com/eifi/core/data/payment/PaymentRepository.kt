package com.eifi.core.data.payment

import kotlinx.coroutines.flow.Flow

/** Payments received and sent, stored on the phone (EIFI-102). */
interface PaymentRepository {
    /** A new PENDING received payment of [amount], or the one already pending for this amount, whatever its label. */
    suspend fun openReceived(amount: Long, label: String?): Payment

    fun getByIdAsFlow(id: String): Flow<Payment?>

    suspend fun cancel(id: String)

    suspend fun confirm(id: String, source: ConfirmationSource)

    /** Stores as EXPIRED the payments whose expiry has passed, e.g. when their countdown ends. */
    suspend fun expireOverdue()
}
