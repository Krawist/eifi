package com.eifi.feature.pay.summary

import androidx.lifecycle.SavedStateHandle
import com.eifi.core.data.PayingAccountRepository
import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.PaymentUrlCodec
import com.eifi.core.payload.ReceivingMethod
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class PaymentSummaryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val start = Instant.parse("2026-10-27T10:00:00Z")

    /** Follows the virtual time of the test. */
    private val clock = object : Clock() {
        override fun instant(): Instant = start.plusMillis(dispatcher.scheduler.currentTime)

        override fun getZone(): ZoneId = ZoneId.of("Africa/Douala")

        override fun withZone(zone: ZoneId): Clock = this
    }

    private val codec = PaymentUrlCodec(baseUrl = "https://pay.eifi.app", clock = clock)

    private val payingAccounts = object : PayingAccountRepository {
        override val lastOperator = MutableStateFlow<Operator?>(null)

        override suspend fun saveLastOperator(operator: Operator) {
            lastOperator.value = operator
        }
    }

    private fun viewModel(url: String) = PaymentSummaryViewModel(
        savedStateHandle = SavedStateHandle(mapOf(PaymentSummaryViewModel.PAYMENT_URL to url)),
        paymentUrlCodec = codec,
        payingAccountRepository = payingAccounts,
        clock = clock,
    )

    /** URL of a payment of 2 500 FCFA to "Kiosque Ndé", expiring 2 minutes after the start of the test. */
    private fun url(receivingMethods: Map<Operator, ReceivingMethod>) = codec.encode(
        PaymentRequest(
            id = "0a1b2c3d4e5f",
            displayName = "Kiosque Ndé",
            amount = 2_500,
            receivingMethods = receivingMethods,
            expiresAt = start.plusSeconds(120),
        ),
    )

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
        fun `should show the payee, the amount and the time left`() = runTest(dispatcher) {
            // given
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            advanceTimeBy(18_000)
            runCurrent()

            // then
            viewModel.uiState.value shouldBe PaymentSummaryUiState.Ready(
                displayName = "Kiosque Ndé",
                amount = 2_500,
                operators = listOf(Operator.OrangeMoney),
                operator = Operator.OrangeMoney,
                receivingMethod = ReceivingMethod.MerchantCode("482913"),
                remainingSeconds = 102,
            )
        }

        @Test
        fun `should offer the last account used when the payee accepts it`() = runTest(dispatcher) {
            // given
            payingAccounts.lastOperator.value = Operator.MtnMomo
            val viewModel = viewModel(
                url(
                    mapOf(
                        Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"),
                        Operator.MtnMomo to ReceivingMethod.PhoneNumber("+237670123456"),
                    ),
                ),
            )
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).operator shouldBe Operator.MtnMomo
        }

        @Test
        fun `should offer the first operator of the payee when the last account used is not accepted`() =
            runTest(dispatcher) {
                // given
                payingAccounts.lastOperator.value = Operator.MtnMomo
                val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
                backgroundScope.launch { viewModel.uiState.collect {} }

                // when
                runCurrent()

                // then
                (viewModel.uiState.value as PaymentSummaryUiState.Ready).operator shouldBe Operator.OrangeMoney
            }

        @Test
        fun `should block the payment once the code expires`() = runTest(dispatcher) {
            // given
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            advanceTimeBy(120_000)
            runCurrent()

            // then
            viewModel.uiState.value shouldBe PaymentSummaryUiState.Expired
        }

        @Test
        fun `should say that an expired code can no longer be paid`() = runTest(dispatcher) {
            // given
            val url = url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")))
            advanceTimeBy(121_000)

            // when
            val viewModel = viewModel(url)

            // then
            viewModel.uiState.value shouldBe PaymentSummaryUiState.Expired
        }

        @Test
        fun `should ask to update the app for a newer URL version`() = runTest(dispatcher) {
            // when
            val viewModel = viewModel("https://pay.eifi.app/p#v2.AAAA")

            // then
            viewModel.uiState.value shouldBe PaymentSummaryUiState.UpdateNeeded
        }

        @Test
        fun `should reject a URL that is not an Eifi payment`() = runTest(dispatcher) {
            // when
            val viewModel = viewModel("https://example.com/p#v1.AAAA")

            // then
            viewModel.uiState.value shouldBe PaymentSummaryUiState.Invalid
        }
    }

    @Nested
    inner class SelectOperator {
        @Test
        fun `should pay with the chosen account`() = runTest(dispatcher) {
            // given
            val viewModel = viewModel(
                url(
                    mapOf(
                        Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"),
                        Operator.MtnMomo to ReceivingMethod.PhoneNumber("+237670123456"),
                    ),
                ),
            )
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.selectOperator(Operator.MtnMomo)
            runCurrent()

            // then
            val state = viewModel.uiState.value as PaymentSummaryUiState.Ready
            state.receivingMethod shouldBe ReceivingMethod.PhoneNumber("+237670123456")
        }
    }

    @Nested
    inner class Confirm {
        @Test
        fun `should remember the account for the next payment`() = runTest(dispatcher) {
            // given
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.confirm()
            runCurrent()

            // then
            payingAccounts.lastOperator.value shouldBe Operator.MtnMomo
        }

        @Test
        fun `should not confirm once the code has expired`() = runTest(dispatcher) {
            // given
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceTimeBy(121_000)
            runCurrent()

            // when
            viewModel.confirm()
            runCurrent()

            // then
            payingAccounts.lastOperator.value.shouldBeNull()
        }
    }
}
