package com.eifi.feature.pay.summary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiBackButton
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod
import com.eifi.core.ussd.SimCard
import com.eifi.feature.pay.R
import com.eifi.feature.pay.displayName

/**
 * "Choisissez votre compte": the payer's accounts compatible with the payee, each with its SIM, and whether to
 * remember the choice.
 */
@Composable
internal fun AccountChoiceScreen(
    uiState: PaymentSummaryUiState.Ready,
    onChoose: (PayingAccount, remember: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accounts = uiState.accounts
    val displayName = uiState.displayName
    var selectedIndex by rememberSaveable { mutableIntStateOf(accounts.indexOf(uiState.account).coerceAtLeast(0)) }
    var remember by rememberSaveable { mutableStateOf(uiState.rememberChoice) }
    BackHandler(onBack = onBack)
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                EifiBackButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart))
                Text(
                    text = stringResource(ReceivingMethodUi.of(uiState.receivingMethod).accountLabel),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(top = EifiSpacing.XLarge),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
            ) {
                Text(
                    text = stringResource(R.string.account_choice_title),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = stringResource(R.string.account_choice_subtitle, displayName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = EifiColors.Muted,
                )
                accounts.forEachIndexed { index, account ->
                    AccountOption(account = account, selected = index == selectedIndex, onClick = {
                        selectedIndex =
                            index
                    })
                }
                RememberRow(checked = remember, onCheckedChange = { remember = it })
                UssdNote(displayName = displayName)
            }
            EifiPrimaryButton(
                text = stringResource(R.string.account_choice_continue),
                onClick = { accounts.getOrNull(selectedIndex)?.let { onChoose(it, remember) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** How the payment goes through, on the chosen SIM. */
@Composable
private fun UssdNote(displayName: String) {
    EifiCard(
        modifier = Modifier.fillMaxWidth(),
        color = EifiColors.Soft,
        contentPadding = PaddingValues(EifiSpacing.Large),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium)) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_eifi_shield),
                contentDescription = null,
                modifier = Modifier.size(EifiDimens.PointIcon),
            )
            Text(
                text = stringResource(R.string.account_choice_note, displayName),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AccountOption(account: PayingAccount, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = EifiRadius.Card,
        color = EifiColors.Surface,
        border = if (selected) BorderStroke(EifiDimens.SelectionBorder, EifiColors.Primary) else null,
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(EifiSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OperatorBadge(operator = account.operator)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(account.operator.displayName), style = MaterialTheme.typography.titleMedium)
                account.sim?.let {
                    Text(
                        text = stringResource(R.string.account_sim, it.slotIndex + 1, it.carrierName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = EifiColors.Muted,
                    )
                }
            }
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = EifiColors.OnPrimary),
            )
        }
    }
}

@Composable
private fun RememberRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    EifiCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(EifiSpacing.Large)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.account_choice_remember),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = EifiColors.Primary,
                    checkmarkColor = EifiColors.OnPrimary,
                ),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AccountChoiceScreenPreview() {
    val accounts = listOf(
        PayingAccount(
            operator = Operator.OrangeMoney,
            sim = SimCard(
                subscriptionId = 1,
                slotIndex = 0,
                carrierName = "Orange CM",
                operator = Operator.OrangeMoney,
            ),
        ),
        PayingAccount(
            operator = Operator.MtnMomo,
            sim = SimCard(subscriptionId = 2, slotIndex = 1, carrierName = "MTN CM", operator = Operator.MtnMomo),
        ),
    )
    EifiTheme {
        AccountChoiceScreen(
            uiState = PaymentSummaryUiState.Ready(
                displayName = "Kiosque Ndé",
                amount = 2_500,
                payeeOperators = listOf(Operator.OrangeMoney, Operator.MtnMomo),
                accounts = accounts,
                account = accounts.first(),
                receivingMethod = ReceivingMethod.MerchantCode("482913"),
                ussdAvailable = true,
                remainingSeconds = 102,
            ),
            onChoose = { _, _ -> },
            onBack = {},
        )
    }
}
