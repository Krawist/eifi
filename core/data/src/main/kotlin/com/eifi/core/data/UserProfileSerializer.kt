package com.eifi.core.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** [UserProfile] as JSON; an empty file means that the onboarding is not completed. */
internal object UserProfileSerializer : Serializer<UserProfile?> {
    /** Unknown keys are ignored, so that an older app version can read a newer file. */
    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: UserProfile? = null

    override suspend fun readFrom(input: InputStream): UserProfile? {
        val text = input.readBytes().decodeToString()
        if (text.isEmpty()) return null
        return try {
            json.decodeFromString(UserProfile.serializer(), text)
        } catch (e: SerializationException) {
            throw CorruptionException("Unreadable user profile", e)
        } catch (e: IllegalArgumentException) {
            throw CorruptionException("Invalid user profile", e)
        }
    }

    override suspend fun writeTo(t: UserProfile?, output: OutputStream) {
        if (t != null) output.write(json.encodeToString(UserProfile.serializer(), t).encodeToByteArray())
    }
}

/** The profile store; an unreadable file is replaced by "no profile", which shows the onboarding again. */
internal fun userProfileDataStore(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    produceFile: () -> File,
): DataStore<UserProfile?> = DataStoreFactory.create(
    serializer = UserProfileSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { null },
    scope = scope,
    produceFile = produceFile,
)
