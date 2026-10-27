package com.eifi.feature.pay

import com.eifi.core.data.AppPermission

/**
 * State of the entry of the Pay mode.
 *
 * @property activePermission the permission being explained to the user, `null` when none is.
 * @property outcome the result of the last action, `null` before the first one.
 * @property openScanner set once the camera is allowed, so that the screen opens the scanner.
 */
internal data class PayUiState(
    val activePermission: AppPermission? = null,
    val outcome: PayOutcome? = null,
    val openScanner: Boolean = false,
)
