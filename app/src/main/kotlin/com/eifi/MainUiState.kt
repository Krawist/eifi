package com.eifi

internal sealed interface MainUiState {
    data object Loading : MainUiState

    data class Ready(val onboardingCompleted: Boolean) : MainUiState
}
