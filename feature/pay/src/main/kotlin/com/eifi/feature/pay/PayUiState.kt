package com.eifi.feature.pay

import com.eifi.core.data.AppPermission

/**
 * State of the entry of the Pay mode, which opens the scanner (EIFI-209).
 *
 * @property activePermission the camera permission while it is explained, `null` otherwise.
 * @property cameraRefused whether the camera is refused, which invites the payer to bring the phones together.
 * @property openScanner set once the camera is allowed, so that the screen opens the scanner.
 */
internal data class PayUiState(
    val activePermission: AppPermission? = null,
    val cameraRefused: Boolean = false,
    val openScanner: Boolean = false,
)
