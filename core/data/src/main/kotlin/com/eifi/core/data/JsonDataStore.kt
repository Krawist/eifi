package com.eifi.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.KSerializer

/**
 * A typed DataStore holding one JSON value; an unreadable file, or a value refused by [validate] (by throwing
 * [IllegalArgumentException]), is replaced by [defaultValue].
 */
internal fun <T> jsonDataStore(
    serializer: KSerializer<T>,
    defaultValue: T,
    validate: (T) -> Unit = {},
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    produceFile: () -> File,
): DataStore<T> = DataStoreFactory.create(
    serializer = JsonSerializer(serializer = serializer, defaultValue = defaultValue, validate = validate),
    corruptionHandler = ReplaceFileCorruptionHandler { defaultValue },
    scope = scope,
    produceFile = produceFile,
)
