package com.eifi.core.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eifi.core.data.AppPermission
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiSpacing

/** Settings row: the permission, its current status, and a button to the system screen that changes it. */
@Composable
fun PermissionSettingsRow(permission: AppPermission, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val content = permission.content
    val granted by rememberPermissionGranted(permission)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Large),
    ) {
        EifiIconTile(
            icon = painterResource(content.icon),
            containerColor = if (granted) EifiColors.PrimarySoft else EifiColors.AccentSoft,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(content.name), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(
                    if (granted) R.string.permission_status_granted else R.string.permission_status_denied,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        EifiSecondaryButton(text = stringResource(R.string.permission_change), onClick = {
            context.openSettings(permission)
        })
    }
}
