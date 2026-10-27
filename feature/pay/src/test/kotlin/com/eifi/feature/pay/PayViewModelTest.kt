package com.eifi.feature.pay

import com.eifi.core.data.AppPermission
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.nfc.NfcPaymentReader
import com.eifi.core.permission.PermissionChecker
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
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
            nfcPaymentReader = object : NfcPaymentReader {
                override val paymentUrls = emptyFlow<String>()
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
    inner class UiState {
        @Test
        fun `should open the scanner when the camera is allowed`() {
            // given
            granted += AppPermission.Camera

            // when
            val state = viewModel.uiState.value

            // then
            state shouldBe PayUiState(openScanner = true)
        }

        @Test
        fun `should explain the camera permission the first time`() {
            // when
            val state = viewModel.uiState.value

            // then
            state shouldBe PayUiState(activePermission = AppPermission.Camera)
        }

        @Test
        fun `should offer NFC when the camera was refused before`() {
            // given
            requested += AppPermission.Camera

            // when
            val state = viewModel.uiState.value

            // then
            state shouldBe PayUiState(cameraRefused = true)
        }
    }

    @Nested
    inner class OnPermissionDone {
        @Test
        fun `should open the scanner once the camera is allowed`() {
            // when
            viewModel.onPermissionDone(granted = true)

            // then
            viewModel.uiState.value shouldBe PayUiState(openScanner = true)
        }

        @Test
        fun `should offer NFC when the camera is refused`() {
            // when
            viewModel.onPermissionDone(granted = false)

            // then
            viewModel.uiState.value shouldBe PayUiState(cameraRefused = true)
        }

        @Test
        fun `should not ask again for the camera`() {
            // when
            viewModel.onPermissionDone(granted = false)

            // then
            requested shouldBe setOf(AppPermission.Camera)
        }
    }

    @Nested
    inner class OnScannerOpened {
        @Test
        fun `should not open the scanner again`() {
            // given
            granted += AppPermission.Camera

            // when
            viewModel.onScannerOpened()

            // then
            viewModel.uiState.value shouldBe PayUiState()
        }
    }
}
