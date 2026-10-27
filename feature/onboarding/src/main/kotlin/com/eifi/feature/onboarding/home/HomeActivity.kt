package com.eifi.feature.onboarding.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.eifi.core.data.ActivityItem
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.component.EifiSecondaryButton
import com.eifi.core.design.format.formatFcfa
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.payload.Operator
import com.eifi.feature.onboarding.R
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** "Recent activity" section of the home screen. */
@Composable
internal fun ActivityHeader(onSeeAll: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = EifiSpacing.Medium)) {
        Text(
            text = stringResource(R.string.home_activity_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        EifiSecondaryButton(text = stringResource(R.string.home_activity_see_all), onClick = onSeeAll)
    }
}

@Composable
internal fun ActivityList(items: List<ActivityItem>) {
    EifiCard(shape = EifiRadius.CardLarge, modifier = Modifier.fillMaxWidth()) {
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.home_activity_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val now = remember { ZonedDateTime.now() }
        items.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(
                    color = EifiColors.Divider,
                    modifier = Modifier.padding(vertical = EifiSpacing.Medium),
                )
            }
            ActivityRow(item = item, now = now)
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityItem, now: ZonedDateTime) {
    val received = item.type == TransactionType.Received
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Large),
    ) {
        EifiIconTile(
            icon = painterResource(
                if (received) DesignR.drawable.ic_eifi_arrow_down else DesignR.drawable.ic_eifi_arrow_up,
            ),
            containerColor = if (received) EifiColors.PrimarySoft else EifiColors.Divider,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    if (received) R.string.home_activity_received else R.string.home_activity_sent,
                    item.counterpart,
                ),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.home_activity_details,
                    stringResource(item.operator.displayName),
                    activityTimeText(activityTime(item.at, now)),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = (if (received) "+" else MINUS_SIGN) + formatFcfa(item.amount),
            style = MaterialTheme.typography.titleMedium,
            color = if (received) EifiColors.OnPrimary else EifiColors.Ink,
        )
    }
}

@Composable
private fun activityTimeText(time: ActivityTime): String = when (time) {
    is ActivityTime.Today -> time.time.format(DateTimeFormatter.ofPattern("HH:mm"))
    ActivityTime.Yesterday -> stringResource(R.string.home_activity_yesterday)
    is ActivityTime.Earlier -> time.date.format(DateTimeFormatter.ofPattern("d MMM"))
}

private val Operator.displayName: Int
    get() = when (this) {
        Operator.OrangeMoney -> R.string.operator_orange_money
        Operator.MtnMomo -> R.string.operator_mtn_momo
    }

/** Typographic minus (U+2212), the width of the plus sign. */
private const val MINUS_SIGN = "−"
