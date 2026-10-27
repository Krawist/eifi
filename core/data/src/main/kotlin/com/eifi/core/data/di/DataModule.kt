package com.eifi.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import com.eifi.core.data.ActivityRepository
import com.eifi.core.data.DataStoreUserPreferencesRepository
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
import com.eifi.core.data.fake.FakeActivityRepository
import com.eifi.core.data.fake.FakeReceivingProfileRepository
import com.eifi.core.data.userProfileDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindUserPreferencesRepository(impl: DataStoreUserPreferencesRepository): UserPreferencesRepository

    @Binds
    abstract fun bindActivityRepository(impl: FakeActivityRepository): ActivityRepository

    @Binds
    abstract fun bindReceivingProfileRepository(impl: FakeReceivingProfileRepository): ReceivingProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideUserProfileStore(@ApplicationContext context: Context): DataStore<UserProfile?> =
            userProfileDataStore { context.dataStoreFile("user_profile.json") }

        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
