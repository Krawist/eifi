package com.eifi.core.ussd

import com.eifi.core.ussd.fake.FakeUssdConfigRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** SIM and USSD bindings. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class UssdModule {
    @Binds
    abstract fun bindSimCardRepository(impl: AndroidSimCardRepository): SimCardRepository

    @Binds
    abstract fun bindUssdConfigRepository(impl: FakeUssdConfigRepository): UssdConfigRepository

    @Binds
    abstract fun bindUssdLauncher(impl: TelecomUssdLauncher): UssdLauncher
}
