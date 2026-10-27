package com.eifi.feature.receive.profile

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
import com.eifi.feature.receive.displayName
import com.eifi.feature.receive.profile.phone.CameroonPhoneCountry
import com.eifi.feature.receive.shortName

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
                    isError = uiState.showErrors && uiState.displayNameMissing,
                    supportingText = stringResource(R.string.receiving_profile_display_name_required)
                        .takeIf { uiState.showErrors && uiState.displayNameMissing },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )
                uiState.operators.forEach { (operator, form) ->
                    OperatorCard(operator = operator, form = form, uiState = uiState, actions = actions)
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
                modifier = Modifier.padding(top = EifiSpacing.Large),
            )
        }
    }
}

@Composable
private fun OperatorCard(
    operator: Operator,
    form: OperatorForm,
    uiState: ReceivingProfileUiState,
    actions: ReceivingProfileActions,
) {
    val operatorName = stringResource(operator.displayName)
    val removable = uiState.operators.size > 1
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
                        countries = uiState.phoneCountries,
                        onSelect = { actions.onCountryChange(operator, it) },
                    )
                }
                ValueField(
                    operator = operator,
                    operatorName = operatorName,
                    form = form,
                    error = form.error?.takeIf { uiState.showErrors || it == OperatorFormError.PhoneInvalid },
                    actions = actions,
                )
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
    error: OperatorFormError?,
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
        isError = error != null,
        supportingText = error?.let { stringResource(it.message) },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (phone) KeyboardType.Phone else KeyboardType.Text,
            imeAction = ImeAction.Done,
        ),
        modifier = Modifier.weight(1f),
    )
}

private val OperatorFormError.message: Int
    get() = when (this) {
        OperatorFormError.PhoneRequired -> R.string.receiving_profile_phone_required
        OperatorFormError.PhoneInvalid -> R.string.receiving_profile_phone_rule
        OperatorFormError.MerchantCodeRequired -> R.string.receiving_profile_code_required
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

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ReceivingProfileScreenPreview() {
    EifiTheme {
        ReceivingProfileScreen(
            uiState = ReceivingProfileUiState(
                phoneCountries = listOf(CameroonPhoneCountry),
                displayName = "Boutique Mama Ngono",
                operators = mapOf(
                    Operator.OrangeMoney to OperatorForm(country = CameroonPhoneCountry, phoneNumber = "6901"),
                ),
            ),
            editing = false,
            actions = ReceivingProfileActions(),
        )
    }
}
