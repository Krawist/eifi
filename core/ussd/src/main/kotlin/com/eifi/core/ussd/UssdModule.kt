package com.eifi.core.ussd

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** SIM and USSD bindings. TODO(EIFI-204): embedded USSD templates and launch with `TelecomManager.placeCall`. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class UssdModule {
    @Binds
    abstract fun bindSimCardRepository(impl: AndroidSimCardRepository): SimCardRepository
}
