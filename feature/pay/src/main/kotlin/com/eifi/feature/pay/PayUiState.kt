package com.eifi.feature.pay

import com.eifi.core.data.AppPermission

/** [activePermission] is being explained to the user; [outcome] is the result of the last action. */
internal data class PayUiState(val activePermission: AppPermission? = null, val outcome: PayOutcome? = null)
