package com.eifi.feature.receive.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.component.EifiBackButton
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiDashedButton
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.component.EifiSegmentedControl
import com.eifi.core.design.component.EifiTextField
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.payload.Operator
import com.eifi.feature.receive.R
import com.eifi.feature.receive.profile.phone.CameroonPhoneCountry
import com.eifi.feature.receive.profile.phone.PhoneCountry

/** "Où recevoir l'argent": display name and, for each operator, a phone number or a merchant code. */
@Composable
internal fun ReceivingProfileScreen(
    uiState: ReceivingProfileUiState,
    editing: Boolean,
    actions: ReceivingProfileActions,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large),
            ) {
                EifiBackButton(onClick = actions.onBack)
                Text(
                    text = stringResource(R.string.receiving_profile_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = EifiSpacing.Medium),
                )
                Text(
                    text = stringResource(R.string.receiving_profile_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                EifiTextField(
                    value = uiState.displayName,
                    onValueChange = actions.onDisplayNameChange,
                    label = stringResource(R.string.receiving_profile_display_name),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )
                uiState.operators.forEach { (operator, form) ->
                    OperatorCard(
                        operator = operator,
                        form = form,
                        countries = uiState.phoneCountries,
                        removable = uiState.operators.size > 1,
                        actions = actions,
                    )
                }
                uiState.missingOperators.forEach { operator ->
                    EifiDashedButton(
                        text = stringResource(R.string.receiving_profile_add, stringResource(operator.displayName)),
                        onClick = { actions.onAddOperator(operator) },
                    )
                }
            }
            EifiPrimaryButton(
                text = stringResource(
                    if (editing) R.string.receiving_profile_save else R.string.receiving_profile_continue,
                ),
                onClick = actions.onSave,
                enabled = uiState.canSave,
                modifier = Modifier.padding(top = EifiSpacing.Large),
            )
        }
    }
}

@Composable
private fun OperatorCard(
    operator: Operator,
    form: OperatorForm,
    countries: List<PhoneCountry>,
    removable: Boolean,
    actions: ReceivingProfileActions,
) {
    val operatorName = stringResource(operator.displayName)
    EifiCard(shape = EifiRadius.CardLarge, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Large),
            ) {
                OperatorBadge(text = stringResource(operator.shortName))
                Text(text = operatorName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (removable) {
                    EifiSecondaryButton(
                        text = stringResource(R.string.receiving_profile_remove),
                        onClick = { actions.onRemoveOperator(operator) },
                    )
                }
            }
            EifiSegmentedControl(
                options = listOf(
                    stringResource(R.string.receiving_profile_phone),
                    stringResource(R.string.receiving_profile_merchant_code),
                ),
                selectedIndex = form.kind.ordinal,
                onSelect = { actions.onKindChange(operator, ReceivingMethodKind.entries[it]) },
            )
            val phone = form.kind == ReceivingMethodKind.PhoneNumber
            Row(horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small)) {
                if (phone) {
                    CountrySelector(
                        selected = form.country,
                        countries = countries,
                        onSelect = { actions.onCountryChange(operator, it) },
                    )
                }
                ValueField(operator = operator, operatorName = operatorName, form = form, actions = actions)
            }
            Text(
                text = stringResource(R.string.receiving_profile_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Phone number of the selected country, or merchant code. */
@Composable
private fun RowScope.ValueField(
    operator: Operator,
    operatorName: String,
    form: OperatorForm,
    actions: ReceivingProfileActions,
) {
    val phone = form.kind == ReceivingMethodKind.PhoneNumber
    EifiTextField(
        value = form.value,
        onValueChange = { actions.onValueChange(operator, it) },
        label = stringResource(
            if (phone) R.string.receiving_profile_phone_label else R.string.receiving_profile_code_label,
            operatorName,
        ),
        containerColor = EifiColors.Background,
        supportingText = stringResource(R.string.receiving_profile_phone_rule)
            .takeIf { phone && form.value.isNotEmpty() && form.receivingMethod == null },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (phone) KeyboardType.Phone else KeyboardType.Text,
            imeAction = ImeAction.Done,
        ),
        modifier = Modifier.weight(1f),
    )
}

/** Operator initials on a soft tile, as on the mockups ("OM"). */
@Composable
private fun OperatorBadge(text: String) {
    Surface(
        shape = EifiRadius.Button,
        color = EifiColors.Soft,
        contentColor = EifiColors.OnSoft,
        modifier = Modifier.size(EifiDimens.IconTileLarge),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@get:StringRes
private val Operator.displayName: Int
    get() = when (this) {
        Operator.OrangeMoney -> R.string.operator_orange_money
        Operator.MtnMomo -> R.string.operator_mtn_momo
    }

@get:StringRes
private val Operator.shortName: Int
    get() = when (this) {
        Operator.OrangeMoney -> R.string.operator_orange_money_short
        Operator.MtnMomo -> R.string.operator_mtn_momo_short
    }

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ReceivingProfileScreenPreview() {
    EifiTheme {
        ReceivingProfileScreen(
            uiState = ReceivingProfileUiState(
                phoneCountries = listOf(CameroonPhoneCountry),
                displayName = "Boutique Mama Ngono",
                operators = mapOf(Operator.OrangeMoney to OperatorForm(country = CameroonPhoneCountry, value = "6901")),
            ),
            editing = false,
            actions = ReceivingProfileActions(),
        )
    }
}
