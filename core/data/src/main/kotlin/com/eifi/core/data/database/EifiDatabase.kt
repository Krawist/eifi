package com.eifi.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Local database of the pilot: the payments received and sent, the USSD codes, then the SMS events. Version 2 adds
 * the counterparty and the operator of a payment (EIFI-205), version 3 the operators accepted by a received payment
 * (EIFI-111) and the USSD codes (EIFI-015).
 */
@Database(
    entities = [PaymentEntity::class, UssdConfigEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
@TypeConverters(OperatorListConverter::class)
internal abstract class EifiDatabase : RoomDatabase() {

    abstract fun paymentDao(): PaymentDao

    abstract fun ussdConfigDao(): UssdConfigDao
}
