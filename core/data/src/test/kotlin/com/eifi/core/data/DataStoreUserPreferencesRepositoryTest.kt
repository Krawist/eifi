package com.eifi.core.data

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.nullable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class DataStoreUserPreferencesRepositoryTest(@TempDir private val folder: File) {
    private val scope = TestScope(UnconfinedTestDispatcher())

    private val file = File(folder, "user_profile.json")

    private val repository = DataStoreUserPreferencesRepository(
        jsonDataStore(
            serializer = UserProfile.serializer().nullable,
            defaultValue = null,
            scope = scope.backgroundScope,
        ) { file },
    )

    @Nested
    inner class Profile {
        @Test
        fun `should be null before the onboarding is completed`() = scope.runTest {
            // when
            val profile = repository.profile.first()

            // then
            profile.shouldBeNull()
        }

        @Test
        fun `should ignore the properties added by a newer version`() = scope.runTest {
            // given
            file.writeText("""{"personalName":"Paul","businessName":null,"language":"fr"}""")

            // when
            val profile = repository.profile.first()

            // then
            profile shouldBe UserProfile(personalName = "Paul", businessName = null)
        }

        @ParameterizedTest
        @ValueSource(strings = ["not json", """{"personalName":null,"businessName":null}"""])
        fun `should read an unreadable or invalid file as no profile`(content: String) = scope.runTest {
            // given
            file.writeText(content)

            // when
            val profile = repository.profile.first()

            // then
            profile.shouldBeNull()
        }
    }

    @Nested
    inner class SaveProfile {
        @Test
        fun `should save the whole profile`() = scope.runTest {
            // given
            val profile = UserProfile(personalName = "Paul", businessName = "Boutique Mama Ngono")

            // when
            repository.saveProfile(profile)

            // then
            repository.profile.first() shouldBe profile
        }

        @Test
        fun `should replace the previous profile`() = scope.runTest {
            // given
            repository.saveProfile(UserProfile(personalName = "Paul", businessName = "Boutique Mama Ngono"))

            // when
            repository.saveProfile(UserProfile(personalName = "Paul", businessName = null))

            // then
            repository.profile.first() shouldBe UserProfile(personalName = "Paul", businessName = null)
        }
    }
}
