package com.eifi.feature.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.AppPermission
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.permission.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Permissions of the Receive mode, asked once: notification access, then the battery optimisation exemption,
 * which is only useful once the notifications can be read. A refusal leaves the mode in manual confirmation.
 */
@HiltViewModel
internal class ReceivePermissionsViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val permissionRequestHistory: PermissionRequestHistory,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReceivePermissionsUiState>(ReceivePermissionsUiState.Loading)
    val uiState: StateFlow<ReceivePermissionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = if (permissionChecker.isGranted(AppPermission.NotificationAccess)) {
                batteryStep()
            } else {
                stepIfNotRequested(AppPermission.NotificationAccess)
            }
        }
    }

    fun onStepDone(permission: AppPermission, granted: Boolean) {
        viewModelScope.launch {
            permissionRequestHistory.markRequested(permission)
            _uiState.value = if (permission == AppPermission.NotificationAccess && granted) {
                batteryStep()
            } else {
                ReceivePermissionsUiState.Done
            }
        }
    }

    private suspend fun batteryStep(): ReceivePermissionsUiState =
        if (permissionChecker.isGranted(AppPermission.BatteryOptimisation)) {
            ReceivePermissionsUiState.Done
        } else {
            stepIfNotRequested(AppPermission.BatteryOptimisation)
        }

    private suspend fun stepIfNotRequested(permission: AppPermission): ReceivePermissionsUiState =
        if (permissionRequestHistory.wasRequested(permission)) {
            ReceivePermissionsUiState.Done
        } else {
            ReceivePermissionsUiState.Asking(permission)
        }
}
