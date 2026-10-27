package com.eifi.feature.pay.summary

import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.payload.Operator
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.ReceivingMethod
import com.eifi.core.ussd.OperatorUssdConfig
import com.eifi.core.ussd.SimCard
import com.eifi.core.ussd.UssdConfigRepository
import com.eifi.core.ussd.UssdLauncher
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PaymentLaunchServiceTest {
    private val now = Instant.parse("2026-10-27T10:00:00Z")

    /** The sent payments stored. */
    private val sentPayments = mutableListOf<Payment>()

    /** Codes called, with the subscription id of their SIM. */
    private val calls = mutableListOf<Pair<String, Int?>>()

    /** Codes typed in the dialer. */
    private val dialed = mutableListOf<String>()

    private var callsAllowed = true

    private val service = PaymentLaunchService(
        paymentRepository = object : PaymentRepository {
            override suspend fun openReceived(amount: Long, label: String?) = error("Not used")

            override suspend fun openSent(payment: Payment): Boolean {
                if (sentPayments.any { it.id == payment.id }) return false
                sentPayments += payment
                return true
            }

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
                if (callsAllowed) calls += code to subscriptionId
                return callsAllowed
            }

            override fun dial(code: String) {
                dialed += code
            }
        },
        clock = Clock.fixed(now, ZoneOffset.UTC),
    )

    /** Helper without side effect: a request of 2 500 FCFA paid to [method] with Orange Money. */
    private fun request(method: ReceivingMethod) = PaymentRequest(
        id = "0a1b2c3d4e5f",
        displayName = "Kiosque Ndé",
        amount = 2_500,
        receivingMethods = mapOf(Operator.OrangeMoney to method),
        expiresAt = now.plusSeconds(120),
    )

    private val account = PayingAccount(
        operator = Operator.OrangeMoney,
        sim = SimCard(subscriptionId = 3, slotIndex = 0, carrierName = "Orange CM", operator = Operator.OrangeMoney),
    )

    @Nested
    inner class IsAvailable {
        @Test
        fun `should offer a payment whose code the config gives`() {
            // given
            val configs = listOf(
                OperatorUssdConfig(
                    operator = Operator.OrangeMoney,
                    merchantPayment = "#150*47*{code}*{amount}#",
                    transfer = null,
                ),
            )

            // when & then
            service.isAvailable(
                request = request(ReceivingMethod.MerchantCode("482913")),
                account = account,
                configs = configs,
            ).shouldBeTrue()
        }

        @Test
        fun `should not offer a payment whose code the config does not give`() {
            // given
            val configs = listOf(
                OperatorUssdConfig(
                    operator = Operator.OrangeMoney,
                    merchantPayment = "#150*47*{code}*{amount}#",
                    transfer = null,
                ),
            )

            // when & then
            service.isAvailable(
                request = request(ReceivingMethod.PhoneNumber("+237690123456")),
                account = account,
                configs = configs,
            ).shouldBeFalse()
        }

        @Test
        fun `should not offer a payment with an operator without config`() {
            // when & then
            service.isAvailable(
                request = request(ReceivingMethod.MerchantCode("482913")),
                account = account,
                configs = emptyList(),
            ).shouldBeFalse()
        }
    }

    @Nested
    inner class Launch {
        @Test
        fun `should call the code on the SIM of the account`() = runTest {
            // when
            val outcome = service.launch(request = request(ReceivingMethod.MerchantCode("482913")), account = account)

            // then
            outcome shouldBe LaunchOutcome.Called
            calls shouldBe listOf("#150*47*482913*2500#" to 3)
        }

        @Test
        fun `should store the sent payment before calling`() = runTest {
            // when
            service.launch(request = request(ReceivingMethod.MerchantCode("482913")), account = account)

            // then
            sentPayments shouldBe listOf(
                PaymentService().newSent(
                    request = request(ReceivingMethod.MerchantCode("482913")),
                    operator = Operator.OrangeMoney,
                    at = now,
                ),
            )
        }

        @Test
        fun `should open the dialer without the phone calls permission`() = runTest {
            // given
            callsAllowed = false

            // when
            val outcome = service.launch(request = request(ReceivingMethod.MerchantCode("482913")), account = account)

            // then
            outcome shouldBe LaunchOutcome.DialerOpened
            dialed shouldBe listOf("#150*47*482913*2500#")
        }

        @Test
        fun `should never launch a session twice`() = runTest {
            // given
            service.launch(request = request(ReceivingMethod.MerchantCode("482913")), account = account)
            calls.clear()

            // when
            val outcome = service.launch(request = request(ReceivingMethod.MerchantCode("482913")), account = account)

            // then
            outcome shouldBe LaunchOutcome.AlreadyLaunched
            calls.shouldBeEmpty()
        }
    }
}
