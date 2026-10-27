package com.eifi.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.eifi.core.data.ActivityRepository
import com.eifi.core.data.AppPermission
import com.eifi.core.data.DataStorePayingAccountRepository
import com.eifi.core.data.DataStorePermissionRequestHistory
import com.eifi.core.data.DataStoreReceivingProfileRepository
import com.eifi.core.data.DataStoreUserPreferencesRepository
import com.eifi.core.data.PayingAccountChoice
import com.eifi.core.data.PayingAccountRepository
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.data.ReceivingProfile
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.ReceivingProfileService
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
import com.eifi.core.data.UserProfileService
import com.eifi.core.data.database.EifiDatabase
import com.eifi.core.data.database.RoomActivityRepository
import com.eifi.core.data.database.RoomPaymentRepository
import com.eifi.core.data.database.RoomUssdConfigRepository
import com.eifi.core.data.jsonDataStore
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.ussd.UssdConfigRepository
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
    abstract fun bindPaymentRepository(impl: RoomPaymentRepository): PaymentRepository

    @Binds
    abstract fun bindActivityRepository(impl: RoomActivityRepository): ActivityRepository

    @Binds
    abstract fun bindUssdConfigRepository(impl: RoomUssdConfigRepository): UssdConfigRepository

    @Binds
    abstract fun bindPayingAccountRepository(impl: DataStorePayingAccountRepository): PayingAccountRepository

    @Binds
    abstract fun bindReceivingProfileRepository(impl: DataStoreReceivingProfileRepository): ReceivingProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideUserProfileStore(
            @ApplicationContext context: Context,
            userProfileService: UserProfileService,
        ): DataStore<UserProfile?> = jsonDataStore(
            serializer = UserProfile.serializer().nullable,
            defaultValue = null,
            validate = { it?.let(userProfileService::validate) },
        ) {
            context.dataStoreFile("user_profile.json")
        }

        @Provides
        @Singleton
        fun provideReceivingProfileStore(
            @ApplicationContext context: Context,
            receivingProfileService: ReceivingProfileService,
        ): DataStore<ReceivingProfile?> = jsonDataStore(
            serializer = ReceivingProfile.serializer().nullable,
            defaultValue = null,
            validate = { it?.let(receivingProfileService::validate) },
        ) {
            context.dataStoreFile("receiving_profile.json")
        }

        @Provides
        @Singleton
        fun providePayingAccountStore(@ApplicationContext context: Context): DataStore<PayingAccountChoice?> =
            jsonDataStore(serializer = PayingAccountChoice.serializer().nullable, defaultValue = null) {
                context.dataStoreFile("paying_account.json")
            }

        @Provides
        @Singleton
        fun providePermissionRequestStore(@ApplicationContext context: Context): DataStore<Set<AppPermission>> =
            jsonDataStore(serializer = SetSerializer(AppPermission.serializer()), defaultValue = emptySet()) {
                context.dataStoreFile("permission_requests.json")
            }

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): EifiDatabase =
            Room.databaseBuilder(context, EifiDatabase::class.java, "eifi.db").build()

        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
