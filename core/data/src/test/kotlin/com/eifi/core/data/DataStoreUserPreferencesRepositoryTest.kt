package com.eifi.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class DataStoreUserPreferencesRepositoryTest(@TempDir private val folder: File) {
    private val scope = TestScope(UnconfinedTestDispatcher())

    private val dataStore = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
        File(folder, "user_preferences.preferences_pb")
    }

    private val repository = DataStoreUserPreferencesRepository(dataStore)

    @Nested
    inner class StartMode {
        @Test
        fun `should be null before the user chooses a mode`() = scope.runTest {
            // when
            val mode = repository.startMode.first()

            // then
            mode.shouldBeNull()
        }

        @Test
        fun `should read an unknown stored mode as null`() = scope.runTest {
            // given
            dataStore.edit { it[stringPreferencesKey("start_mode")] = "Retired" }

            // when
            val mode = repository.startMode.first()

            // then
            mode.shouldBeNull()
        }
    }

    @Nested
    inner class SetStartMode {
        @Test
        fun `should replace the previously chosen mode`() = scope.runTest {
            // given
            repository.setStartMode(AppMode.Pay)

            // when
            repository.setStartMode(AppMode.Receive)

            // then
            repository.startMode.first() shouldBe AppMode.Receive
        }
    }
}
