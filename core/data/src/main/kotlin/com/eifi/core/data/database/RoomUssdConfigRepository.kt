package com.eifi.core.data.database

import com.eifi.core.ussd.OperatorUssdConfig
import com.eifi.core.ussd.UssdConfigRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [UssdConfigRepository] on Room, preloaded by `DatabaseSeeder` until the codes come from the backend. */
internal class RoomUssdConfigRepository @Inject constructor(database: EifiDatabase) : UssdConfigRepository {
    override val configs: Flow<List<OperatorUssdConfig>> =
        database.ussdConfigDao().getAllAsFlow().map { rows -> rows.map { it.toConfig() } }
}
