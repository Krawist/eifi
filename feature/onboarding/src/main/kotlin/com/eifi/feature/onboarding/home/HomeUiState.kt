package com.eifi.feature.onboarding.home

import com.eifi.core.data.ActivityItem
import com.eifi.core.nfc.NfcStatus

internal data class HomeUiState(
    val greetingName: String? = null,
    val nfcStatus: NfcStatus? = null,
    val recentActivity: List<ActivityItem> = emptyList(),
)
