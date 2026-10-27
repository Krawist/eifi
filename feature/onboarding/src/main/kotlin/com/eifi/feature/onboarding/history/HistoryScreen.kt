package com.eifi.feature.onboarding.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiBackButton
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.component.EifiPill
import com.eifi.core.design.component.EifiPillTone
import com.eifi.core.design.format.formatFcfa
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme
import com.eifi.core.payload.Operator
import com.eifi.feature.onboarding.R
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** "Historique": the payments received and sent, by day; a sent payment opens its receipt ([onOpenReceipt]). */
@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onOpenReceipt: (paymentId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { LocalDate.now() }
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(horizontal = EifiSpacing.Gutter)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = EifiSpacing.Gutter)) {
                EifiBackButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart))
                Text(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            if (uiState.days.isEmpty()) {
                Text(
                    text = stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = EifiColors.Muted,
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
                contentPadding = PaddingValues(bottom = EifiSpacing.Gutter),
            ) {
                uiState.days.forEach { day ->
                    item(key = day.date.toString()) {
                        Text(
                            text = dayTitle(date = day.date, today = today),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = EifiSpacing.Medium),
                        )
                    }
                    items(items = day.entries, key = { it.id }) { entry ->
                        HistoryRow(
                            entry = entry,
                            onClick = { onOpenReceipt(entry.id) }.takeIf { entry.type == TransactionType.Sent },
                        )
                    }
                }
            }
        }
    }
}

/** A payment; [onClick] is `null` when it has no receipt to open. */
@Composable
private fun HistoryRow(entry: HistoryUiState.Entry, onClick: (() -> Unit)?) {
    val received = entry.type == TransactionType.Received
    EifiCard(
        shape = EifiRadius.Card,
        contentPadding = PaddingValues(EifiSpacing.Large),
        modifier = Modifier.fillMaxWidth().then(
            if (onClick !=
                null
            ) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
        ) {
            EifiIconTile(
                icon = painterResource(
                    if (received) DesignR.drawable.ic_eifi_arrow_down else DesignR.drawable.ic_eifi_arrow_up,
                ),
                containerColor = if (received) EifiColors.PrimarySoft else EifiColors.Divider,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(EifiSpacing.XSmall)) {
                Text(
                    text = title(entry),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = details(entry),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EifiColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                StatusPill(status = entry.status)
            }
            // A received payment only adds money once confirmed.
            val credited = entry.status == HistoryStatus.Confirmed || entry.status == HistoryStatus.Manual
            Text(
                text = when {
                    !received -> MINUS_SIGN
                    credited -> "+"
                    else -> ""
                } + formatFcfa(entry.amount),
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    !received -> EifiColors.Ink
                    credited -> EifiColors.OnPrimary
                    else -> EifiColors.Muted
                },
            )
        }
    }
}

@Composable
private fun StatusPill(status: HistoryStatus) {
    val (text, tone) = when (status) {
        HistoryStatus.Waiting -> R.string.history_status_waiting to EifiPillTone.Accent
        HistoryStatus.Confirmed -> R.string.history_status_confirmed to EifiPillTone.Soft
        HistoryStatus.Manual -> R.string.history_status_manual to EifiPillTone.Accent
        HistoryStatus.Expired -> R.string.history_status_expired to EifiPillTone.Neutral
        HistoryStatus.Cancelled -> R.string.history_status_cancelled to EifiPillTone.Neutral
        HistoryStatus.RequestSent -> R.string.history_status_request_sent to EifiPillTone.Neutral
        HistoryStatus.DebitConfirmed -> R.string.history_status_debit_confirmed to EifiPillTone.Soft
    }
    EifiPill(text = stringResource(text), tone = tone)
}

/** "Reçu de …" or "Payé à …", or the kind of payment when the other party is unknown. */
@Composable
private fun title(entry: HistoryUiState.Entry): String {
    val received = entry.type == TransactionType.Received
    return when {
        entry.counterpart.isNullOrEmpty() ->
            stringResource(if (received) R.string.history_received else R.string.history_sent)
        received -> stringResource(R.string.home_activity_received, entry.counterpart)
        else -> stringResource(R.string.home_activity_sent, entry.counterpart)
    }
}

/** The time, the operator when known and the label, separated by « · ». */
@Composable
private fun details(entry: HistoryUiState.Entry): String = listOfNotNull(
    DateTimeFormatter.ofPattern("HH:mm").format(entry.at.atZone(ZoneId.systemDefault())),
    entry.operator?.let { stringResource(it.displayName) },
    entry.label,
).joinToString(separator = " · ")

@Composable
private fun dayTitle(date: LocalDate, today: LocalDate): String = when (date) {
    today -> stringResource(R.string.history_today)
    today.minusDays(1) -> stringResource(R.string.home_activity_yesterday)
    else -> DateTimeFormatter.ofPattern(stringResource(R.string.history_date_pattern)).format(date)
}

private val Operator.displayName: Int
    get() = when (this) {
        Operator.OrangeMoney -> R.string.operator_orange_money
        Operator.MtnMomo -> R.string.operator_mtn_momo
    }

/** Typographic minus (U+2212), the width of the plus sign. */
private const val MINUS_SIGN = "−"

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun HistoryScreenPreview() {
    val now = Instant.parse("2026-10-27T13:32:00Z")
    EifiTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                days = listOf(
                    HistoryUiState.Day(
                        date = LocalDate.parse("2026-10-27"),
                        entries = listOf(
                            HistoryUiState.Entry(
                                id = "1",
                                type = TransactionType.Received,
                                counterpart = "+237 6XX XX XX 41",
                                label = "Pain",
                                operator = Operator.OrangeMoney,
                                amount = 2_500,
                                at = now,
                                status = HistoryStatus.Confirmed,
                            ),
                            HistoryUiState.Entry(
                                id = "2",
                                type = TransactionType.Sent,
                                counterpart = "Kiosque Ndé",
                                label = null,
                                operator = Operator.MtnMomo,
                                amount = 1_000,
                                at = now.minus(Duration.ofHours(1)),
                                status = HistoryStatus.RequestSent,
                            ),
                        ),
                    ),
                ),
            ),
            onOpenReceipt = {},
            onBack = {},
        )
    }
}
