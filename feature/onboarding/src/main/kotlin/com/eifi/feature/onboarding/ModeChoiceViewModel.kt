package com.eifi.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.AppMode
import com.eifi.core.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [selectedMode] is the highlighted card; [chosenMode] is set once the choice is saved, so that the screen
 * only navigates away afterward.
 */
internal data class ModeChoiceUiState(val selectedMode: AppMode = AppMode.Pay, val chosenMode: AppMode? = null)

@HiltViewModel
internal class ModeChoiceViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeChoiceUiState())
    val uiState: StateFlow<ModeChoiceUiState> = _uiState.asStateFlow()

    fun select(mode: AppMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    /** Remembers the selected mode as the mode the app opens on. */
    fun confirm() {
        val mode = _uiState.value.selectedMode
        viewModelScope.launch {
            userPreferencesRepository.setStartMode(mode)
            _uiState.update { it.copy(chosenMode = mode) }
        }
    }
}
