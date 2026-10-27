package com.eifi.core.data.seed

import com.eifi.core.data.database.EifiDatabase
import com.eifi.core.data.database.PaymentEntity
import com.eifi.core.data.database.UssdConfigEntity
import java.time.Clock
import javax.inject.Inject

/**
 * Preloads the empty tables at each start (EIFI-015): the USSD codes, and sample payments for the pilot. A table
 * that already has rows is left as it is.
 */
class DatabaseSeeder @Inject internal constructor(private val database: EifiDatabase, private val clock: Clock) {
    suspend fun seed() {
        val ussdConfigDao = database.ussdConfigDao()
        if (ussdConfigDao.count() == 0) ussdConfigDao.insertAll(SeedData.ussdConfigs.map(UssdConfigEntity::of))
        val paymentDao = database.paymentDao()
        if (paymentDao.count() ==
            0
        ) {
            SeedData.payments(clock.instant()).forEach { paymentDao.insert(PaymentEntity.of(it)) }
        }
    }
}
