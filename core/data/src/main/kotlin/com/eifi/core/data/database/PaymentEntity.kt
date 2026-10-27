package com.eifi.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import java.time.Instant

/**
 * Row of [Payment]; instants in epoch milliseconds, enums by name. Each column holds the [Payment] property of the
 * same name.
 *
 * @property id primary key, [Payment.id].
 * @property type [Payment.type].
 * @property amount [Payment.amount], in FCFA.
 * @property label [Payment.label].
 * @property createdAt [Payment.createdAt].
 * @property expiresAt [Payment.expiresAt].
 * @property status [Payment.status].
 * @property confirmationSource [Payment.confirmationSource].
 * @property endedAt [Payment.endedAt].
 */
@Entity(tableName = "payment", indices = [Index(value = ["type", "status", "amount"])])
internal data class PaymentEntity(
    @PrimaryKey val id: String,
    val type: TransactionType,
    val amount: Long,
    val label: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val status: PaymentStatus,
    val confirmationSource: ConfirmationSource?,
    val endedAt: Long?,
) {
    fun toPayment() = Payment(
        id = id,
        type = type,
        amount = amount,
        label = label,
        createdAt = Instant.ofEpochMilli(createdAt),
        expiresAt = Instant.ofEpochMilli(expiresAt),
        status = status,
        confirmationSource = confirmationSource,
        endedAt = endedAt?.let(Instant::ofEpochMilli),
    )

    companion object {
        fun of(payment: Payment) = PaymentEntity(
            id = payment.id,
            type = payment.type,
            amount = payment.amount,
            label = payment.label,
            createdAt = payment.createdAt.toEpochMilli(),
            expiresAt = payment.expiresAt.toEpochMilli(),
            status = payment.status,
            confirmationSource = payment.confirmationSource,
            endedAt = payment.endedAt?.toEpochMilli(),
        )
    }
}
