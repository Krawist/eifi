package com.eifi.feature.receive.amount

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.AmountDisplay
import com.eifi.core.design.component.EifiBackButton
import com.eifi.core.design.component.EifiLogo
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.component.EifiPillTone
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.component.EifiTextField
import com.eifi.core.design.component.NumericKeypad
import com.eifi.core.design.format.formatFcfa
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.payload.Operator
import com.eifi.feature.receive.R
import com.eifi.feature.receive.displayName

/**
 * "Saisie du montant": amount on the keypad, optional label, accepted operators and "Encaisser".
 * [automaticConfirmation] is `false` when the notifications cannot be read, which shows the manual confirmation.
 */
@Composable
internal fun AmountEntryScreen(
    uiState: AmountEntryUiState,
    automaticConfirmation: Boolean,
    actions: AmountEntryActions,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                EifiBackButton(onClick = actions.onBack)
                EifiLogo()
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = uiState.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AmountDisplay(amount = uiState.amount, modifier = Modifier.fillMaxWidth())
                AmountLimit(uiState = uiState)
                LabelInput(uiState = uiState, actions = actions)
                AcceptedOperators(
                    uiState = uiState,
                    automaticConfirmation = automaticConfirmation,
                    actions = actions,
                )
            }
            NumericKeypad(onDigits = actions.onDigits, onDelete = actions.onDelete, onClear = actions.onClear)
            EifiPrimaryButton(
                text = if (uiState.amount > 0) {
                    stringResource(R.string.amount_entry_collect_amount, formatFcfa(uiState.amount))
                } else {
                    stringResource(R.string.amount_entry_collect)
                },
                onClick = actions.onCollect,
                enabled = uiState.canCollect,
                modifier = Modifier.padding(top = EifiSpacing.Large),
            )
        }
    }
}

/** Why the amount cannot go lower or higher, under the amount. */
@Composable
private fun AmountLimit(uiState: AmountEntryUiState) {
    val text = when {
        uiState.maximumReached -> stringResource(R.string.amount_entry_maximum, formatFcfa(AmountInput.MAX))
        uiState.belowMinimum -> stringResource(R.string.amount_entry_minimum, formatFcfa(AmountInput.MIN))
        else -> return
    }
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun LabelInput(uiState: AmountEntryUiState, actions: AmountEntryActions) {
    if (uiState.labelShown) {
        EifiTextField(
            value = uiState.label,
            onValueChange = actions.onLabelChange,
            label = stringResource(R.string.amount_entry_label),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
        )
    } else {
        EifiSecondaryButton(text = stringResource(R.string.amount_entry_add_label), onClick = actions.onShowLabel)
    }
}

/**
 * Operators of the receiving profile, each turned on or off for this payment when the profile has several, with a
 * short message on the consequence; and the manual confirmation when notifications cannot be read.
 */
@Composable
private fun AcceptedOperators(
    uiState: AmountEntryUiState,
    automaticConfirmation: Boolean,
    actions: AmountEntryActions,
) {
    OperatorNoticeToast(notice = uiState.operatorNotice, onShown = actions.onOperatorNoticeShown)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
    ) {
        uiState.acceptedOperators.forEach { operator ->
            val selected = operator in uiState.selectedOperators
            EifiPill(
                text = stringResource(operator.displayName),
                modifier = Modifier
                    .clip(CircleShape)
                    .toggleable(
                        value = selected,
                        enabled = uiState.acceptedOperators.size > 1,
                        role = Role.Switch,
                        onValueChange = { actions.onToggleOperator(operator) },
                    ),
                tone = if (selected) EifiPillTone.Soft else EifiPillTone.Neutral,
                icon = if (selected) painterResource(DesignR.drawable.ic_eifi_check) else null,
            )
        }
        if (!automaticConfirmation) {
            EifiPill(text = stringResource(R.string.amount_entry_manual_confirmation), tone = EifiPillTone.Accent)
        }
    }
}

/** Shows [notice] once as a toast. */
@Composable
private fun OperatorNoticeToast(notice: OperatorNotice?, onShown: () -> Unit) {
    val context = LocalContext.current
    val message = notice?.let {
        val operator = stringResource(it.operator.displayName)
        when (it) {
            is OperatorNotice.TurnedOn -> stringResource(R.string.amount_entry_operator_on, operator)
            is OperatorNotice.TurnedOff -> stringResource(R.string.amount_entry_operator_off, operator)
            is OperatorNotice.LastKept -> stringResource(R.string.amount_entry_operator_last, operator)
        }
    }
    LaunchedEffect(notice) {
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onShown()
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AmountEntryScreenPreview() {
    EifiTheme {
        AmountEntryScreen(
            uiState = AmountEntryUiState(
                displayName = "Boutique Mama Ngono",
                acceptedOperators = listOf(Operator.OrangeMoney, Operator.MtnMomo),
                selectedOperators = listOf(Operator.OrangeMoney),
                amount = 2_500,
            ),
            automaticConfirmation = false,
            actions = AmountEntryActions(),
        )
    }
}
