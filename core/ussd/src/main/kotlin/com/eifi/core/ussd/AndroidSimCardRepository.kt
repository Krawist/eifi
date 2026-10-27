package com.eifi.core.ussd

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** [SimCardRepository] reading the active subscriptions of [SubscriptionManager]. */
internal class AndroidSimCardRepository @Inject constructor(@ApplicationContext private val context: Context) :
    SimCardRepository {

    override fun activeSimCards(): List<SimCard>? {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        val subscriptions = context.getSystemService(SubscriptionManager::class.java)?.activeSubscriptionInfoList
        return subscriptions.orEmpty().sortedBy { it.simSlotIndex }.map { it.toSimCard() }
    }

    private fun SubscriptionInfo.toSimCard() = SimCard(
        subscriptionId = subscriptionId,
        slotIndex = simSlotIndex,
        carrierName = carrierName?.toString().orEmpty(),
        operator = networkCode()?.let { (mcc, mnc) -> OperatorNetworkService.operatorOf(mcc = mcc, mnc = mnc) },
    )

    /** MCC and MNC, as written in the ITU list ("624", "02"); `null` when unknown. */
    @Suppress("DEPRECATION")
    private fun SubscriptionInfo.networkCode(): Pair<String, String>? = if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.Q
    ) {
        val mcc = mccString
        val mnc = mncString
        if (mcc != null && mnc != null) mcc to mnc else null
    } else {
        "%03d".format(mcc) to "%02d".format(mnc)
    }
}
