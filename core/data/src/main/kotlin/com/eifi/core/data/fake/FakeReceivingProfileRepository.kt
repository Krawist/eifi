package com.eifi.core.data.fake

import com.eifi.core.data.ReceivingProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Never configured, so that Receive mode always goes through the profile. TODO(EIFI-100): real profile. */
internal class FakeReceivingProfileRepository @Inject constructor() : ReceivingProfileRepository {
    override val isConfigured: Flow<Boolean> = flowOf(false)
}
