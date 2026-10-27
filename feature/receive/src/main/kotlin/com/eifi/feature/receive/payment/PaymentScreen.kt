package com.eifi.feature.receive.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.AmountDisplay
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiCloseButton
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.component.EifiPillTone
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.EifiQrCode
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.feature.receive.R

/**
 * "QR et NFC actifs": amount, QR code of the payment URL and countdown while the payment is pending; once
 * expired, an offer to start again with the same amount. NFC comes with EIFI-104.
 */
@Composable
internal fun PaymentScreen(
    uiState: PaymentUiState,
    onCancel: () -> Unit,
    onRestart: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                EifiCloseButton(onClick = onClose)
                if (uiState.status == PaymentStatus.Pending) {
                    EifiPill(
                        text = stringResource(R.string.payment_expires_in, countdown(uiState.remainingSeconds)),
                        tone = EifiPillTone.Accent,
                        icon = painterResource(DesignR.drawable.ic_eifi_timer),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.payment_amount_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AmountDisplay(amount = uiState.amount, modifier = Modifier.fillMaxWidth())
                    uiState.label?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                EifiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PaymentCardContent(uiState = uiState)
                    }
                }
                if (uiState.status == PaymentStatus.Pending) Waiting()
            }
            Actions(uiState = uiState, onCancel = onCancel, onRestart = onRestart, onClose = onClose)
        }
    }
}

/** The QR code while pending, at full brightness; what happened otherwise. */
@Composable
private fun PaymentCardContent(uiState: PaymentUiState) {
    when (uiState.status) {
        PaymentStatus.Pending -> if (uiState.url == null) {
            CircularProgressIndicator(color = EifiColors.Primary)
        } else {
            MaxBrightness()
            EifiQrCode(
                content = uiState.url,
                contentDescription = stringResource(R.string.payment_qr_description),
                modifier = Modifier.fillMaxWidth(),
            )
            CardText(text = stringResource(R.string.payment_scan_hint))
        }
        PaymentStatus.Expired -> {
            Text(text = stringResource(R.string.payment_expired), style = MaterialTheme.typography.titleLarge)
            CardText(text = stringResource(R.string.payment_expired_body))
        }
        // TODO(EIFI-106): the "Paiement reçu" screen.
        PaymentStatus.Confirmed -> EifiPill(text = stringResource(R.string.payment_confirmed))
        PaymentStatus.Cancelled -> Unit
    }
}

@Composable
private fun CardText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

// TODO(EIFI-105): "En attente du SMS de l'opérateur…" once the SMS are read.
@Composable
private fun Waiting() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(WAITING_INDICATOR_SIZE),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            strokeWidth = 2.dp,
        )
        Text(
            text = stringResource(R.string.payment_waiting),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Actions(uiState: PaymentUiState, onCancel: () -> Unit, onRestart: () -> Unit, onClose: () -> Unit) {
    Column(modifier = Modifier.padding(top = EifiSpacing.Large)) {
        when (uiState.status) {
            PaymentStatus.Pending -> EifiSecondaryButton(
                text = stringResource(R.string.payment_cancel),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
            )
            PaymentStatus.Expired -> {
                EifiPrimaryButton(text = stringResource(R.string.payment_restart), onClick = onRestart)
                EifiSecondaryButton(
                    text = stringResource(R.string.payment_back),
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            PaymentStatus.Confirmed -> EifiPrimaryButton(
                text = stringResource(R.string.payment_back),
                onClick = onClose,
            )
            PaymentStatus.Cancelled -> Unit
        }
    }
}

/** "1:42" */
private fun countdown(seconds: Long): String = "%d:%02d".format(
    seconds / SECONDS_PER_MINUTE,
    seconds % SECONDS_PER_MINUTE,
)

private const val SECONDS_PER_MINUTE = 60

private val WAITING_INDICATOR_SIZE = 18.dp

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentScreenPreview() {
    EifiTheme {
        PaymentScreen(
            uiState = PaymentUiState(
                amount = 2_500,
                label = null,
                status = PaymentStatus.Pending,
                remainingSeconds = 102,
                url = "https://pay.eifi.app/p#v1.CgsMDQ4PaSB2gHnEEwhCb3V0aXF1ZQERBkgpE",
            ),
            onCancel = {},
            onRestart = {},
            onClose = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentScreenExpiredPreview() {
    EifiTheme {
        PaymentScreen(
            uiState = PaymentUiState(
                amount = 2_500,
                label = null,
                status = PaymentStatus.Expired,
                remainingSeconds = 0,
            ),
            onCancel = {},
            onRestart = {},
            onClose = {},
        )
    }
}
