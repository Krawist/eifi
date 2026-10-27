package com.eifi.core.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.format.formatFcfa
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing

/** An operator SMS turned into "Paiement reçu", at the top of the notification access explanation. */
@Composable
internal fun NotificationIllustration(modifier: Modifier = Modifier) {
    val amount = formatFcfa(ILLUSTRATION_AMOUNT)
    EifiCard(
        shape = EifiRadius.CardLarge,
        contentPadding = PaddingValues(EifiSpacing.Large),
        modifier = modifier.fillMaxWidth(),
    ) {
        SmsRow(amount = amount)
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = EifiSpacing.Small),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_eifi_arrow_down),
                contentDescription = null,
                tint = EifiColors.Muted,
            )
        }
        ReceivedRow(amount = amount)
    }
}

@Composable
private fun SmsRow(amount: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
        modifier = Modifier
            .fillMaxWidth()
            .background(EifiColors.Background, EifiRadius.Button)
            .padding(EifiSpacing.Medium),
    ) {
        EifiIconTile(icon = painterResource(DesignR.drawable.ic_eifi_message), containerColor = EifiColors.Soft)
        Column {
            Text(
                text = stringResource(R.string.permission_notification_sms_sender),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.permission_notification_sms_text, amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ReceivedRow(amount: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
        modifier = Modifier
            .fillMaxWidth()
            .background(EifiColors.Primary, EifiRadius.Button)
            .padding(EifiSpacing.Large),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_eifi_check),
            contentDescription = null,
            tint = EifiColors.OnPrimary,
            modifier = Modifier.size(EifiDimens.PointIcon),
        )
        Text(
            text = stringResource(R.string.permission_notification_received, amount),
            style = MaterialTheme.typography.titleMedium,
            color = EifiColors.OnPrimary,
        )
    }
}

private const val ILLUSTRATION_AMOUNT = 2_500L
