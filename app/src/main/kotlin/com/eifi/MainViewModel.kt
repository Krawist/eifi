package com.eifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/** Resolves once, at launch, which graph the app opens on; later preference changes do not move the user. */
@HiltViewModel
internal class MainViewModel @Inject constructor(userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    val uiState: StateFlow<MainUiState> = flow {
        emit(MainUiState.Ready(onboardingCompleted = userPreferencesRepository.profile.first() != null))
    }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = MainUiState.Loading)
}
