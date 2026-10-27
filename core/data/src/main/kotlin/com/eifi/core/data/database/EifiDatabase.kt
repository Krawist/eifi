package com.eifi.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/** Local database of the pilot: the payments received and sent, then the SMS events. */
@Database(entities = [PaymentEntity::class], version = 1, exportSchema = true)
internal abstract class EifiDatabase : RoomDatabase() {

    abstract fun paymentDao(): PaymentDao
}
