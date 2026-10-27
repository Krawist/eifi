package com.eifi.core.data

import androidx.datastore.core.DataStore
import com.eifi.core.payload.Operator
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** [PayingAccountRepository] backed by a typed DataStore holding the last [Operator]. */
internal class DataStorePayingAccountRepository @Inject constructor(
    private val lastOperatorStore: DataStore<Operator?>,
) : PayingAccountRepository {

    override val lastOperator: Flow<Operator?> = lastOperatorStore.data

    override suspend fun saveLastOperator(operator: Operator) {
        lastOperatorStore.updateData { operator }
    }
}
