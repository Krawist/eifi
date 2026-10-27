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

    /** Stores as EXPIRED the PENDING payments whose expiry has passed, ended at their expiry. */
    @Query(
        "UPDATE payment SET status = 'Expired', endedAt = expiresAt " +
            "WHERE status = 'Pending' AND expiresAt <= :now",
    )
    suspend fun expireOverdue(now: Long)
}
