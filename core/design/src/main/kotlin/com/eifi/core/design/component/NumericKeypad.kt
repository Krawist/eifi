package com.eifi.core.design.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.eifi.core.design.R
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

private val DigitRows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
private const val TRIPLE_ZERO = "000"
private const val ZERO = "0"

/**
 * Amount keypad: digits on white keys, then « 000 », « 0 » and delete; a long press on delete calls [onClear].
 * [onDigits] receives "0" to "9" or "000"; the caller owns the amount and its limits.
 */
@Composable
fun NumericKeypad(
    onDigits: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(EifiDimens.KeypadGap)) {
        DigitRows.forEach { row ->
            KeypadRow {
                row.forEach { digit -> KeypadKey(onClick = { onDigits(digit) }) { KeyLabel(digit) } }
            }
        }
        KeypadRow {
            KeypadKey(onClick = { onDigits(TRIPLE_ZERO) }, color = Color.Transparent) { KeyLabel(TRIPLE_ZERO) }
            KeypadKey(onClick = { onDigits(ZERO) }) { KeyLabel(ZERO) }
            KeypadKey(onClick = onDelete, onLongClick = onClear, color = Color.Transparent) {
                Icon(
                    painter = painterResource(R.drawable.ic_eifi_backspace),
                    contentDescription = stringResource(R.string.keypad_delete),
                )
            }
        }
    }
}

@Composable
private fun KeypadRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EifiDimens.KeypadGap),
        content = content,
    )
}

/** A key; [onLongClick], when set, fires on a long press with a haptic feedback. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.KeypadKey(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    color: Color = EifiColors.Surface,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Surface(
        shape = EifiRadius.Button,
        color = color,
        contentColor = EifiColors.Ink,
        modifier = Modifier
            .weight(1f)
            .height(EifiDimens.KeypadKeyHeight)
            .clip(EifiRadius.Button)
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick?.let {
                    {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        it()
                    }
                },
            ),
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun KeyLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 26.sp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun NumericKeypadPreview() {
    EifiTheme {
        NumericKeypad(onDigits = {}, onDelete = {}, onClear = {}, modifier = Modifier.padding(EifiSpacing.Gutter))
    }
}
