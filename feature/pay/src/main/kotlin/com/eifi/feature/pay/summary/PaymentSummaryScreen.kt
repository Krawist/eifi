package com.eifi.feature.pay.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.AmountDisplay
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiCloseButton
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.component.EifiPillTone
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.PlaceholderScreen
import com.eifi.core.design.format.formatFcfa
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod
import com.eifi.feature.pay.R
import com.eifi.feature.pay.displayName

/**
 * "Récapitulatif": the payee, the amount, the payer's account, the fees and the confirmation, while the payee's
 * code is valid; otherwise why the URL cannot be paid.
 */
@Composable
internal fun PaymentSummaryScreen(
    uiState: PaymentSummaryUiState,
    onSelectOperator: (Operator) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PaymentSummaryUiState.Ready -> Summary(
            uiState = uiState,
            onSelectOperator = onSelectOperator,
            onConfirm = onConfirm,
            onClose = onClose,
            modifier = modifier,
        )
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
private fun Summary(
    uiState: PaymentSummaryUiState.Ready,
    onSelectOperator: (Operator) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val methodUi = ReceivingMethodUi.of(uiState.receivingMethod)
    val operatorName = stringResource(uiState.operator.displayName)
    var choosingAccount by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            TopBar(
                title = stringResource(methodUi.title),
                remainingSeconds = uiState.remainingSeconds,
                onClose = onClose,
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Payee(uiState = uiState, methodUi = methodUi)
                AccountCard(
                    label = stringResource(methodUi.accountLabel),
                    operator = uiState.operator,
                    onChange = if (uiState.operators.size > 1) ({ choosingAccount = true }) else null,
                )
                Fees(methodUi = methodUi, operatorName = operatorName)
                Note(
                    icon = DesignR.drawable.ic_eifi_shield,
                    text = stringResource(R.string.summary_pin_reminder, operatorName),
                )
            }
            if (uiState.confirmed) {
                Text(
                    text = stringResource(R.string.summary_confirm_soon),
                    modifier = Modifier.fillMaxWidth().padding(bottom = EifiSpacing.Medium),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            EifiPrimaryButton(
                text = stringResource(methodUi.action, formatFcfa(uiState.amount)),
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    if (choosingAccount) {
        AccountSheet(
            operators = uiState.operators,
            selected = uiState.operator,
            onSelect = {
                onSelectOperator(it)
                choosingAccount = false
            },
            onDismiss = { choosingAccount = false },
        )
    }
}

@Composable
private fun TopBar(title: String, remainingSeconds: Long, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        EifiCloseButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        EifiPill(
            text = stringResource(R.string.summary_expires_in, countdown(remainingSeconds)),
            modifier = Modifier.align(Alignment.CenterEnd),
            tone = EifiPillTone.Accent,
            icon = painterResource(DesignR.drawable.ic_eifi_timer),
        )
    }
}

@Composable
private fun Payee(uiState: PaymentSummaryUiState.Ready, methodUi: ReceivingMethodUi) {
    Column(
        verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EifiIconTile(
            icon = painterResource(methodUi.icon),
            containerColor = methodUi.iconColor,
            size = EifiDimens.IconTileHero,
        )
        Text(
            text = uiState.displayName,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        EifiPill(text = stringResource(methodUi.badge), tone = EifiPillTone.Neutral)
        AmountDisplay(amount = uiState.amount, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Fees(methodUi: ReceivingMethodUi, operatorName: String) {
    EifiCard(
        modifier = Modifier.fillMaxWidth(),
        color = methodUi.feesColor,
        contentPadding = PaddingValues(EifiSpacing.Large),
    ) {
        Text(text = stringResource(methodUi.fees), style = MaterialTheme.typography.titleMedium)
        methodUi.feesDetail?.let {
            Text(
                text = stringResource(it, operatorName),
                modifier = Modifier.padding(top = EifiSpacing.XSmall),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun Note(icon: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium)) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(EifiDimens.PointIcon),
            tint = EifiColors.Muted,
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = EifiColors.Muted)
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

private fun countdown(seconds: Long): String = "%d:%02d".format(
    seconds / SECONDS_PER_MINUTE,
    seconds % SECONDS_PER_MINUTE,
)

private const val SECONDS_PER_MINUTE = 60

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentSummaryScreenPreview() {
    EifiTheme {
        PaymentSummaryScreen(
            uiState = PaymentSummaryUiState.Ready(
                displayName = "Kiosque Ndé",
                amount = 2_500,
                operators = listOf(Operator.OrangeMoney, Operator.MtnMomo),
                operator = Operator.OrangeMoney,
                receivingMethod = ReceivingMethod.MerchantCode("482913"),
                remainingSeconds = 102,
            ),
            onSelectOperator = {},
            onConfirm = {},
            onClose = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PaymentSummaryScreenTransferPreview() {
    EifiTheme {
        PaymentSummaryScreen(
            uiState = PaymentSummaryUiState.Ready(
                displayName = "Paul",
                amount = 2_500,
                operators = listOf(Operator.OrangeMoney),
                operator = Operator.OrangeMoney,
                receivingMethod = ReceivingMethod.PhoneNumber("+237690123456"),
                remainingSeconds = 102,
            ),
            onSelectOperator = {},
            onConfirm = {},
            onClose = {},
        )
    }
}
