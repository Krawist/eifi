package com.eifi.feature.onboarding.home

/**
 * What the home graph opens outside its feature, wired by the app.
 *
 * @property onPay opens the Pay mode.
 * @property onPaymentUrl opens the summary of a payment read by NFC, with its URL.
 * @property onReceive opens the Receive mode.
 * @property onOpenReceipt opens the receipt of a sent payment, with its id.
 * @property onEditReceivingProfile opens the receiving profile.
 */
data class HomeGraphActions(
    val onPay: () -> Unit,
    val onPaymentUrl: (url: String) -> Unit,
    val onReceive: () -> Unit,
    val onOpenReceipt: (paymentId: String) -> Unit,
    val onEditReceivingProfile: () -> Unit,
)
