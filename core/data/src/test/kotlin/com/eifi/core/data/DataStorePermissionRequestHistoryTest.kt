package com.eifi.core.data

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.io.File
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.SetSerializer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class DataStorePermissionRequestHistoryTest(@param:TempDir private val folder: File) {
    private val scope = TestScope(UnconfinedTestDispatcher())

    private val history = DataStorePermissionRequestHistory(
        jsonDataStore(
            serializer = SetSerializer(AppPermission.serializer()),
            defaultValue = emptySet(),
            scope = scope.backgroundScope,
        ) { File(folder, "permission_requests.json") },
    )

    @Nested
    inner class WasRequested {
        @Test
        fun `should be false before the permission is requested`() = scope.runTest {
            // when
            val requested = history.wasRequested(AppPermission.Camera)

            // then
            requested.shouldBeFalse()
        }
    }

    @Nested
    inner class MarkRequested {
        @Test
        fun `should remember the requested permission`() = scope.runTest {
            // when
            history.markRequested(AppPermission.Camera)

            // then
            history.wasRequested(AppPermission.Camera).shouldBeTrue()
        }

        @Test
        fun `should not mark the other permissions`() = scope.runTest {
            // when
            history.markRequested(AppPermission.Camera)

            // then
            history.wasRequested(AppPermission.PhoneCalls).shouldBeFalse()
        }
    }
}
