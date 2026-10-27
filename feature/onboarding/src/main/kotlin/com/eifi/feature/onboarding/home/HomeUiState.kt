package com.eifi.feature.onboarding.home

import com.eifi.core.data.ActivityItem
import com.eifi.core.nfc.NfcStatus

/**
 * State of the home screen.
 *
 * @property greetingName name in the greeting, `null` while the profile loads.
 * @property nfcStatus NFC status of the phone, shown on the Pay card; `null` while it loads.
 * @property recentActivity last payments received and sent, most recent first.
 */
internal data class HomeUiState(
    val greetingName: String? = null,
    val nfcStatus: NfcStatus? = null,
    val recentActivity: List<ActivityItem> = emptyList(),
)
