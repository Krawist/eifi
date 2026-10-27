package com.eifi.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.UserPreferencesRepository
import com.eifi.core.data.UserProfile
import com.eifi.core.data.UserProfileService
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
internal class NamesViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userProfileService: UserProfileService,
) : ViewModel() {

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
        _uiState.update { it.copy(personalName = name.take(UserProfileService.MAX_NAME_LENGTH)) }
    }

    fun onBusinessNameChange(name: String) {
        _uiState.update { it.copy(businessName = name.take(UserProfileService.MAX_NAME_LENGTH)) }
    }

    /** Saves the profile, which completes the onboarding; without any name, shows why it cannot. */
    fun finish() {
        val state = _uiState.value
        if (!state.canFinish) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        val profile = UserProfile(
            personalName = state.personalName.trim().ifEmpty { null },
            businessName = state.businessName.trim().ifEmpty { null },
        )
        userProfileService.validate(profile)
        viewModelScope.launch {
            userPreferencesRepository.saveProfile(profile)
            _uiState.update { it.copy(completed = true) }
        }
    }
}
