package com.eifi.core.data

import androidx.datastore.core.DataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** [UserPreferencesRepository] backed by a typed DataStore holding the whole [UserProfile]. */
internal class DataStoreUserPreferencesRepository @Inject constructor(
    private val profileStore: DataStore<UserProfile?>,
) : UserPreferencesRepository {

    override val profile: Flow<UserProfile?> = profileStore.data

    override suspend fun saveProfile(profile: UserProfile) {
        profileStore.updateData { profile }
    }
}
