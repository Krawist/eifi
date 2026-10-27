package com.eifi.feature.receive

import com.eifi.core.data.AppPermission
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.permission.PermissionChecker
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ReceivePermissionsViewModelTest {
    private val granted = mutableSetOf<AppPermission>()

    private val requested = mutableListOf<AppPermission>()

    private val checker = object : PermissionChecker {
        override fun isGranted(permission: AppPermission) = permission in granted
    }

    private val history = object : PermissionRequestHistory {
        override suspend fun wasRequested(permission: AppPermission) = permission in requested

        override suspend fun markRequested(permission: AppPermission) {
            requested += permission
        }
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should ask for the notification access first`() {
            // when
            val viewModel = ReceivePermissionsViewModel(checker, history)

            // then
            viewModel.uiState.value shouldBe ReceivePermissionsUiState.Asking(AppPermission.NotificationAccess)
        }

        @Test
        fun `should not ask again for a notification access already refused`() {
            // given
            requested += AppPermission.NotificationAccess

            // when
            val viewModel = ReceivePermissionsViewModel(checker, history)

            // then
            viewModel.uiState.value shouldBe ReceivePermissionsUiState.Done
        }

        @Test
        fun `should ask for the battery when the notification access is already granted`() {
            // given
            granted += AppPermission.NotificationAccess

            // when
            val viewModel = ReceivePermissionsViewModel(checker, history)

            // then
            viewModel.uiState.value shouldBe ReceivePermissionsUiState.Asking(AppPermission.BatteryOptimisation)
        }

        @Test
        fun `should open the mode directly when everything is granted`() {
            // given
            granted += listOf(AppPermission.NotificationAccess, AppPermission.BatteryOptimisation)

            // when
            val viewModel = ReceivePermissionsViewModel(checker, history)

            // then
            viewModel.uiState.value shouldBe ReceivePermissionsUiState.Done
        }
    }

    @Nested
    inner class OnStepDone {
        @Test
        fun `should ask for the battery once the notification access is granted`() {
            // given
            val viewModel = ReceivePermissionsViewModel(checker, history)

            // when
            viewModel.onStepDone(AppPermission.NotificationAccess, granted = true)

            // then
            viewModel.uiState.value shouldBe ReceivePermissionsUiState.Asking(AppPermission.BatteryOptimisation)
        }

        @Test
        fun `should skip the battery when the notification access is refused`() {
            // given
            val viewModel = ReceivePermissionsViewModel(checker, history)

            // when
            viewModel.onStepDone(AppPermission.NotificationAccess, granted = false)

            // then
            viewModel.uiState.value shouldBe ReceivePermissionsUiState.Done
        }

        @Test
        fun `should remember that the permission was requested`() {
            // given
            val viewModel = ReceivePermissionsViewModel(checker, history)

            // when
            viewModel.onStepDone(AppPermission.NotificationAccess, granted = false)

            // then
            requested shouldContainExactly listOf(AppPermission.NotificationAccess)
        }
    }
}
