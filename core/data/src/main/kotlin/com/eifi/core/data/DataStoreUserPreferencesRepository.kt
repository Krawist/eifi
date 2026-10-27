package com.eifi.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [UserPreferencesRepository] backed by a Preferences DataStore. An unknown stored mode reads as `null`. */
internal class DataStoreUserPreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserPreferencesRepository {

    override val startMode: Flow<AppMode?> = dataStore.data.map { preferences ->
        AppMode.entries.firstOrNull { it.name == preferences[START_MODE] }
    }

    override suspend fun setStartMode(mode: AppMode) {
        dataStore.edit { it[START_MODE] = mode.name }
    }

    private companion object {
        val START_MODE = stringPreferencesKey("start_mode")
    }
}
