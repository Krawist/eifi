package com.eifi.core.ussd

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * [UssdLauncher] with [TelecomManager.placeCall] and the [PhoneAccountHandle] of the chosen SIM. The code goes in a
 * `tel:` URI, where `#` is encoded as `%23`.
 */
internal class TelecomUssdLauncher @Inject constructor(@ApplicationContext private val context: Context) :
    UssdLauncher {

    @SuppressLint("MissingPermission")
    override fun call(code: String, subscriptionId: Int?): Boolean {
        if (!granted(Manifest.permission.CALL_PHONE)) return false
        val extras = Bundle()
        subscriptionId?.let(::phoneAccount)?.let { extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it) }
        context.getSystemService(TelecomManager::class.java).placeCall(telUri(code), extras)
        return true
    }

    override fun dial(code: String) {
        context.startActivity(Intent(Intent.ACTION_DIAL, telUri(code)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** `Uri.fromParts` encodes `#` as `%23`, which a raw `tel:` string would cut. */
    private fun telUri(code: String): Uri = Uri.fromParts("tel", code, null)

    /** The phone account of [subscriptionId]: by subscription from Android 11, by id (subscription or ICCID) before. */
    @SuppressLint("MissingPermission")
    private fun phoneAccount(subscriptionId: Int): PhoneAccountHandle? {
        if (!granted(Manifest.permission.READ_PHONE_STATE)) return null
        val accounts = context.getSystemService(TelecomManager::class.java).callCapablePhoneAccounts
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val telephony = context.getSystemService(TelephonyManager::class.java)
            accounts.firstOrNull { telephony.getSubscriptionId(it) == subscriptionId }
        } else {
            val iccId = context.getSystemService(SubscriptionManager::class.java)
                ?.getActiveSubscriptionInfo(subscriptionId)?.iccId
            accounts.firstOrNull { it.id == subscriptionId.toString() || (iccId != null && it.id == iccId) }
        }
    }

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
