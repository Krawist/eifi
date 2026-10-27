package com.eifi.core.nfc

import android.app.Activity
import android.content.ComponentName
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation

/**
 * Gives the payment tag priority over the other apps that emulate an NDEF tag on the same AID (e.g. Samsung's
 * built-in tag), so that Android neither asks which app to use nor reports a conflict. Only holds while the
 * activity is resumed: call [prefer] once resumed and [release] before it pauses.
 */
object NfcPaymentPriority {
    /** `false` when NFC is missing or the service is not registered yet (it is enabled with the emission). */
    fun prefer(activity: Activity): Boolean {
        val cardEmulation = cardEmulation(activity) ?: return false
        return cardEmulation.setPreferredService(activity, ComponentName(activity, PaymentTagService::class.java))
    }

    fun release(activity: Activity) {
        cardEmulation(activity)?.unsetPreferredService(activity)
    }

    private fun cardEmulation(activity: Activity): CardEmulation? =
        NfcAdapter.getDefaultAdapter(activity)?.let(CardEmulation::getInstance)
}
