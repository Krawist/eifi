package com.eifi.feature.onboarding.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.ActivityRepository
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfileService
import com.eifi.core.nfc.NfcPaymentReader
import com.eifi.core.nfc.NfcStatusProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    activityRepository: ActivityRepository,
    nfcStatusProvider: NfcStatusProvider,
    nfcPaymentReader: NfcPaymentReader,
    userProfileService: UserProfileService,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        userPreferencesRepository.profile,
        nfcStatusProvider.status,
        activityRepository.recentActivities,
    ) { profile, nfcStatus, activity ->
        HomeUiState(
            greetingName = profile?.let(userProfileService::greetingName),
            nfcStatus = nfcStatus,
            recentActivity = activity,
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    /** Payment URLs read by NFC while the home screen is shown: each one starts the Pay flow. */
    val paymentUrls: Flow<String> = nfcPaymentReader.paymentUrls

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
