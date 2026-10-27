package com.eifi.core.data

import kotlinx.coroutines.flow.Flow

/** Receiving profile of the Receive mode: display name and receiving method per operator (EIFI-100). */
interface ReceivingProfileRepository {
    /** `false` until the user has filled in the profile, which then opens on the first use of Receive mode. */
    val isConfigured: Flow<Boolean>
}
