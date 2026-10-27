package com.eifi.feature.pay.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.AmountDisplay
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiDarkButton
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.payload.Operator
import com.eifi.feature.pay.R
import com.eifi.feature.pay.displayName
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The payer's receipt: "Demande envoyée" until the debit SMS confirms it ("Paiement effectué", EIFI-206). Its
 * layout (receipt card, dark button) differs from the payee's "Paiement reçu", so a screenshot cannot pass for it.
 * [receiptModifier] marks the part shared as an image.
 */
@Composable
internal fun ReceiptScreen(
    uiState: ReceiptUiState,
    onDone: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    receiptModifier: Modifier = Modifier,
) {
    val operatorName = uiState.operator?.let { stringResource(it.displayName) }
    val payeeName = uiState.payeeName.orEmpty()
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .then(receiptModifier)
                    .background(EifiColors.Background)
                    .padding(vertical = EifiSpacing.Large),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large),
            ) {
                Header(debitConfirmed = uiState.debitConfirmed)
                ReceiptCard(uiState = uiState, operatorName = operatorName)
                operatorName?.let {
                    StatusLine(text = stringResource(R.string.receipt_request_sent, it), done = true)
                    if (uiState.debitConfirmed) {
                        StatusLine(text = stringResource(R.string.receipt_debit_confirmed, it), done = true)
                    } else {
                        StatusLine(text = stringResource(R.string.receipt_check_sms, it), done = false)
                    }
                }
                Text(
                    text = stringResource(R.string.receipt_payee_confirmation, payeeName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EifiColors.Muted,
                )
            }
            EifiDarkButton(text = stringResource(R.string.receipt_done), onClick = onDone)
            EifiSecondaryButton(
                text = stringResource(R.string.receipt_share),
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Header(debitConfirmed: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EifiIconTile(
            icon = painterResource(DesignR.drawable.ic_eifi_receipt),
            containerColor = EifiColors.Soft,
            size = EifiDimens.IconTileHero,
        )
        Text(
            text = stringResource(
                if (debitConfirmed) R.string.receipt_title_confirmed else R.string.receipt_title_sent,
            ),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.receipt_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = EifiColors.Muted,
        )
    }
}

@Composable
private fun ReceiptCard(uiState: ReceiptUiState, operatorName: String?) {
    EifiCard(modifier = Modifier.fillMaxWidth(), shape = EifiRadius.CardLarge) {
        Text(
            text = stringResource(
                if (uiState.debitConfirmed) R.string.receipt_paid_to else R.string.receipt_to,
                uiState.payeeName.orEmpty(),
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = EifiColors.Muted,
        )
        AmountDisplay(amount = uiState.amount)
        HorizontalDivider(modifier = Modifier.padding(vertical = EifiSpacing.Large), color = EifiColors.Divider)
        Column(verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium)) {
            operatorName?.let { DetailRow(label = stringResource(R.string.receipt_account), value = it) }
            DetailRow(
                label = stringResource(R.string.receipt_date),
                value = DateTimeFormatter.ofPattern(stringResource(R.string.receipt_date_pattern))
                    .format(uiState.sentAt.atZone(ZoneId.systemDefault())),
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = EifiColors.Muted)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

/** A step of the payment: done (check on the primary colour), or what the payer should check. */
@Composable
private fun StatusLine(text: String, done: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = if (done) EifiColors.Primary else EifiColors.AccentSoft,
            contentColor = if (done) EifiColors.OnPrimary else EifiColors.OnAccent,
            modifier = Modifier.size(EifiDimens.SelectionBadge),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(
                        if (done) DesignR.drawable.ic_eifi_check else DesignR.drawable.ic_eifi_message,
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(EifiDimens.PillIcon),
                )
            }
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ReceiptScreenPreview() {
    EifiTheme {
        ReceiptScreen(
            uiState = ReceiptUiState(
                payeeName = "Kiosque Ndé",
                amount = 2_500,
                operator = Operator.OrangeMoney,
                sentAt = Instant.parse("2026-10-27T13:32:00Z"),
                debitConfirmed = false,
            ),
            onDone = {},
            onShare = {},
        )
    }
}
