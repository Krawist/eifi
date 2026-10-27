package com.eifi.feature.pay.summary

import com.eifi.core.data.payment.PaymentRepository
import com.eifi.core.data.payment.PaymentService
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.ussd.OperatorUssdConfig
import com.eifi.core.ussd.UssdCodeService
import com.eifi.core.ussd.UssdConfigRepository
import com.eifi.core.ussd.UssdLauncher
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Launches the USSD code of a payment once per session, on the SIM of the payer's account. */
internal class PaymentLaunchService @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val paymentService: PaymentService,
    private val ussdConfigRepository: UssdConfigRepository,
    private val ussdLauncher: UssdLauncher,
    private val clock: Clock,
) {
    /** The USSD codes of the supported operators. */
    val ussdConfigs: Flow<List<OperatorUssdConfig>> = ussdConfigRepository.configs

    /** Whether [configs] give a USSD code for paying [request] with [account]. */
    fun isAvailable(request: PaymentRequest, account: PayingAccount, configs: List<OperatorUssdConfig>): Boolean =
        code(request = request, account = account, configs = configs) != null

    /**
     * Stores the sent payment, then calls its code, or opens the dialer without the phone calls permission; nothing
     * when the session was already launched.
     */
    suspend fun launch(request: PaymentRequest, account: PayingAccount): LaunchOutcome {
        val code =
            checkNotNull(code(request = request, account = account, configs = ussdConfigRepository.configs.first())) {
                "No USSD code for ${account.operator}"
            }
        return when {
            !paymentRepository.openSent(paymentService.newSent(request = request, at = clock.instant())) ->
                LaunchOutcome.AlreadyLaunched
            ussdLauncher.call(code = code, subscriptionId = account.sim?.subscriptionId) -> LaunchOutcome.Called
            else -> {
                ussdLauncher.dial(code)
                LaunchOutcome.DialerOpened
            }
        }
    }

    private fun code(request: PaymentRequest, account: PayingAccount, configs: List<OperatorUssdConfig>): String? =
        configs.firstOrNull { it.operator == account.operator }?.let {
            UssdCodeService.code(
                config = it,
                method = request.receivingMethods.getValue(account.operator),
                amount = request.amount,
            )
        }
}
