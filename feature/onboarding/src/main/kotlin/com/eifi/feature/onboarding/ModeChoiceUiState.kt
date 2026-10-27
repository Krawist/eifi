package com.eifi.feature.onboarding

import com.eifi.core.data.AppMode

/**
 * State of the mode choice screen.
 *
 * @property selectedMode the highlighted card, opened at the end of the onboarding.
 */
internal data class ModeChoiceUiState(val selectedMode: AppMode = AppMode.Pay)
