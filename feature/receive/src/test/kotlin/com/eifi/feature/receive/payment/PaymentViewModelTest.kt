package com.eifi.feature.receive.payment

import androidx.lifecycle.SavedStateHandle
import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PaymentViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val start = Instant.parse("2026-10-27T10:00:00Z")

    /** Follows the virtual time of the test. */
    private val clock = object : Clock() {
        override fun instant(): Instant = start.plusMillis(dispatcher.scheduler.currentTime)

        override fun getZone(): ZoneId = ZoneId.of("Africa/Douala")

        override fun withZone(zone: ZoneId): Clock = this
    }

    private val paymentService = PaymentService()

    private val stored = MutableStateFlow(
        Payment(
            id = "0a1b2c3d4e5f",
            type = TransactionType.Received,
            amount = 2_500,
            label = "Pain",
            createdAt = start,
            expiresAt = start.plusSeconds(120),
        ),
    )

    private val repository = object : PaymentRepository {
        override suspend fun openReceived(amount: Long, label: String?) = stored.value

        override fun getByIdAsFlow(id: String) = stored.map { it.takeIf { payment -> payment.id == id } }

        override suspend fun cancel(id: String) {
            stored.value = paymentService.cancel(payment = stored.value, at = clock.instant())
        }

        override suspend fun confirm(id: String, source: ConfirmationSource) = Unit

        override suspend fun expireOverdue() {
            if (paymentService.statusAt(payment = stored.value, now = clock.instant()) == PaymentStatus.Expired) {
                stored.value = paymentService.expire(payment = stored.value, at = clock.instant())
            }
        }
    }

    private val viewModel by lazy {
        PaymentViewModel(
            savedStateHandle = SavedStateHandle(mapOf(PaymentViewModel.PAYMENT_ID to "0a1b2c3d4e5f")),
            paymentRepository = repository,
            paymentService = paymentService,
            clock = clock,
        )
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should count down the time left`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            advanceTimeBy(18_000)
            runCurrent()

            // then
            viewModel.uiState.value shouldBe
                PaymentUiState(amount = 2_500, label = "Pain", status = PaymentStatus.Pending, remainingSeconds = 102)
        }

        @Test
        fun `should store the expiry when the countdown ends`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            advanceTimeBy(120_000)
            runCurrent()

            // then
            stored.value.status shouldBe PaymentStatus.Expired
        }
    }

    @Nested
    inner class Cancel {
        @Test
        fun `should cancel a pending payment`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.cancel()
            runCurrent()

            // then
            stored.value.status shouldBe PaymentStatus.Cancelled
        }

        @Test
        fun `should not cancel an expired payment`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceTimeBy(121_000)
            runCurrent()

            // when
            viewModel.cancel()
            runCurrent()

            // then
            stored.value.status shouldBe PaymentStatus.Expired
        }
    }
}
