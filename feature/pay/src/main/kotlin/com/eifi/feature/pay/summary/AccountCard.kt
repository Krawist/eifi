package com.eifi.feature.pay.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.payload.Operator
import com.eifi.feature.pay.R
import com.eifi.feature.pay.displayName
import com.eifi.feature.pay.shortName

/** The payer's account; [onChange] is `null` when there is no other compatible account. */
@Composable
internal fun AccountCard(label: String, account: PayingAccount, onChange: (() -> Unit)?) {
    EifiCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(EifiSpacing.Large)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OperatorBadge(operator = account.operator)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = accountName(account), style = MaterialTheme.typography.titleMedium)
            }
            if (onChange != null) {
                TextButton(
                    onClick = onChange,
                    colors = ButtonDefaults.textButtonColors(contentColor = EifiColors.OnPrimary),
                ) {
                    Text(
                        text = stringResource(R.string.summary_account_change),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

/** Why the payment is blocked: none of the payer's SIMs is with one of [payeeOperators]. */
@Composable
internal fun NoAccountCard(displayName: String, payeeOperators: List<Operator>) {
    EifiCard(
        modifier = Modifier.fillMaxWidth(),
        color = EifiColors.AccentSoft,
        contentPadding = PaddingValues(EifiSpacing.Large),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small)) {
            Text(text = stringResource(R.string.summary_no_account_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.summary_no_account_body, displayName),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small)) {
                payeeOperators.forEach { EifiPill(text = stringResource(it.displayName)) }
            }
        }
    }
}

/** "Orange Money · SIM 1", or the operator alone when the SIMs are not known. */
@Composable
internal fun accountName(account: PayingAccount): String {
    val operator = stringResource(account.operator.displayName)
    return account.sim?.let { stringResource(R.string.summary_account_sim, operator, it.slotIndex + 1) } ?: operator
}

@Composable
internal fun OperatorBadge(operator: Operator) {
    Surface(
        shape = EifiRadius.Button,
        color = EifiColors.Soft,
        contentColor = EifiColors.OnSoft,
        modifier = Modifier.size(EifiDimens.IconTileLarge),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = stringResource(operator.shortName), style = MaterialTheme.typography.labelMedium)
        }
    }
}
