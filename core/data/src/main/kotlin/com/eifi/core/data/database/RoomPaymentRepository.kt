package com.eifi.core.data.database

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [PaymentRepository] on Room. The overdue payments are expired before a new one is opened, so that the expiry
 * also applies to the payments left while the app was in the background.
 */
internal class RoomPaymentRepository @Inject constructor(
    private val database: EifiDatabase,
    private val paymentService: PaymentService,
    private val clock: Clock,
) : PaymentRepository {

    private val dao = database.paymentDao()

    override suspend fun openReceived(amount: Long, label: String?): Payment = inTransaction {
        val now = clock.instant()
        dao.expireOverdue(now.toEpochMilli())
        dao.getPendingReceivedByAmount(amount)?.toPayment()
            ?: paymentService.newReceived(amount = amount, label = label, at = now).also {
                dao.insert(PaymentEntity.of(it))
            }
    }

    override fun getByIdAsFlow(id: String): Flow<Payment?> = dao.getByIdAsFlow(id).map { it?.toPayment() }

    override suspend fun cancel(id: String) = transition(id) {
        paymentService.cancel(payment = it, at = clock.instant())
    }

    override suspend fun confirm(id: String, source: ConfirmationSource) =
        transition(id) { paymentService.confirm(payment = it, source = source, at = clock.instant()) }

    override suspend fun expireOverdue() = dao.expireOverdue(clock.instant().toEpochMilli())

    private suspend fun transition(id: String, change: (Payment) -> Payment) = inTransaction {
        val payment = checkNotNull(dao.getById(id)?.toPayment()) { "No payment $id" }
        val changed = change(payment)
        if (changed != payment) dao.update(PaymentEntity.of(changed))
    }

    /** One write transaction: two "Encaisser" cannot both open a payment for the same amount. */
    private suspend fun <R> inTransaction(block: suspend () -> R): R =
        database.useWriterConnection { connection -> connection.immediateTransaction { block() } }
}
