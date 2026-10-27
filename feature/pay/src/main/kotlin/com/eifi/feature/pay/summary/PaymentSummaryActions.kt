package com.eifi.feature.pay.summary

/**
 * What the payer can do on the summary.
 *
 * @property onPermissionDone the phone calls permission was explained and asked, with whether it is granted.
 * @property onChangeAccount opens "Choisissez votre compte".
 * @property onChooseAccount an account was chosen, with whether to remember it.
 * @property onCloseAccountChoice back from "Choisissez votre compte" without changing the account.
 * @property onConfirm confirms the payment.
 * @property onClose leaves the summary.
 */
internal data class PaymentSummaryActions(
    val onPermissionDone: (granted: Boolean) -> Unit = {},
    val onChangeAccount: () -> Unit = {},
    val onChooseAccount: (PayingAccount, remember: Boolean) -> Unit = { _, _ -> },
    val onCloseAccountChoice: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onClose: () -> Unit = {},
)
