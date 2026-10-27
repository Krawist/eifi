package com.eifi.feature.onboarding.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.data.ActivityItem
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.component.EifiLogo
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.component.EifiPillTone
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.nfc.NfcStatus
import com.eifi.core.payload.Operator
import com.eifi.feature.onboarding.R
import java.time.Instant
import java.time.temporal.ChronoUnit

/** Home screen ("Accueil"): both modes, Pay first, then the recent activity. */
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onPay: () -> Unit,
    onReceive: () -> Unit,
    onSeeAllActivity: () -> Unit,
    onOpenSettings: () -> Unit,
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
            Header(onOpenSettings = onOpenSettings)
            Text(
                text = uiState.greetingName?.let { stringResource(R.string.home_greeting, it) }
                    ?: stringResource(R.string.home_greeting_without_name),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = EifiSpacing.Small),
            )
            PayCard(nfcStatus = uiState.nfcStatus, onClick = onPay)
            ReceiveCard(onClick = onReceive)
            ActivityHeader(onSeeAll = onSeeAllActivity)
            ActivityList(items = uiState.recentActivity)
        }
    }
}

@Composable
private fun Header(onOpenSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        EifiLogo(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(EifiDimens.RoundButton),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = EifiColors.Surface,
                contentColor = EifiColors.Ink,
            ),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_eifi_person),
                contentDescription = stringResource(R.string.home_settings),
            )
        }
    }
}

@Composable
private fun PayCard(nfcStatus: NfcStatus?, onClick: () -> Unit) {
    EifiCard(
        onClick = onClick,
        color = EifiColors.Primary,
        shape = EifiRadius.CardLarge,
        contentPadding = PaddingValues(EifiSpacing.XLarge),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_eifi_scan),
                contentDescription = null,
                tint = EifiColors.OnPrimary,
                modifier = Modifier.size(EifiDimens.IconTile),
            )
            Spacer(modifier = Modifier.weight(1f))
            if (nfcStatus != null) NfcStatusPill(status = nfcStatus)
        }
        Spacer(modifier = Modifier.height(EifiDimens.PayCardSpace))
        Text(
            text = stringResource(R.string.home_pay_title),
            style = MaterialTheme.typography.headlineLarge,
            color = EifiColors.OnPrimary,
        )
        Text(
            text = stringResource(R.string.home_pay_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = EifiColors.OnPrimary,
        )
    }
}

@Composable
private fun NfcStatusPill(status: NfcStatus) {
    when (status) {
        NfcStatus.Ready -> EifiPill(
            text = stringResource(R.string.home_nfc_ready),
            tone = EifiPillTone.OnPrimary,
            icon = painterResource(DesignR.drawable.ic_eifi_contactless),
        )
        NfcStatus.Disabled -> EifiPill(text = stringResource(R.string.home_nfc_disabled), tone = EifiPillTone.Accent)
        NfcStatus.Unavailable -> EifiPill(
            text = stringResource(R.string.home_nfc_unavailable),
            tone = EifiPillTone.Neutral,
        )
    }
}

@Composable
private fun ReceiveCard(onClick: () -> Unit) {
    EifiCard(onClick = onClick, shape = EifiRadius.CardLarge, modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Large),
        ) {
            EifiIconTile(
                icon = painterResource(DesignR.drawable.ic_eifi_arrow_down),
                containerColor = EifiColors.Accent,
                size = EifiDimens.IconTileLarge,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.home_receive_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.home_receive_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                painter = painterResource(DesignR.drawable.ic_eifi_chevron_right),
                contentDescription = null,
                tint = EifiColors.Muted,
            )
        }
    }
}

private const val PREVIEW_MINUTES_AGO = 25L

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    val now = Instant.now()
    EifiTheme {
        HomeScreen(
            uiState = HomeUiState(
                greetingName = "Boutique Mama Ngono",
                nfcStatus = NfcStatus.Ready,
                recentActivity = listOf(
                    ActivityItem(
                        id = "1",
                        type = TransactionType.Received,
                        counterpart = "+237 6XX XX XX 41",
                        operator = Operator.OrangeMoney,
                        amount = 2_500,
                        at = now.minus(PREVIEW_MINUTES_AGO, ChronoUnit.MINUTES),
                    ),
                    ActivityItem(
                        id = "2",
                        type = TransactionType.Sent,
                        counterpart = "Kiosque Ndé",
                        operator = Operator.MtnMomo,
                        amount = 1_000,
                        at = now.minus(1, ChronoUnit.DAYS),
                    ),
                ),
            ),
            onPay = {},
            onReceive = {},
            onSeeAllActivity = {},
            onOpenSettings = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenEmptyPreview() {
    EifiTheme {
        HomeScreen(
            uiState = HomeUiState(greetingName = "Paul", nfcStatus = NfcStatus.Disabled),
            onPay = {},
            onReceive = {},
            onSeeAllActivity = {},
            onOpenSettings = {},
        )
    }
}
