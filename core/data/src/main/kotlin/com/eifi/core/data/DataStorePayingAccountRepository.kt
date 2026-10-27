package com.eifi.core.data

import androidx.datastore.core.DataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** [PayingAccountRepository] backed by a typed DataStore holding the [PayingAccountChoice]. */
internal class DataStorePayingAccountRepository @Inject constructor(
    private val choiceStore: DataStore<PayingAccountChoice?>,
) : PayingAccountRepository {

    override val choice: Flow<PayingAccountChoice?> = choiceStore.data

    override suspend fun saveChoice(choice: PayingAccountChoice) {
        choiceStore.updateData { choice }
    }
}
