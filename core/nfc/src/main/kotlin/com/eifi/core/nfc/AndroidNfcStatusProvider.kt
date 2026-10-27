package com.eifi.core.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Reads [NfcAdapter.isEnabled] and listens to [NfcAdapter.ACTION_ADAPTER_STATE_CHANGED]. */
internal class AndroidNfcStatusProvider @Inject constructor(@ApplicationContext private val context: Context) :
    NfcStatusProvider {

    override val status: Flow<NfcStatus> = callbackFlow {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        if (adapter == null) {
            trySend(NfcStatus.Unavailable)
            awaitClose()
            return@callbackFlow
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(adapter.status)
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        trySend(adapter.status)
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    private val NfcAdapter.status: NfcStatus
        get() = if (isEnabled) NfcStatus.Ready else NfcStatus.Disabled
}
