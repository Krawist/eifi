package com.eifi.core.data

import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod
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

internal class DataStoreReceivingProfileRepositoryTest(@TempDir private val folder: File) {
    private val scope = TestScope(UnconfinedTestDispatcher())

    private val file = File(folder, "receiving_profile.json")

    private val repository = DataStoreReceivingProfileRepository(
        jsonDataStore(
            serializer = ReceivingProfile.serializer().nullable,
            defaultValue = null,
            scope = scope.backgroundScope,
        ) { file },
    )

    @Nested
    inner class Profile {
        @Test
        fun `should be null before the profile is filled in`() = scope.runTest {
            // when
            val profile = repository.profile.first()

            // then
            profile.shouldBeNull()
        }

        @Test
        fun `should read a phone number that is not in E164 format as no profile`() = scope.runTest {
            // given
            file.writeText(
                """{"displayName":"Kiosque","receivingMethods":""" +
                    """{"OrangeMoney":{"type":"phoneNumber","value":"690123456"}}}""",
            )

            // when
            val profile = repository.profile.first()

            // then
            profile.shouldBeNull()
        }
    }

    @Nested
    inner class SaveProfile {
        @Test
        fun `should save a merchant code and a phone number`() = scope.runTest {
            // given
            val profile = ReceivingProfile(
                displayName = "Boutique Mama Ngono",
                receivingMethods = mapOf(
                    Operator.OrangeMoney to ReceivingMethod.PhoneNumber("+237690123456"),
                    Operator.MtnMomo to ReceivingMethod.MerchantCode("730154"),
                ),
            )

            // when
            repository.saveProfile(profile)

            // then
            repository.profile.first() shouldBe profile
        }
    }
}
