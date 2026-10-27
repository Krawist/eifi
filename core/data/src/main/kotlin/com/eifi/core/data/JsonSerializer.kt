package com.eifi.core.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Stores a value as JSON in a typed DataStore; an empty file means [defaultValue].
 * [validate] checks the value read. Unknown keys are ignored, so that an older app version can read a newer file:
 * a new property needs a default value.
 */
internal class JsonSerializer<T>(
    private val serializer: KSerializer<T>,
    override val defaultValue: T,
    private val validate: (T) -> Unit,
) : Serializer<T> {

    override suspend fun readFrom(input: InputStream): T {
        val text = input.readBytes().decodeToString()
        if (text.isEmpty()) return defaultValue
        return try {
            json.decodeFromString(serializer, text).also(validate)
        } catch (e: SerializationException) {
            throw CorruptionException("Unreadable JSON", e)
        } catch (e: IllegalArgumentException) {
            throw CorruptionException("Invalid content", e)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        if (t != null) {
            withContext(Dispatchers.IO) {
                output.write(json.encodeToString(serializer, t).encodeToByteArray())
            }
        }
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
