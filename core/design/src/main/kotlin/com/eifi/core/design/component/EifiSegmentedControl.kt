package com.eifi.core.design.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Choice between a few exclusive options, as on iOS: the selected one is a white tab on a grey track. */
@Composable
fun EifiSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = EifiRadius.Button, color = EifiColors.Divider) {
        Row(modifier = Modifier.padding(EifiSpacing.XSmall).selectableGroup()) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                Surface(
                    shape = EifiRadius.Button,
                    color = if (selected) EifiColors.Surface else Color.Transparent,
                    contentColor = if (selected) EifiColors.Ink else EifiColors.Muted,
                    modifier = Modifier
                        .weight(1f)
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(index) }),
                ) {
                    Box(
                        modifier = Modifier.heightIn(min = EifiDimens.MinTouchTarget),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = option, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun EifiSegmentedControlPreview() {
    EifiTheme {
        EifiSegmentedControl(
            options = listOf("Mon numéro", "Code marchand"),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier.padding(EifiSpacing.Gutter),
        )
    }
}
