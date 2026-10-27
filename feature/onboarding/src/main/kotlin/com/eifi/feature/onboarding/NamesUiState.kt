package com.eifi.feature.onboarding

/**
 * [showErrors] is set when the user tried to finish without any name, so that the screen says why;
 * [completed] is set once the profile is saved, so that the screen only navigates away afterwards.
 */
internal data class NamesUiState(
    val personalName: String = "",
    val businessName: String = "",
    val showErrors: Boolean = false,
    val completed: Boolean = false,
) {
    val canFinish: Boolean
        get() = personalName.isNotBlank() || businessName.isNotBlank()
}
