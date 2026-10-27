package com.eifi.feature.pay.summary

/**
 * What the payer can do on the summary.
 *
 * @property onPermissionDone the phone calls permission was explained and asked, with whether it is granted.
 * @property onChangeAccount opens "Choisissez votre compte".
 * @property onChooseAccount an account was chosen, with whether to remember it.
 * @property onCloseAccountChoice back from "Choisissez votre compte" without changing the account.
 * @property onConfirm confirms the payment, through the biometric prompt.
 * @property onAuthenticated the payer passed the biometric prompt.
 * @property onAuthenticationCancelled the payer cancelled the biometric prompt, or it failed.
 * @property onScreenLockMissing the phone has no screen lock for the prompt.
 * @property onCloseScreenLockNeeded back from the screen lock explanation.
 * @property onShowReceipt opens the receipt of the launched payment, with its id.
 * @property onClose leaves the summary.
 */
internal data class PaymentSummaryActions(
    val onPermissionDone: (granted: Boolean) -> Unit = {},
    val onChangeAccount: () -> Unit = {},
    val onChooseAccount: (PayingAccount, remember: Boolean) -> Unit = { _, _ -> },
    val onCloseAccountChoice: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onAuthenticated: () -> Unit = {},
    val onAuthenticationCancelled: () -> Unit = {},
    val onScreenLockMissing: () -> Unit = {},
    val onCloseScreenLockNeeded: () -> Unit = {},
    val onShowReceipt: (paymentId: String) -> Unit = {},
    val onClose: () -> Unit = {},
)
