package com.eifi.feature.onboarding.history

import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.payload.Operator
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class HistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val now = Instant.parse("2026-10-27T10:00:00Z")

    private val payments = MutableStateFlow<List<Payment>>(emptyList())

    private val viewModel by lazy {
        HistoryViewModel(
            paymentRepository = object : PaymentRepository {
                override suspend fun openReceived(amount: Long, label: String?, acceptedOperators: List<Operator>?) =
                    error("Not used")

                override suspend fun openSent(payment: Payment) = error("Not used")

                override fun getByIdAsFlow(id: String) = error("Not used")

                override fun getAllAsFlow() = payments

                override suspend fun cancel(id: String) = error("Not used")

                override suspend fun confirm(id: String, source: ConfirmationSource) = error("Not used")

                override suspend fun expireOverdue() = error("Not used")
            },
            paymentService = PaymentService(),
            clock = Clock.fixed(now, ZoneId.of("Africa/Douala")),
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
        fun `should group the payments by day of the phone`() = runTest(dispatcher) {
            // given
            payments.value = listOf(
                Payment(
                    id = "000000000002",
                    type = TransactionType.Sent,
                    amount = 1_000,
                    label = null,
                    createdAt = now,
                    expiresAt = now.plusSeconds(120),
                    counterpartyName = "Kiosque Ndé",
                    operator = Operator.MtnMomo,
                ),
                Payment(
                    id = "000000000001",
                    type = TransactionType.Received,
                    amount = 2_500,
                    label = "Pain",
                    createdAt = Instant.parse("2026-10-26T23:30:00Z"),
                    expiresAt = Instant.parse("2026-10-26T23:32:00Z"),
                    status = PaymentStatus.Confirmed,
                    confirmationSource = ConfirmationSource.Sms,
                    counterpartyName = "+237 6XX XX XX 41",
                    operator = Operator.OrangeMoney,
                ),
            )
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            viewModel.uiState.value?.days?.map { day -> day.date to day.entries.map { it.id } } shouldBe listOf(
                LocalDate.parse("2026-10-27") to listOf("000000000002", "000000000001"),
            )
        }

        @ParameterizedTest
        @CsvSource(
            "Received, Pending, , 120, Waiting",
            "Received, Pending, , -1, Expired",
            "Received, Confirmed, Sms, 120, Confirmed",
            "Received, Confirmed, Manual, 120, Manual",
            "Received, Cancelled, , 120, Cancelled",
            "Sent, Pending, , -1, RequestSent",
            "Sent, Confirmed, Sms, 120, DebitConfirmed",
        )
        fun `should show the status of each payment`(
            type: TransactionType,
            status: PaymentStatus,
            source: ConfirmationSource?,
            expiresInSeconds: Long,
            expected: HistoryStatus,
        ) = runTest(dispatcher) {
            // given
            payments.value = listOf(
                Payment(
                    id = "000000000001",
                    type = type,
                    amount = 2_500,
                    label = null,
                    createdAt = now.minusSeconds(10),
                    expiresAt = now.plusSeconds(expiresInSeconds),
                    status = status,
                    confirmationSource = source,
                ),
            )
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            viewModel.uiState.value?.days?.single()?.entries?.single()?.status shouldBe expected
        }
    }
}
