package com.eifi.core.nfc

import android.app.Activity
import android.app.Application
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * [NfcPaymentReader] with [NfcAdapter.enableReaderMode] on the activity in the foreground: while it is collected,
 * tags go to Eifi instead of the system, and the first URI record of their NDEF message is emitted.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
internal class AndroidNfcPaymentReader @Inject constructor(@ApplicationContext context: Context) :
    NfcPaymentReader {

    /** Reader mode needs a resumed activity; `null` while the app is in the background. */
    private val resumedActivity = MutableStateFlow<Activity?>(null)

    init {
        (context as Application).registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    resumedActivity.value = activity
                }

                override fun onActivityPaused(activity: Activity) {
                    if (resumedActivity.value == activity) resumedActivity.value = null
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

                override fun onActivityStarted(activity: Activity) = Unit

                override fun onActivityStopped(activity: Activity) = Unit

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }

    override val paymentUrls: Flow<String> = resumedActivity.flatMapLatest { activity ->
        val adapter = activity?.let { NfcAdapter.getDefaultAdapter(it) }
        if (activity == null || adapter == null) emptyFlow() else readerMode(activity, adapter)
    }

    private fun readerMode(activity: Activity, adapter: NfcAdapter): Flow<String> = callbackFlow {
        adapter.enableReaderMode(
            activity,
            { tag -> uriOf(tag)?.let { trySend(it) } },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B,
            null,
        )
        awaitClose { adapter.disableReaderMode(activity) }
    }

    /** First URI record of the NDEF message of [tag], `null` when it has none or leaves the field too soon. */
    private fun uriOf(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.use {
                it.connect()
                it.ndefMessage?.records?.firstNotNullOfOrNull { record -> record.toUri()?.toString() }
            }
        } catch (_: IOException) {
            null
        }
    }
}
