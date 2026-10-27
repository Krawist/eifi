package com.eifi.feature.pay

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.design.theme.EifiTransitions
import com.eifi.core.permission.PermissionStep

/** Entry of the Pay mode: tap a receiving phone or scan its code, with the permission steps (EIFI-032). */
@Composable
internal fun PayScreen(
    uiState: PayUiState,
    onPay: () -> Unit,
    onScan: () -> Unit,
    onPermissionDone: (granted: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = uiState.activePermission,
        transitionSpec = { EifiTransitions.step(forward = targetState != null) },
        label = "pay",
        modifier = modifier,
    ) { permission ->
        if (permission != null) {
            PermissionStep(permission = permission, onDone = onPermissionDone)
        } else {
            PayContent(uiState = uiState, onPay = onPay, onScan = onScan, onBack = onBack)
        }
    }
}

@Composable
private fun PayContent(uiState: PayUiState, onPay: () -> Unit, onScan: () -> Unit, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(R.string.pay_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(R.string.pay_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            uiState.outcome?.let { outcome ->
                Text(
                    text = stringResource(outcome.message),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = EifiSpacing.Small),
                )
            }
            EifiPrimaryButton(text = stringResource(R.string.pay_action_pay), onClick = onPay)
            EifiPrimaryButton(text = stringResource(R.string.pay_action_scan), onClick = onScan)
            EifiSecondaryButton(
                text = stringResource(R.string.pay_back),
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val PayOutcome.message: Int
    get() = when (this) {
        PayOutcome.UssdReady -> R.string.pay_outcome_ussd_ready
        PayOutcome.DialYourself -> R.string.pay_outcome_dial_yourself
        PayOutcome.UseNfc -> R.string.pay_outcome_use_nfc
    }

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PayScreenPreview() {
    EifiTheme {
        PayScreen(
            uiState = PayUiState(outcome = PayOutcome.DialYourself),
            onPay = {},
            onScan = {},
            onPermissionDone = {},
            onBack = {},
        )
    }
}
