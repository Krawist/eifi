package com.eifi.feature.receive.payment

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
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.design.component.AmountDisplay
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.component.EifiPillTone
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.feature.receive.R

/** Received payment waiting to be paid; the QR code and NFC come with EIFI-103. */
@Composable
internal fun PaymentScreen(
    uiState: PaymentUiState,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(R.string.payment_title), style = MaterialTheme.typography.headlineMedium)
            AmountDisplay(amount = uiState.amount, modifier = Modifier.fillMaxWidth())
            uiState.label?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusPill(uiState = uiState)
            Text(
                text = stringResource(R.string.payment_qr_coming),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (uiState.status == PaymentStatus.Pending) {
                EifiSecondaryButton(text = stringResource(R.string.payment_cancel), onClick = onCancel)
            } else {
                EifiPrimaryButton(text = stringResource(R.string.payment_back), onClick = onBack)
            }
        }
    }
}

@Composable
private fun StatusPill(uiState: PaymentUiState) {
    when (uiState.status) {
        PaymentStatus.Pending -> EifiPill(
            text = stringResource(R.string.payment_waiting, countdown(uiState.remainingSeconds)),
            tone = EifiPillTone.Accent,
        )
        PaymentStatus.Expired -> EifiPill(text = stringResource(R.string.payment_expired), tone = EifiPillTone.Neutral)
        PaymentStatus.Confirmed -> EifiPill(text = stringResource(R.string.payment_confirmed))
        PaymentStatus.Cancelled -> Unit
    }
}

/** "1:42" */
private fun countdown(seconds: Long): String = "%d:%02d".format(
    seconds / SECONDS_PER_MINUTE,
    seconds % SECONDS_PER_MINUTE,
)

private const val SECONDS_PER_MINUTE = 60

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentScreenPreview() {
    EifiTheme {
        PaymentScreen(
            uiState = PaymentUiState(
                amount = 2_500,
                label = "Pain",
                status = PaymentStatus.Pending,
                remainingSeconds = 102,
            ),
            onCancel = {},
            onBack = {},
        )
    }
}
