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
 * Entry of the Pay mode: a payment can be read by NFC anywhere, so choosing "Payer" means scanning (EIFI-209). Opens
 * the scanner once the camera is allowed, explaining the permission the first time; refused, it invites the payer to
 * bring the phones together, while still reading NFC.
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

    init {
        viewModelScope.launch {
            val camera = AppPermission.Camera
            val state = when {
                permissionChecker.isGranted(camera) -> PayUiState(openScanner = true)
                permissionRequestHistory.wasRequested(camera) -> PayUiState(cameraRefused = true)
                else -> PayUiState(activePermission = camera)
            }
            _uiState.update { state }
        }
    }

    /** The camera permission was explained and asked. */
    fun onPermissionDone(granted: Boolean) {
        viewModelScope.launch {
            permissionRequestHistory.markRequested(AppPermission.Camera)
            _uiState.update { if (granted) PayUiState(openScanner = true) else PayUiState(cameraRefused = true) }
        }
    }

    /** The scanner has been opened: it does not open again. */
    fun onScannerOpened() {
        _uiState.update { it.copy(openScanner = false) }
    }
}
