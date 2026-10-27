package com.eifi

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eifi.core.design.theme.EifiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // On a recreation (rotation…), the link was already handled.
        if (savedInstanceState == null) handlePaymentLink(intent)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val paymentLink by viewModel.paymentLink.collectAsStateWithLifecycle()
            EifiTheme {
                EifiApp(
                    uiState = uiState,
                    paymentLink = paymentLink,
                    onPaymentLinkOpened = viewModel::onPaymentLinkOpened,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlePaymentLink(intent)
    }

    /** A payment URL from a link or from a tag read outside the app; see the intent filters of the manifest. */
    private fun handlePaymentLink(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW || intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            intent.dataString?.let(viewModel::onPaymentLink)
        }
    }
}
