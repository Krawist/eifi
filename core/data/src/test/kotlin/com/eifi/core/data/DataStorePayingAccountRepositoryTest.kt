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
internal class DataStorePayingAccountRepositoryTest(@TempDir private val folder: File) {
    private val scope = TestScope(UnconfinedTestDispatcher())

    private val repository = DataStorePayingAccountRepository(
        jsonDataStore(
            serializer = Operator.serializer().nullable,
            defaultValue = null,
            scope = scope.backgroundScope,
        ) { File(folder, "last_paying_operator.json") },
    )

    @Nested
    inner class LastOperator {
        @Test
        fun `should be null before the first payment`() = scope.runTest {
            // when
            val operator = repository.lastOperator.first()

            // then
            operator.shouldBeNull()
        }
    }

    @Nested
    inner class SaveLastOperator {
        @Test
        fun `should give back the saved operator`() = scope.runTest {
            // when
            repository.saveLastOperator(Operator.MtnMomo)

            // then
            repository.lastOperator.first() shouldBe Operator.MtnMomo
        }
    }
}
