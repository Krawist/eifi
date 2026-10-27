package com.eifi.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PaymentDao {
    @Insert
    suspend fun insert(payment: PaymentEntity)

    @Update
    suspend fun update(payment: PaymentEntity)

    @Query("SELECT * FROM payment WHERE id = :id")
    suspend fun getById(id: String): PaymentEntity?

    @Query("SELECT * FROM payment WHERE id = :id")
    fun getByIdAsFlow(id: String): Flow<PaymentEntity?>

    @Query("SELECT * FROM payment WHERE type = 'Received' AND status = 'Pending' AND amount = :amount LIMIT 1")
    suspend fun getPendingReceivedByAmount(amount: Long): PaymentEntity?

    /** Every payment, latest first. */
    @Query("SELECT * FROM payment ORDER BY createdAt DESC")
    fun getAllAsFlow(): Flow<List<PaymentEntity>>

    /** The payments sent and the payments received and confirmed, with their operator, latest first. */
    @Query(
        "SELECT * FROM payment WHERE operator IS NOT NULL " +
            "AND (type = 'Sent' OR status = 'Confirmed') ORDER BY createdAt DESC LIMIT :limit",
    )
    fun getRecentActivityAsFlow(limit: Int): Flow<List<PaymentEntity>>

    @Query("SELECT COUNT(*) FROM payment")
    suspend fun count(): Int

    /**
     * Stores as EXPIRED the PENDING received payments whose expiry has passed, ended at their expiry. A sent payment
     * stays PENDING: its request was sent, whatever the expiry of the payee's code.
     */
    @Query(
        "UPDATE payment SET status = 'Expired', endedAt = expiresAt " +
            "WHERE type = 'Received' AND status = 'Pending' AND expiresAt <= :now",
    )
    suspend fun expireOverdue(now: Long)
}
