package com.eifi.feature.onboarding

/**
 * State of the names screen, the last onboarding step.
 *
 * @property personalName personal name, as typed.
 * @property businessName business name, as typed.
 * @property showErrors set when the user tried to finish without any name, so that the screen says why.
 * @property completed set once the profile is saved, so that the screen only navigates away afterwards.
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
