package com.eifi.core.data

import androidx.datastore.core.DataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** [ReceivingProfileRepository] backed by a typed DataStore holding the whole [ReceivingProfile]. */
internal class DataStoreReceivingProfileRepository @Inject constructor(
    private val profileStore: DataStore<ReceivingProfile?>,
) : ReceivingProfileRepository {

    override val profile: Flow<ReceivingProfile?> = profileStore.data

    override suspend fun saveProfile(profile: ReceivingProfile) {
        profileStore.updateData { profile }
    }
}
