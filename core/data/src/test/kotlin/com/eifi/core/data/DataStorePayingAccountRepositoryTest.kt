package com.eifi.core.data

import com.eifi.core.payload.Operator
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.nullable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@OptIn(ExperimentalCoroutinesApi::class)
internal class DataStorePayingAccountRepositoryTest(@param:TempDir private val folder: File) {
    private val scope = TestScope(UnconfinedTestDispatcher())

    private val repository = DataStorePayingAccountRepository(
        jsonDataStore(
            serializer = PayingAccountChoice.serializer().nullable,
            defaultValue = null,
            scope = scope.backgroundScope,
        ) { File(folder, "paying_account.json") },
    )

    @Nested
    inner class Choice {
        @Test
        fun `should be null before an account is remembered`() = scope.runTest {
            // when
            val choice = repository.choice.first()

            // then
            choice.shouldBeNull()
        }
    }

    @Nested
    inner class SaveChoice {
        @Test
        fun `should give back the saved account`() = scope.runTest {
            // when
            repository.saveChoice(PayingAccountChoice(operator = Operator.MtnMomo, subscriptionId = 3))

            // then
            repository.choice.first() shouldBe PayingAccountChoice(operator = Operator.MtnMomo, subscriptionId = 3)
        }
    }
}
