package com.eifi.feature.pay

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.component.PlaceholderScreen
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.permission.PermissionStep

/**
 * Entry of the Pay mode, shown before the scanner: the camera permission step the first time, or, without the camera,
 * an invitation to bring the phones together (NFC is still read).
 */
@Composable
internal fun PayScreen(
    uiState: PayUiState,
    onPermissionDone: (granted: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.activePermission != null ->
            PermissionStep(
                permission = uiState.activePermission,
                onDone = onPermissionDone,
                modifier = modifier,
                onBack = onBack,
            )
        uiState.cameraRefused -> PlaceholderScreen(
            title = stringResource(R.string.pay_title),
            subtitle = stringResource(R.string.pay_outcome_use_nfc),
            actionLabel = stringResource(R.string.pay_back),
            onAction = onBack,
            modifier = modifier,
        )
        else -> Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {}
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PayScreenPreview() {
    EifiTheme {
        PayScreen(uiState = PayUiState(cameraRefused = true), onPermissionDone = {}, onBack = {})
    }
}
