package com.eifi.feature.pay.summary

import com.eifi.core.data.AppPermission
import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod

/** What the payment URL gave: the payment to make, or why there is none. */
internal sealed interface PaymentSummaryUiState {
    /**
     * A payment that can be made, until its countdown ends.
     *
     * @property displayName name of the payee.
     * @property amount amount to pay, in FCFA.
     * @property payeeOperators the operators the payee accepts.
     * @property accounts the payer's accounts compatible with the payee, see [PayingAccountService.accounts].
     * @property account the account to pay with: the chosen one, else the remembered one, else the first one;
     * `null` when no account is compatible, which blocks the payment.
     * @property receivingMethod where the money goes with [account], or with the payee's first operator when there
     * is no account: decides between "Payer" and "Envoyer".
     * @property ussdAvailable whether the USSD code of [account] is known; otherwise the payment is not offered.
     * @property remainingSeconds time left before the payee's code expires.
     * @property activePermission the permission being explained, `null` when none is.
     * @property choosingAccount whether the "Choisissez votre compte" screen shows.
     * @property rememberChoice whether the account is offered first on the next payments.
     * @property confirmationStep where the confirmation stands: biometric prompt, missing screen lock, authenticated.
     * @property launchOutcome what launching the USSD code did, `null` until it is launched.
     */
    data class Ready(
        val displayName: String,
        val amount: Long,
        val payeeOperators: List<Operator>,
        val accounts: List<PayingAccount>,
        val account: PayingAccount?,
        val receivingMethod: ReceivingMethod,
        val ussdAvailable: Boolean,
        val remainingSeconds: Long,
        val activePermission: AppPermission? = null,
        val choosingAccount: Boolean = false,
        val rememberChoice: Boolean = true,
        val confirmationStep: ConfirmationStep = ConfirmationStep.None,
        val launchOutcome: LaunchOutcome? = null,
    ) : PaymentSummaryUiState

    /** The payee's code has expired, before or while the summary shows: they must show a new one. */
    data object Expired : PaymentSummaryUiState

    /** The URL comes from a newer version of Eifi: the payer should update the app. */
    data object UpdateNeeded : PaymentSummaryUiState

    /** Not an Eifi payment, or damaged. */
    data object Invalid : PaymentSummaryUiState
}
