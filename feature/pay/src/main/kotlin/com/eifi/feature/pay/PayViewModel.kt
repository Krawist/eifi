package com.eifi.feature.pay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.AppPermission
import com.eifi.core.data.PermissionRequestHistory
import com.eifi.core.nfc.NfcPaymentReader
import com.eifi.core.permission.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Entry of the Pay mode: reads a payment by NFC, or opens the scanner once the camera is allowed. Still asks for
 * the phone calls permission before paying (until EIFI-204). Each permission is asked once; after a refusal, the
 * alternative is offered directly.
 */
@HiltViewModel
internal class PayViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val permissionRequestHistory: PermissionRequestHistory,
    nfcPaymentReader: NfcPaymentReader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PayUiState())
    val uiState: StateFlow<PayUiState> = _uiState.asStateFlow()

    /** Payment URLs read by NFC while the screen is shown: each one opens its summary. */
    val paymentUrls: Flow<String> = nfcPaymentReader.paymentUrls

    fun onPay() = request(AppPermission.PhoneCalls)

    fun onScan() = request(AppPermission.Camera)

    fun onStepDone(permission: AppPermission, granted: Boolean) {
        viewModelScope.launch {
            permissionRequestHistory.markRequested(permission)
            _uiState.update { stateAfter(permission, granted) }
        }
    }

    /** The scanner has been opened: back on this screen, it does not open again. */
    fun onScannerOpened() {
        _uiState.update { it.copy(openScanner = false) }
    }

    private fun request(permission: AppPermission) {
        viewModelScope.launch {
            val state = when {
                permissionChecker.isGranted(permission) -> stateAfter(permission, granted = true)
                permissionRequestHistory.wasRequested(permission) -> stateAfter(permission, granted = false)
                else -> PayUiState(activePermission = permission)
            }
            _uiState.update { state }
        }
    }

    private fun stateAfter(permission: AppPermission, granted: Boolean): PayUiState = when (permission) {
        AppPermission.Camera -> if (granted) PayUiState(openScanner = true) else PayUiState(outcome = PayOutcome.UseNfc)
        else -> PayUiState(outcome = if (granted) PayOutcome.UssdReady else PayOutcome.DialYourself)
    }
}
