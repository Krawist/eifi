package com.eifi.feature.receive.payment

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.eifi.core.nfc.NfcPaymentPriority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Eifi first for the NFC tag while in the composition and resumed (see [NfcPaymentPriority]). The service is
 * enabled by the emission at about the same time, and the NFC stack takes a moment to register it: retried until
 * it is accepted.
 */
@Composable
internal fun NfcPriority() {
    val activity = LocalActivity.current ?: return
    val scope = rememberCoroutineScope()
    LifecycleResumeEffect(activity) {
        val attempts = scope.launch {
            repeat(MAX_ATTEMPTS) {
                if (NfcPaymentPriority.prefer(activity)) return@launch
                delay(RETRY_DELAY_MS)
            }
        }
        onPauseOrDispose {
            attempts.cancel()
            NfcPaymentPriority.release(activity)
        }
    }
}

private const val MAX_ATTEMPTS = 10
private const val RETRY_DELAY_MS = 200L
