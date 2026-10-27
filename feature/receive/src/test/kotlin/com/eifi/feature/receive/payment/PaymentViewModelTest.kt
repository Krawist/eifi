package com.eifi.feature.receive.payment

import androidx.lifecycle.SavedStateHandle
import com.eifi.core.data.ReceivingProfile
import com.eifi.core.data.ReceivingProfileRepository
import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.nfc.NfcPaymentEmitter
import com.eifi.core.nfc.NfcStatus
import com.eifi.core.nfc.NfcStatusProvider
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
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalCoroutinesApi::class)
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

    private val codec = PaymentUrlCodec(baseUrl = "https://pay.eifi.app", clock = clock)

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

    /** Amount and label of the last [PaymentRepository.openReceived]. */
    private var opened: Pair<Long, String?>? = null

    private val repository = object : PaymentRepository {
        override suspend fun openReceived(amount: Long, label: String?, acceptedOperators: List<Operator>?): Payment {
            opened = amount to label
            return stored.value.copy(id = "a1b2c3d4e5f6")
        }

        override suspend fun openSent(payment: Payment) = false

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

    private val receivingProfiles = object : ReceivingProfileRepository {
        override val profile = MutableStateFlow<ReceivingProfile?>(
            ReceivingProfile(
                displayName = "Kiosque Ndé",
                receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
            ),
        )

        override suspend fun saveProfile(profile: ReceivingProfile) {
            this.profile.value = profile
        }
    }

    /** The URL emitted over NFC, `null` when none is. */
    private var emitted: String? = null

    private val emitter = object : NfcPaymentEmitter {
        override fun start(url: String) {
            emitted = url
        }

        override fun stop(url: String) {
            if (emitted == url) emitted = null
        }
    }

    private val viewModel by lazy {
        PaymentViewModel(
            savedStateHandle = SavedStateHandle(mapOf(PaymentViewModel.PAYMENT_ID to "0a1b2c3d4e5f")),
            paymentRepository = repository,
            paymentService = paymentService,
            paymentChannelService = PaymentChannelService(
                receivingProfileRepository = receivingProfiles,
                paymentService = paymentService,
                paymentUrlCodec = codec,
                nfcPaymentEmitter = emitter,
                nfcStatusProvider = object : NfcStatusProvider {
                    override val status = flowOf(NfcStatus.Ready)
                },
            ),
            clock = clock,
        )
    }

    /** Payment URL of the stored payment, paid to the receiving profile. */
    private fun expectedUrl() = codec.encode(
        PaymentRequest(
            id = "0a1b2c3d4e5f",
            displayName = "Kiosque Ndé",
            amount = 2_500,
            receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
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
        fun `should count down the time left and show the payment URL`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            advanceTimeBy(18_000)
            runCurrent()

            // then
            viewModel.uiState.value shouldBe PaymentUiState(
                amount = 2_500,
                label = "Pain",
                status = PaymentStatus.Pending,
                remainingSeconds = 102,
                url = expectedUrl(),
                nfcStatus = NfcStatus.Ready,
            )
        }

        @Test
        fun `should show no URL without receiving profile`() = runTest(dispatcher) {
            // given
            receivingProfiles.profile.value = null
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            viewModel.uiState.value?.url.shouldBeNull()
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
    inner class NfcEmission {
        @Test
        fun `should emit the payment URL while the payment is pending`() = runTest(dispatcher) {
            // given
            viewModel

            // when
            runCurrent()

            // then
            emitted shouldBe expectedUrl()
        }

        @Test
        fun `should stop emitting once the payment is cancelled`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.cancel()
            runCurrent()

            // then
            emitted.shouldBeNull()
        }

        @Test
        fun `should stop emitting once the payment has expired`() = runTest(dispatcher) {
            // given
            viewModel

            // when
            advanceTimeBy(121_000)
            runCurrent()

            // then
            emitted.shouldBeNull()
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

    @Nested
    inner class Restart {
        @Test
        fun `should open a new payment with the same amount and label once expired`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceTimeBy(121_000)
            runCurrent()

            // when
            viewModel.restart()
            runCurrent()

            // then
            opened shouldBe (2_500L to "Pain")
        }

        @Test
        fun `should show the new payment once opened`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceTimeBy(121_000)
            runCurrent()

            // when
            viewModel.restart()
            advanceTimeBy(1_000)
            runCurrent()

            // then
            viewModel.uiState.value?.restartedPaymentId shouldBe "a1b2c3d4e5f6"
        }

        @Test
        fun `should not restart a pending payment`() = runTest(dispatcher) {
            // given
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.restart()
            runCurrent()

            // then
            opened.shouldBeNull()
        }
    }
}
