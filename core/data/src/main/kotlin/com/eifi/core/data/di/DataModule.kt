package com.eifi.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import com.eifi.core.data.ActivityRepository
import com.eifi.core.data.AppPermission
import com.eifi.core.data.DataStorePermissionRequestHistory
import com.eifi.core.data.DataStoreReceivingProfileRepository
import com.eifi.core.data.DataStoreUserPreferencesRepository
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.data.ReceivingProfile
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
import com.eifi.core.data.fake.FakeActivityRepository
import com.eifi.core.data.jsonDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.nullable

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindUserPreferencesRepository(impl: DataStoreUserPreferencesRepository): UserPreferencesRepository

    @Binds
    abstract fun bindPermissionRequestHistory(impl: DataStorePermissionRequestHistory): PermissionRequestHistory

    @Binds
    abstract fun bindActivityRepository(impl: FakeActivityRepository): ActivityRepository

    @Binds
    abstract fun bindReceivingProfileRepository(impl: DataStoreReceivingProfileRepository): ReceivingProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideUserProfileStore(@ApplicationContext context: Context): DataStore<UserProfile?> =
            jsonDataStore(serializer = UserProfile.serializer().nullable, defaultValue = null) {
                context.dataStoreFile("user_profile.json")
            }

        @Provides
        @Singleton
        fun provideReceivingProfileStore(@ApplicationContext context: Context): DataStore<ReceivingProfile?> =
            jsonDataStore(serializer = ReceivingProfile.serializer().nullable, defaultValue = null) {
                context.dataStoreFile("receiving_profile.json")
            }

        @Provides
        @Singleton
        fun providePermissionRequestStore(@ApplicationContext context: Context): DataStore<Set<AppPermission>> =
            jsonDataStore(serializer = SetSerializer(AppPermission.serializer()), defaultValue = emptySet()) {
                context.dataStoreFile("permission_requests.json")
            }

        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
