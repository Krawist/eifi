package com.eifi.core.nfc

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [NfcPaymentEmitter] through [PaymentTagService], which is only enabled while a URL is emitted: outside a payment,
 * the NDEF application is left to other apps.
 */
@Singleton
internal class HceNfcPaymentEmitter @Inject constructor(@ApplicationContext private val context: Context) :
    NfcPaymentEmitter {

    private var url: String? = null

    /** The tag read by [PaymentTagService], `null` when no URL is emitted. */
    @Volatile
    var tag: Type4Tag? = null
        private set

    @Synchronized
    override fun start(url: String) {
        if (url == this.url) return
        this.url = url
        tag = Type4Tag(ndefUriMessage(url))
        setServiceEnabled(true)
    }

    @Synchronized
    override fun stop(url: String) {
        if (url != this.url) return
        this.url = null
        tag = null
        setServiceEnabled(false)
    }

    private fun setServiceEnabled(enabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, PaymentTagService::class.java),
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP,
        )
    }
}
