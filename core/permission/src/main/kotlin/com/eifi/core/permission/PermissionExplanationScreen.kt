package com.eifi.core.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.data.AppPermission
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiBackButton
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Why a permission is needed ("Confirmation automatique" mockup), with the way to do without it. */
@Composable
internal fun PermissionExplanationScreen(
    permission: AppPermission,
    onAllow: () -> Unit,
    onLater: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val content = permission.content
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
            ) {
                if (onBack != null) EifiBackButton(onClick = onBack)
                if (permission == AppPermission.NotificationAccess) {
                    NotificationIllustration(modifier = Modifier.padding(vertical = EifiSpacing.Large))
                } else {
                    EifiIconTile(
                        icon = painterResource(content.icon),
                        containerColor = EifiColors.Soft,
                        size = EifiDimens.IconTileHero,
                        modifier = Modifier.padding(vertical = EifiSpacing.Large),
                    )
                }
                Text(text = stringResource(content.title), style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = stringResource(content.body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                content.points.forEach { point -> Point(text = stringResource(point)) }
                if (permission == AppPermission.BatteryOptimisation) {
                    remember { DeviceBrand.current() }?.let { brand ->
                        BatteryGuide(brand = brand, modifier = Modifier.padding(top = EifiSpacing.Medium))
                    }
                }
            }
            EifiPrimaryButton(
                text = stringResource(content.allow),
                onClick = onAllow,
                modifier = Modifier.padding(top = EifiSpacing.Large),
            )
            EifiSecondaryButton(
                text = stringResource(content.later),
                onClick = onLater,
                modifier = Modifier.fillMaxWidth().padding(top = EifiSpacing.Small),
            )
        }
    }
}

@Composable
private fun Point(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium), verticalAlignment = Alignment.Top) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_eifi_check),
            contentDescription = null,
            tint = EifiColors.Primary,
            modifier = Modifier.padding(top = EifiSpacing.XSmall / 2).size(EifiDimens.PointIcon),
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun NotificationExplanationPreview() {
    EifiTheme {
        PermissionExplanationScreen(
            permission = AppPermission.NotificationAccess,
            onAllow = {},
            onLater = {},
            onBack = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PhoneExplanationPreview() {
    EifiTheme {
        PermissionExplanationScreen(permission = AppPermission.PhoneCalls, onAllow = {}, onLater = {})
    }
}
