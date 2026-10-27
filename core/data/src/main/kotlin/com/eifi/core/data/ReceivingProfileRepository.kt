package com.eifi.core.data

import kotlinx.coroutines.flow.Flow

/** Receiving profile of the Receive mode, filled in the first time the mode is used (EIFI-100). */
interface ReceivingProfileRepository {
    /** `null` until the user has filled in the profile. */
    val profile: Flow<ReceivingProfile?>

    suspend fun saveProfile(profile: ReceivingProfile)
}
