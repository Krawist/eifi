package com.eifi.feature.pay.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.payload.Operator
import com.eifi.feature.pay.R
import com.eifi.feature.pay.displayName
import com.eifi.feature.pay.shortName

/** The payer's account; [onChange] is `null` when the payee accepts a single operator. */
@Composable
internal fun AccountCard(label: String, operator: Operator, onChange: (() -> Unit)?) {
    EifiCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(EifiSpacing.Large)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OperatorBadge(operator = operator)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = stringResource(operator.displayName), style = MaterialTheme.typography.titleMedium)
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

/** Choice among the payee's operators. TODO(EIFI-202): the "Choix de l'opérateur" screen, with the SIMs. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountSheet(
    operators: List<Operator>,
    selected: Operator,
    onSelect: (Operator) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = EifiColors.Background) {
        Column(
            modifier = Modifier.navigationBarsPadding().padding(EifiSpacing.Gutter),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
        ) {
            Text(text = stringResource(R.string.summary_account_title), style = MaterialTheme.typography.titleLarge)
            operators.forEach { operator ->
                EifiCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(operator) },
                    contentPadding = PaddingValues(EifiSpacing.Large),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OperatorBadge(operator = operator)
                        Text(
                            text = stringResource(operator.displayName),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (operator == selected) {
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_eifi_check),
                                contentDescription = null,
                                modifier = Modifier.size(EifiDimens.SelectionBadge),
                                tint = EifiColors.OnPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperatorBadge(operator: Operator) {
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
