package com.eifi.feature.pay.receipt

import androidx.lifecycle.SavedStateHandle
import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.payload.Operator
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReceiptViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val stored = MutableStateFlow<Payment?>(null)

    private val viewModel by lazy {
        ReceiptViewModel(
            savedStateHandle = SavedStateHandle(mapOf(ReceiptViewModel.PAYMENT_ID to "0a1b2c3d4e5f")),
            paymentRepository = object : PaymentRepository {
                override suspend fun openReceived(amount: Long, label: String?, acceptedOperators: List<Operator>?) =
                    error("Not used")

                override suspend fun openSent(payment: Payment) = error("Not used")

                override fun getByIdAsFlow(id: String) = stored.map { it?.takeIf { payment -> payment.id == id } }

                override fun getAllAsFlow() = emptyFlow<List<Payment>>()

                override suspend fun cancel(id: String) = error("Not used")

                override suspend fun confirm(id: String, source: ConfirmationSource) = error("Not used")

                override suspend fun expireOverdue() = error("Not used")
            },
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
        fun `should show the request sent until the debit is confirmed`() = runTest(dispatcher) {
            // given
            stored.value = Payment(
                id = "0a1b2c3d4e5f",
                type = TransactionType.Sent,
                amount = 2_500,
                label = null,
                createdAt = Instant.parse("2026-10-27T13:32:00Z"),
                expiresAt = Instant.parse("2026-10-27T13:34:00Z"),
                counterpartyName = "Kiosque Ndé",
                operator = Operator.OrangeMoney,
            )
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            viewModel.uiState.value shouldBe ReceiptUiState(
                payeeName = "Kiosque Ndé",
                amount = 2_500,
                operator = Operator.OrangeMoney,
                sentAt = Instant.parse("2026-10-27T13:32:00Z"),
                debitConfirmed = false,
            )
        }

        @Test
        fun `should show the debit confirmed once the payment is confirmed`() = runTest(dispatcher) {
            // given
            stored.value = Payment(
                id = "0a1b2c3d4e5f",
                type = TransactionType.Sent,
                amount = 2_500,
                label = null,
                createdAt = Instant.parse("2026-10-27T13:32:00Z"),
                expiresAt = Instant.parse("2026-10-27T13:34:00Z"),
                status = PaymentStatus.Confirmed,
                confirmationSource = ConfirmationSource.Sms,
                endedAt = Instant.parse("2026-10-27T13:33:00Z"),
                counterpartyName = "Kiosque Ndé",
                operator = Operator.OrangeMoney,
            )
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            viewModel.uiState.value?.debitConfirmed shouldBe true
        }
    }
}
