package com.eifi.feature.pay

import com.eifi.core.data.AppPermission
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.permission.PermissionChecker
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PayViewModelTest {
    private val granted = mutableSetOf<AppPermission>()

    private val requested = mutableSetOf<AppPermission>()

    private val viewModel by lazy {
        PayViewModel(
            permissionChecker = object : PermissionChecker {
                override fun isGranted(permission: AppPermission) = permission in granted
            },
            permissionRequestHistory = object : PermissionRequestHistory {
                override suspend fun wasRequested(permission: AppPermission) = permission in requested

                override suspend fun markRequested(permission: AppPermission) {
                    requested += permission
                }
            },
        )
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
    inner class OnPay {
        @Test
        fun `should explain the phone calls permission the first time`() {
            // when
            viewModel.onPay()

            // then
            viewModel.uiState.value shouldBe PayUiState(activePermission = AppPermission.PhoneCalls)
        }

        @Test
        fun `should launch the USSD code when the permission is granted`() {
            // given
            granted += AppPermission.PhoneCalls

            // when
            viewModel.onPay()

            // then
            viewModel.uiState.value shouldBe PayUiState(outcome = PayOutcome.UssdReady)
        }

        @Test
        fun `should offer to dial the code when the permission was refused before`() {
            // given
            requested += AppPermission.PhoneCalls

            // when
            viewModel.onPay()

            // then
            viewModel.uiState.value shouldBe PayUiState(outcome = PayOutcome.DialYourself)
        }
    }

    @Nested
    inner class OnScan {
        @Test
        fun `should explain the camera permission the first time`() {
            // when
            viewModel.onScan()

            // then
            viewModel.uiState.value shouldBe PayUiState(activePermission = AppPermission.Camera)
        }
    }

    @Nested
    inner class OnStepDone {
        @Test
        fun `should offer NFC when the camera is refused`() {
            // given
            viewModel.onScan()

            // when
            viewModel.onStepDone(AppPermission.Camera, granted = false)

            // then
            viewModel.uiState.value shouldBe PayUiState(outcome = PayOutcome.UseNfc)
        }

        @Test
        fun `should launch the USSD code once the phone calls permission is granted`() {
            // given
            viewModel.onPay()

            // when
            viewModel.onStepDone(AppPermission.PhoneCalls, granted = true)

            // then
            viewModel.uiState.value shouldBe PayUiState(outcome = PayOutcome.UssdReady)
        }
    }
}
