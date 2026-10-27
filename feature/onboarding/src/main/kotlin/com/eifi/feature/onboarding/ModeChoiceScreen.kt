package com.eifi.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.data.AppMode
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiBackButton
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Second onboarding screen ("Choix du mode"). [onBack] is `null` when there is no previous screen. */
@Composable
internal fun ModeChoiceScreen(
    uiState: ModeChoiceUiState,
    onSelect: (AppMode) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    OnboardingLayout(
        step = 1,
        buttonLabel = stringResource(R.string.mode_choice_continue),
        onButtonClick = onContinue,
        modifier = modifier,
    ) {
        if (onBack != null) EifiBackButton(onClick = onBack)
        Text(
            text = stringResource(R.string.mode_choice_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = EifiSpacing.XLarge),
        )
        Text(
            text = stringResource(R.string.mode_choice_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.selectableGroup().padding(top = EifiSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large),
        ) {
            ModeOptions.forEach { option ->
                ModeCard(
                    option = option,
                    selected = uiState.selectedMode == option.mode,
                    onClick = { onSelect(option.mode) },
                )
            }
        }
    }
}

/** How a mode is presented on its card. */
private data class ModeOption(
    val mode: AppMode,
    @field:DrawableRes val icon: Int,
    val iconBackground: Color,
    @field:StringRes val title: Int,
    @field:StringRes val description: Int,
)

private val ModeOptions = listOf(
    ModeOption(
        mode = AppMode.Pay,
        icon = DesignR.drawable.ic_eifi_phone_tap,
        iconBackground = EifiColors.Primary,
        title = R.string.mode_pay,
        description = R.string.mode_pay_description,
    ),
    ModeOption(
        mode = AppMode.Receive,
        icon = DesignR.drawable.ic_eifi_store,
        iconBackground = EifiColors.Accent,
        title = R.string.mode_receive,
        description = R.string.mode_receive_description,
    ),
)

@Composable
private fun ModeCard(option: ModeOption, selected: Boolean, onClick: () -> Unit) {
    Surface(
        selected = selected,
        onClick = onClick,
        shape = EifiRadius.CardLarge,
        color = EifiColors.Surface,
        border = if (selected) BorderStroke(EifiDimens.SelectionBorder, EifiColors.Primary) else null,
        modifier = Modifier.fillMaxWidth().semantics { role = Role.RadioButton },
    ) {
        Column(
            modifier = Modifier.padding(EifiSpacing.XLarge),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EifiIconTile(
                    icon = painterResource(option.icon),
                    containerColor = option.iconBackground,
                    size = EifiDimens.IconTileLarge,
                )
                Box(modifier = Modifier.weight(1f))
                if (selected) SelectedBadge()
            }
            Text(
                text = stringResource(option.title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = EifiSpacing.Small),
            )
            Text(
                text = stringResource(option.description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SelectedBadge() {
    Surface(
        shape = CircleShape,
        color = EifiColors.Primary,
        contentColor = EifiColors.OnPrimary,
        modifier = Modifier.size(EifiDimens.SelectionBadge),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_eifi_check),
                contentDescription = null,
                modifier = Modifier.size(EifiDimens.PillIcon),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ModeChoiceScreenPreview() {
    EifiTheme {
        ModeChoiceScreen(uiState = ModeChoiceUiState(), onSelect = {}, onContinue = {}, onBack = {})
    }
}
