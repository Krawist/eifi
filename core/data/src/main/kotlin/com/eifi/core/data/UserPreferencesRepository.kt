package com.eifi.core.data

import kotlinx.coroutines.flow.Flow

/** Local user preferences. */
interface UserPreferencesRepository {
    /** Mode the app opens on, or `null` until the user has chosen one in the onboarding. */
    val startMode: Flow<AppMode?>

    suspend fun setStartMode(mode: AppMode)
}
