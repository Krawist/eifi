package com.eifi.feature.pay

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pay placeholder: asks for the phone calls permission before paying and the camera before scanning, once.
 * After a refusal, the alternative is offered directly.
 */
@HiltViewModel
internal class PayViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val permissionRequestHistory: PermissionRequestHistory,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PayUiState())
    val uiState: StateFlow<PayUiState> = _uiState.asStateFlow()

    fun onPay() = request(AppPermission.PhoneCalls)

    fun onScan() = request(AppPermission.Camera)

    fun onStepDone(permission: AppPermission, granted: Boolean) {
        viewModelScope.launch {
            permissionRequestHistory.markRequested(permission)
            _uiState.update { PayUiState(outcome = outcomeOf(permission, granted)) }
        }
    }

    private fun request(permission: AppPermission) {
        viewModelScope.launch {
            val state = when {
                permissionChecker.isGranted(permission) -> PayUiState(outcome = outcomeOf(permission, granted = true))
                permissionRequestHistory.wasRequested(permission) ->
                    PayUiState(outcome = outcomeOf(permission, granted = false))
                else -> PayUiState(activePermission = permission)
            }
            _uiState.update { state }
        }
    }

    private fun outcomeOf(permission: AppPermission, granted: Boolean): PayOutcome = when (permission) {
        AppPermission.Camera -> if (granted) PayOutcome.ScannerReady else PayOutcome.UseNfc
        else -> if (granted) PayOutcome.UssdReady else PayOutcome.DialYourself
    }
}
