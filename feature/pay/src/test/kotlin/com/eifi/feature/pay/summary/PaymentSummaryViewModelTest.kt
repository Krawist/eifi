package com.eifi.feature.pay.summary

import androidx.lifecycle.SavedStateHandle
import com.eifi.core.data.AppPermission
import com.eifi.core.data.PayingAccountChoice
import com.eifi.core.data.PayingAccountRepository
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.PaymentUrlCodec
import com.eifi.core.payload.ReceivingMethod
import com.eifi.core.permission.PermissionChecker
import com.eifi.core.ussd.OperatorUssdConfig
import com.eifi.core.ussd.SimCard
import com.eifi.core.ussd.SimCardRepository
import com.eifi.core.ussd.UssdConfigRepository
import com.eifi.core.ussd.UssdLauncher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
        override val choice = MutableStateFlow<PayingAccountChoice?>(null)

        override suspend fun saveChoice(choice: PayingAccountChoice) {
            this.choice.value = choice
        }
    }

    /** The payer's SIMs, `null` without the phone calls permission. */
    private var sims: List<SimCard>? = null

    private var phoneCallsGranted = true

    private val requestedPermissions = mutableSetOf<AppPermission>()

    /** Ids of the sent payments stored. */
    private val sentPayments = mutableSetOf<String>()

    /** Codes called, with the subscription id of their SIM. */
    private val calls = mutableListOf<Pair<String, Int?>>()

    private fun viewModel(url: String) = PaymentSummaryViewModel(
        savedStateHandle = SavedStateHandle(mapOf(PaymentSummaryViewModel.PAYMENT_URL to url)),
        paymentUrlCodec = codec,
        payingAccountService = PayingAccountService(
            simCardRepository = object : SimCardRepository {
                override fun activeSimCards() = sims
            },
            payingAccountRepository = payingAccounts,
            permissionChecker = object : PermissionChecker {
                override fun isGranted(permission: AppPermission) = phoneCallsGranted
            },
            permissionRequestHistory = object : PermissionRequestHistory {
                override suspend fun wasRequested(permission: AppPermission) = permission in requestedPermissions

                override suspend fun markRequested(permission: AppPermission) {
                    requestedPermissions += permission
                }
            },
        ),
        paymentLaunchService = PaymentLaunchService(
            paymentRepository = object : PaymentRepository {
                override suspend fun openReceived(amount: Long, label: String?) = error("Not used")

                override suspend fun openSent(payment: Payment) = sentPayments.add(payment.id)

                override fun getByIdAsFlow(id: String) = error("Not used")

                override suspend fun cancel(id: String) = error("Not used")

                override suspend fun confirm(id: String, source: ConfirmationSource) = error("Not used")

                override suspend fun expireOverdue() = error("Not used")
            },
            paymentService = PaymentService(),
            ussdConfigRepository = object : UssdConfigRepository {
                override val configs = flowOf(
                    listOf(
                        OperatorUssdConfig(
                            operator = Operator.OrangeMoney,
                            merchantPayment = "#150*47*{code}*{amount}#",
                            transfer = null,
                        ),
                        OperatorUssdConfig(
                            operator = Operator.MtnMomo,
                            merchantPayment = "*126*2*{code}*{amount}#",
                            transfer = null,
                        ),
                    ),
                )
            },
            ussdLauncher = object : UssdLauncher {
                override fun call(code: String, subscriptionId: Int?): Boolean {
                    calls += code to subscriptionId
                    return true
                }

                override fun dial(code: String) = error("Not used")
            },
            clock = clock,
        ),
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

    /** Helper without side effect: a SIM of [operator] in [slotIndex]. */
    private fun sim(slotIndex: Int, operator: Operator?) = SimCard(
        subscriptionId = slotIndex + 1,
        slotIndex = slotIndex,
        carrierName = "",
        operator = operator,
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
        fun `should show the payee, the amount, the account and the time left`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 0, operator = Operator.OrangeMoney))
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            advanceTimeBy(18_000)
            runCurrent()

            // then
            val account =
                PayingAccount(
                    operator = Operator.OrangeMoney,
                    sim = sim(slotIndex = 0, operator = Operator.OrangeMoney),
                )
            viewModel.uiState.value shouldBe PaymentSummaryUiState.Ready(
                paymentId = "0a1b2c3d4e5f",
                displayName = "Kiosque Ndé",
                amount = 2_500,
                payeeOperators = listOf(Operator.OrangeMoney),
                accounts = listOf(account),
                account = account,
                receivingMethod = ReceivingMethod.MerchantCode("482913"),
                ussdAvailable = true,
                remainingSeconds = 102,
            )
        }

        @Test
        fun `should offer the remembered account when the payee accepts it`() = runTest(dispatcher) {
            // given
            sims =
                listOf(
                    sim(slotIndex = 0, operator = Operator.OrangeMoney),
                    sim(slotIndex = 1, operator = Operator.MtnMomo),
                )
            payingAccounts.choice.value = PayingAccountChoice(operator = Operator.MtnMomo, subscriptionId = 2)
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
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).account shouldBe
                PayingAccount(operator = Operator.MtnMomo, sim = sim(slotIndex = 1, operator = Operator.MtnMomo))
        }

        @Test
        fun `should block the payment when no SIM is with an operator the payee accepts`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 0, operator = null))
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).account.shouldBeNull()
        }

        @Test
        fun `should offer every operator of the payee when the SIMs are not known`() = runTest(dispatcher) {
            // given
            phoneCallsGranted = false
            requestedPermissions += AppPermission.PhoneCalls
            val viewModel = viewModel(
                url(
                    mapOf(
                        Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"),
                        Operator.MtnMomo to ReceivingMethod.MerchantCode("771234"),
                    ),
                ),
            )
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).accounts shouldBe listOf(
                PayingAccount(operator = Operator.OrangeMoney, sim = null),
                PayingAccount(operator = Operator.MtnMomo, sim = null),
            )
        }

        @Test
        fun `should ask for the phone calls permission at the first summary`() = runTest(dispatcher) {
            // given
            phoneCallsGranted = false
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).activePermission shouldBe AppPermission.PhoneCalls
        }

        @Test
        fun `should not ask again for the phone calls permission`() = runTest(dispatcher) {
            // given
            phoneCallsGranted = false
            requestedPermissions += AppPermission.PhoneCalls
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }

            // when
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).activePermission.shouldBeNull()
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
    inner class OnPermissionDone {
        @Test
        fun `should offer the SIMs once the permission is granted`() = runTest(dispatcher) {
            // given
            phoneCallsGranted = false
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            sims = listOf(sim(slotIndex = 0, operator = Operator.OrangeMoney))

            // when
            viewModel.onPermissionDone(granted = true)
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).account shouldBe
                PayingAccount(
                    operator = Operator.OrangeMoney,
                    sim = sim(slotIndex = 0, operator = Operator.OrangeMoney),
                )
        }

        @Test
        fun `should not ask again once asked`() = runTest(dispatcher) {
            // given
            phoneCallsGranted = false
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.onPermissionDone(granted = false)
            runCurrent()

            // then
            requestedPermissions shouldBe setOf(AppPermission.PhoneCalls)
        }
    }

    @Nested
    inner class ChooseAccount {
        @Test
        fun `should pay with the chosen account`() = runTest(dispatcher) {
            // given
            sims =
                listOf(
                    sim(slotIndex = 0, operator = Operator.OrangeMoney),
                    sim(slotIndex = 1, operator = Operator.MtnMomo),
                )
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
            viewModel.chooseAccount(
                account = PayingAccount(
                    operator = Operator.MtnMomo,
                    sim = sim(slotIndex = 1, operator = Operator.MtnMomo),
                ),
                remember = false,
            )
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).receivingMethod shouldBe
                ReceivingMethod.PhoneNumber("+237670123456")
        }

        @Test
        fun `should remember the chosen account when asked`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.chooseAccount(
                account = PayingAccount(
                    operator = Operator.MtnMomo,
                    sim = sim(slotIndex = 1, operator = Operator.MtnMomo),
                ),
                remember = true,
            )
            runCurrent()

            // then
            payingAccounts.choice.value shouldBe PayingAccountChoice(operator = Operator.MtnMomo, subscriptionId = 2)
        }
    }

    @Nested
    inner class Confirm {
        @Test
        fun `should ask for the biometric prompt`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.confirm()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).confirmationStep shouldBe
                ConfirmationStep.Authenticating
        }

        @Test
        fun `should not ask for the biometric prompt without compatible account`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 0, operator = null))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.confirm()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).confirmationStep shouldBe ConfirmationStep.None
        }

        @Test
        fun `should not ask for the biometric prompt without known USSD code`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 0, operator = Operator.OrangeMoney))
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.PhoneNumber("+237690123456"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.confirm()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).confirmationStep shouldBe ConfirmationStep.None
        }

        @Test
        fun `should not ask for the biometric prompt once the code has expired`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceTimeBy(121_000.milliseconds)
            runCurrent()

            // when
            viewModel.confirm()
            runCurrent()

            // then
            viewModel.uiState.value shouldBe PaymentSummaryUiState.Expired
        }
    }

    @Nested
    inner class OnAuthenticated {
        @Test
        fun `should call the USSD code on the SIM of the account`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.OrangeMoney))
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            viewModel.confirm()
            runCurrent()

            // when
            viewModel.onAuthenticated()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).launchOutcome shouldBe LaunchOutcome.Called
            calls shouldBe listOf("#150*47*482913*2500#" to 2)
        }

        @Test
        fun `should not launch a session already launched`() = runTest(dispatcher) {
            // given
            sentPayments += "0a1b2c3d4e5f"
            sims = listOf(sim(slotIndex = 1, operator = Operator.OrangeMoney))
            val viewModel = viewModel(url(mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            viewModel.confirm()
            runCurrent()

            // when
            viewModel.onAuthenticated()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).launchOutcome shouldBe
                LaunchOutcome.AlreadyLaunched
            calls.shouldBeEmpty()
        }

        @Test
        fun `should confirm the payment`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            viewModel.confirm()
            runCurrent()

            // when
            viewModel.onAuthenticated()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).confirmationStep shouldBe
                ConfirmationStep.Authenticated
        }

        @Test
        fun `should remember the account for the next payment`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            viewModel.confirm()
            runCurrent()

            // when
            viewModel.onAuthenticated()
            runCurrent()

            // then
            payingAccounts.choice.value shouldBe PayingAccountChoice(operator = Operator.MtnMomo, subscriptionId = 2)
        }

        @Test
        fun `should not remember the account when the payer opted out`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            viewModel.chooseAccount(
                account = PayingAccount(
                    operator = Operator.MtnMomo,
                    sim = sim(slotIndex = 1, operator = Operator.MtnMomo),
                ),
                remember = false,
            )
            viewModel.confirm()
            runCurrent()

            // when
            viewModel.onAuthenticated()
            runCurrent()

            // then
            payingAccounts.choice.value.shouldBeNull()
        }

        @Test
        fun `should ignore an authentication without prompt`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            // when
            viewModel.onAuthenticated()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).confirmationStep shouldBe ConfirmationStep.None
        }
    }

    @Nested
    inner class OnAuthenticationCancelled {
        @Test
        fun `should go back to the summary without confirming`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            viewModel.confirm()
            runCurrent()

            // when
            viewModel.onAuthenticationCancelled()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).confirmationStep shouldBe ConfirmationStep.None
        }

        @Test
        fun `should keep a confirmed payment confirmed`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            viewModel.confirm()
            runCurrent()
            viewModel.onAuthenticated()
            runCurrent()

            // when
            viewModel.onAuthenticationCancelled()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).confirmationStep shouldBe
                ConfirmationStep.Authenticated
        }
    }

    @Nested
    inner class OnScreenLockMissing {
        @Test
        fun `should ask to set up a screen lock`() = runTest(dispatcher) {
            // given
            sims = listOf(sim(slotIndex = 1, operator = Operator.MtnMomo))
            val viewModel = viewModel(url(mapOf(Operator.MtnMomo to ReceivingMethod.MerchantCode("482913"))))
            backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            viewModel.confirm()
            runCurrent()

            // when
            viewModel.onScreenLockMissing()
            runCurrent()

            // then
            (viewModel.uiState.value as PaymentSummaryUiState.Ready).confirmationStep shouldBe
                ConfirmationStep.ScreenLockNeeded
        }
    }
}
