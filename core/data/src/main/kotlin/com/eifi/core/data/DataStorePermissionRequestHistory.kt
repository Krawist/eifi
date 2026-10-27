package com.eifi.core.data

import androidx.datastore.core.DataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** [PermissionRequestHistory] stored as a JSON set of [AppPermission]. */
internal class DataStorePermissionRequestHistory @Inject constructor(
    private val store: DataStore<Set<AppPermission>>,
) : PermissionRequestHistory {

    override suspend fun wasRequested(permission: AppPermission): Boolean = permission in store.data.first()

    override suspend fun markRequested(permission: AppPermission) {
        store.updateData { it + permission }
    }
}
