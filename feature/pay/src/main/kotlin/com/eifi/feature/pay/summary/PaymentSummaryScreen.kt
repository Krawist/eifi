package com.eifi.feature.pay.summary

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
import com.eifi.core.design.component.AmountDisplay
import com.eifi.core.design.component.EifiCloseButton
import com.eifi.core.design.component.PlaceholderScreen
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.feature.pay.R

/**
 * Where the three ways of reading a payment lead: the payee and the amount, or why the URL cannot be paid.
 * TODO(EIFI-201): the full summary (account, fees, countdown, "Payer" / "Envoyer").
 */
@Composable
internal fun PaymentSummaryScreen(uiState: PaymentSummaryUiState, onClose: () -> Unit, modifier: Modifier = Modifier) {
    when (uiState) {
        is PaymentSummaryUiState.Ready -> Summary(uiState = uiState, onClose = onClose, modifier = modifier)
        PaymentSummaryUiState.Expired -> Problem(
            title = R.string.summary_expired_title,
            body = R.string.summary_expired_body,
            onClose = onClose,
            modifier = modifier,
        )
        PaymentSummaryUiState.UpdateNeeded -> Problem(
            title = R.string.summary_update_title,
            body = R.string.summary_update_body,
            onClose = onClose,
            modifier = modifier,
        )
        PaymentSummaryUiState.Invalid -> Problem(
            title = R.string.summary_invalid_title,
            body = R.string.summary_invalid_body,
            onClose = onClose,
            modifier = modifier,
        )
    }
}

@Composable
private fun Summary(uiState: PaymentSummaryUiState.Ready, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            EifiCloseButton(onClick = onClose)
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.summary_pay_to),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = uiState.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                AmountDisplay(amount = uiState.amount, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun Problem(title: Int, body: Int, onClose: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(title),
        subtitle = stringResource(body),
        actionLabel = stringResource(R.string.summary_back),
        onAction = onClose,
        modifier = modifier,
    )
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentSummaryScreenPreview() {
    EifiTheme {
        PaymentSummaryScreen(
            uiState = PaymentSummaryUiState.Ready(displayName = "Kiosque Ndé", amount = 2_500),
            onClose = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentSummaryScreenExpiredPreview() {
    EifiTheme {
        PaymentSummaryScreen(uiState = PaymentSummaryUiState.Expired, onClose = {})
    }
}
