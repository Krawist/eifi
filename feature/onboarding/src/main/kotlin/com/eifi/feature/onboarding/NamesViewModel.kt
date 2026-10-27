package com.eifi.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Last onboarding step: the personal and business names, prefilled when the onboarding is replayed. */
@HiltViewModel
internal class NamesViewModel @Inject constructor(private val userPreferencesRepository: UserPreferencesRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(NamesUiState())
    val uiState: StateFlow<NamesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.profile.first()?.let { profile ->
                _uiState.update {
                    it.copy(
                        personalName = profile.personalName.orEmpty(),
                        businessName = profile.businessName.orEmpty(),
                    )
                }
            }
        }
    }

    fun onPersonalNameChange(name: String) {
        _uiState.update { it.copy(personalName = name.take(UserProfile.MAX_NAME_LENGTH)) }
    }

    fun onBusinessNameChange(name: String) {
        _uiState.update { it.copy(businessName = name.take(UserProfile.MAX_NAME_LENGTH)) }
    }

    /** Saves the profile, which completes the onboarding. Does nothing without any name. */
    fun finish() {
        val state = _uiState.value
        if (!state.canFinish) return
        viewModelScope.launch {
            userPreferencesRepository.saveProfile(
                UserProfile(
                    personalName = state.personalName.trim().ifEmpty { null },
                    businessName = state.businessName.trim().ifEmpty { null },
                ),
            )
            _uiState.update { it.copy(completed = true) }
        }
    }
}
