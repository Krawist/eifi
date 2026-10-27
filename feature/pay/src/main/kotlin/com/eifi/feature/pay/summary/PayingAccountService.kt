package com.eifi.feature.pay.summary

import com.eifi.core.data.AppPermission
import com.eifi.core.data.PayingAccountChoice
import com.eifi.core.data.PayingAccountRepository
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.payload.Operator
import com.eifi.core.permission.PermissionChecker
import com.eifi.core.ussd.SimCard
import com.eifi.core.ussd.SimCardRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * The accounts the payer can pay a payee with, and the one offered first. A payment only works between two accounts
 * of the same operator, so each account is a SIM of an operator the payee accepts.
 */
internal class PayingAccountService @Inject constructor(
    private val simCardRepository: SimCardRepository,
    private val payingAccountRepository: PayingAccountRepository,
    private val permissionChecker: PermissionChecker,
    private val permissionRequestHistory: PermissionRequestHistory,
) {
    /** The remembered account, `null` until one is. */
    val choice: Flow<PayingAccountChoice?> = payingAccountRepository.choice

    /** Whether to ask for the phone calls permission, needed to list the SIMs: once, if it is not granted. */
    suspend fun shouldAskPhoneCalls(): Boolean = !permissionChecker.isGranted(AppPermission.PhoneCalls) &&
        !permissionRequestHistory.wasRequested(AppPermission.PhoneCalls)

    /** The phone calls permission was asked: it is not asked again. */
    suspend fun onPhoneCallsAsked() {
        permissionRequestHistory.markRequested(AppPermission.PhoneCalls)
    }

    /** The SIMs of the phone, `null` without the phone calls permission. */
    fun simCards(): List<SimCard>? = simCardRepository.activeSimCards()

    /**
     * For each operator of [payeeOperators], in their order, the SIMs of that operator; without [sims], one account
     * per operator, as the payer's SIMs are not known.
     */
    fun accounts(payeeOperators: Collection<Operator>, sims: List<SimCard>?): List<PayingAccount> = if (sims == null) {
        payeeOperators.map { PayingAccount(operator = it, sim = null) }
    } else {
        payeeOperators.flatMap { operator ->
            sims.filter { it.operator == operator }.map { PayingAccount(operator = operator, sim = it) }
        }
    }

    /**
     * The account of [choice] among [accounts], else another SIM of its operator (e.g. after a SIM change), else the
     * first account; `null` when there is none.
     */
    fun defaultAccount(accounts: List<PayingAccount>, choice: PayingAccountChoice?): PayingAccount? =
        accounts.firstOrNull { it.operator == choice?.operator && it.sim?.subscriptionId == choice.subscriptionId }
            ?: accounts.firstOrNull { it.operator == choice?.operator }
            ?: accounts.firstOrNull()

    /** Offers [account] first on the next payments. */
    suspend fun remember(account: PayingAccount) {
        payingAccountRepository.saveChoice(
            PayingAccountChoice(operator = account.operator, subscriptionId = account.sim?.subscriptionId),
        )
    }
}
