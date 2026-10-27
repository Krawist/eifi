package com.eifi.feature.pay.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/** Opens the receipt when the payer comes back to Eifi after the USSD code was launched (operator's prompt closed). */
@Composable
internal fun ReceiptOnReturn(uiState: PaymentSummaryUiState.Ready, onShowReceipt: (String) -> Unit) {
    var leftAfterLaunch by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        if (uiState.launchOutcome != null) leftAfterLaunch = true
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (leftAfterLaunch) onShowReceipt(uiState.paymentId)
    }
}
