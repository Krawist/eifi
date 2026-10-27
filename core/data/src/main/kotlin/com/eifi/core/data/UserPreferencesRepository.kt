package com.eifi.core.data

import kotlinx.coroutines.flow.Flow

/** Local user preferences. */
interface UserPreferencesRepository {
    /** `null` until the onboarding is completed: saving the profile is its last step. */
    val profile: Flow<UserProfile?>

    suspend fun saveProfile(profile: UserProfile)
}
