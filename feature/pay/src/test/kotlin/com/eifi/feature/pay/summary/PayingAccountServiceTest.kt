package com.eifi.feature.pay.summary

import com.eifi.core.data.AppPermission
import com.eifi.core.data.PayingAccountChoice
import com.eifi.core.data.PayingAccountRepository
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.payload.Operator
import com.eifi.core.permission.PermissionChecker
import com.eifi.core.ussd.SimCard
import com.eifi.core.ussd.SimCardRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PayingAccountServiceTest {
    private val service = PayingAccountService(
        simCardRepository = object : SimCardRepository {
            override fun activeSimCards(): List<SimCard>? = null
        },
        payingAccountRepository = object : PayingAccountRepository {
            override val choice = MutableStateFlow<PayingAccountChoice?>(null)

            override suspend fun saveChoice(choice: PayingAccountChoice) = Unit
        },
        permissionChecker = object : PermissionChecker {
            override fun isGranted(permission: AppPermission) = true
        },
        permissionRequestHistory = object : PermissionRequestHistory {
            override suspend fun wasRequested(permission: AppPermission) = false

            override suspend fun markRequested(permission: AppPermission) = Unit
        },
    )

    @Nested
    inner class Accounts {
        @Test
        fun `should offer each SIM of an operator the payee accepts`() {
            // given
            val orange =
                SimCard(subscriptionId = 1, slotIndex = 0, carrierName = "Orange CM", operator = Operator.OrangeMoney)
            val mtn = SimCard(subscriptionId = 2, slotIndex = 1, carrierName = "MTN CM", operator = Operator.MtnMomo)

            // when
            val accounts = service.accounts(payeeOperators = listOf(Operator.MtnMomo), sims = listOf(orange, mtn))

            // then
            accounts shouldBe listOf(PayingAccount(operator = Operator.MtnMomo, sim = mtn))
        }

        @Test
        fun `should offer no account when no SIM is with an operator the payee accepts`() {
            // given
            val other = SimCard(subscriptionId = 1, slotIndex = 0, carrierName = "Nexttel", operator = null)

            // when
            val accounts = service.accounts(payeeOperators = listOf(Operator.OrangeMoney), sims = listOf(other))

            // then
            accounts.shouldBeEmpty()
        }

        @Test
        fun `should offer every operator of the payee when the SIMs are not known`() {
            // when
            val accounts = service.accounts(
                payeeOperators = listOf(Operator.OrangeMoney, Operator.MtnMomo),
                sims = null,
            )

            // then
            accounts shouldBe listOf(
                PayingAccount(operator = Operator.OrangeMoney, sim = null),
                PayingAccount(operator = Operator.MtnMomo, sim = null),
            )
        }
    }

    @Nested
    inner class DefaultAccount {
        @Test
        fun `should offer the remembered account`() {
            // given
            val accounts = listOf(
                PayingAccount(
                    operator = Operator.OrangeMoney,
                    sim = sim(subscriptionId = 1, operator = Operator.OrangeMoney),
                ),
                PayingAccount(
                    operator = Operator.OrangeMoney,
                    sim = sim(subscriptionId = 2, operator = Operator.OrangeMoney),
                ),
            )

            // when
            val account = service.defaultAccount(
                accounts = accounts,
                choice = PayingAccountChoice(operator = Operator.OrangeMoney, subscriptionId = 2),
            )

            // then
            account shouldBe accounts[1]
        }

        @Test
        fun `should offer another SIM of the remembered operator when its SIM is gone`() {
            // given
            val accounts = listOf(
                PayingAccount(
                    operator = Operator.OrangeMoney,
                    sim = sim(subscriptionId = 1, operator = Operator.OrangeMoney),
                ),
                PayingAccount(operator = Operator.MtnMomo, sim = sim(subscriptionId = 2, operator = Operator.MtnMomo)),
            )

            // when
            val account = service.defaultAccount(
                accounts = accounts,
                choice = PayingAccountChoice(operator = Operator.MtnMomo, subscriptionId = 7),
            )

            // then
            account shouldBe accounts[1]
        }

        @Test
        fun `should offer the first account without remembered one`() {
            // given
            val accounts = listOf(
                PayingAccount(operator = Operator.OrangeMoney, sim = null),
                PayingAccount(operator = Operator.MtnMomo, sim = null),
            )

            // when
            val account = service.defaultAccount(accounts = accounts, choice = null)

            // then
            account shouldBe accounts[0]
        }

        @Test
        fun `should offer nothing without compatible account`() {
            // when
            val account = service.defaultAccount(
                accounts = emptyList(),
                choice = PayingAccountChoice(operator = Operator.MtnMomo, subscriptionId = null),
            )

            // then
            account.shouldBeNull()
        }
    }

    /** Helper without side effect: a SIM in the first slot. */
    private fun sim(subscriptionId: Int, operator: Operator) =
        SimCard(subscriptionId = subscriptionId, slotIndex = 0, carrierName = "", operator = operator)
}
