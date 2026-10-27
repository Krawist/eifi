package com.eifi.feature.onboarding

import androidx.lifecycle.ViewModel
import com.eifi.core.data.AppMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
internal class ModeChoiceViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ModeChoiceUiState())
    val uiState: StateFlow<ModeChoiceUiState> = _uiState.asStateFlow()

    fun select(mode: AppMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }
}
