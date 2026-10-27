package com.eifi.feature.pay

import com.eifi.core.data.AppPermission

/**
 * State of the Pay placeholder.
 *
 * @property activePermission the permission being explained to the user, `null` when none is.
 * @property outcome the result of the last action, `null` before the first one.
 */
internal data class PayUiState(val activePermission: AppPermission? = null, val outcome: PayOutcome? = null)
