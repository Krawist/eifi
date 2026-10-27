package com.eifi.feature.onboarding

/** [completed] is set once the profile is saved, so that the screen only navigates away afterwards. */
internal data class NamesUiState(
    val personalName: String = "",
    val businessName: String = "",
    val completed: Boolean = false,
) {
    val canFinish: Boolean
        get() = personalName.isNotBlank() || businessName.isNotBlank()
}
