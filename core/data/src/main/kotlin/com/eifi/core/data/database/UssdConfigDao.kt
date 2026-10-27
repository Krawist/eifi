package com.eifi.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface UssdConfigDao {
    @Insert
    suspend fun insertAll(configs: List<UssdConfigEntity>)

    @Query("SELECT * FROM ussd_config")
    fun getAllAsFlow(): Flow<List<UssdConfigEntity>>

    @Query("SELECT COUNT(*) FROM ussd_config")
    suspend fun count(): Int
}
