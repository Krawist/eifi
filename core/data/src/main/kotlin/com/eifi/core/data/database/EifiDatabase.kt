package com.eifi.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Local database of the pilot: the payments received and sent, then the SMS events. Version 2 adds the counterparty
 * and the operator of a payment (EIFI-205).
 */
@Database(
    entities = [PaymentEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
internal abstract class EifiDatabase : RoomDatabase() {

    abstract fun paymentDao(): PaymentDao
}
