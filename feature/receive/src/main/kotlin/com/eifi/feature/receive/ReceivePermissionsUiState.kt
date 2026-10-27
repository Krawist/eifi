package com.eifi.feature.receive

import com.eifi.core.data.AppPermission

internal sealed interface ReceivePermissionsUiState {
    data object Loading : ReceivePermissionsUiState

    data class Asking(val permission: AppPermission) : ReceivePermissionsUiState

    /** Every permission was granted, refused or already asked: the Receive mode opens. */
    data object Done : ReceivePermissionsUiState
}
