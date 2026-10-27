package com.eifi.feature.onboarding.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.data.AppPermission
import com.eifi.core.design.component.EifiBackButton
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.permission.PermissionSettingsRow
import com.eifi.feature.onboarding.R

/** Settings: status of each permission, with a way to change it, the receiving profile, and the introduction. */
@Composable
internal fun SettingsScreen(
    onEditReceivingProfile: () -> Unit,
    onReplayIntroduction: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(EifiSpacing.Gutter),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.Large),
        ) {
            EifiBackButton(onClick = onBack)
            Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge)
            Text(
                text = stringResource(R.string.settings_permissions),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = EifiSpacing.Small),
            )
            EifiCard(shape = EifiRadius.CardLarge, modifier = Modifier.fillMaxWidth()) {
                AppPermission.entries.forEachIndexed { index, permission ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = EifiColors.Divider,
                            modifier = Modifier.padding(vertical = EifiSpacing.Medium),
                        )
                    }
                    PermissionSettingsRow(permission = permission)
                }
            }
            EifiSecondaryButton(
                text = stringResource(R.string.settings_edit_receiving_profile),
                onClick = onEditReceivingProfile,
                modifier = Modifier.fillMaxWidth(),
            )
            EifiSecondaryButton(
                text = stringResource(R.string.settings_replay_introduction),
                onClick = onReplayIntroduction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    EifiTheme { SettingsScreen(onEditReceivingProfile = {}, onReplayIntroduction = {}, onBack = {}) }
}
