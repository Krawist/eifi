package com.eifi.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.R
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Small non-interactive label (« ✓ Orange Money », « Manuel »), with an optional leading icon. */
@Composable
fun EifiPill(
    text: String,
    modifier: Modifier = Modifier,
    tone: EifiPillTone = EifiPillTone.Soft,
    icon: Painter? = null,
) {
    Surface(modifier = modifier, shape = CircleShape, color = tone.container, contentColor = tone.content) {
        Row(
            modifier = Modifier
                .heightIn(min = EifiDimens.PillHeight)
                .padding(horizontal = EifiSpacing.Medium, vertical = EifiSpacing.XSmall),
            horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(painter = icon, contentDescription = null, modifier = Modifier.size(EifiDimens.PillIcon))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiPillPreview() {
    EifiTheme {
        Row(
            modifier = Modifier.padding(EifiSpacing.Gutter),
            horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        ) {
            EifiPill(text = "Orange Money", icon = painterResource(R.drawable.ic_eifi_check))
            EifiPill(text = "Manuel", tone = EifiPillTone.Accent)
            EifiPill(text = "SIM 1", tone = EifiPillTone.Neutral)
        }
    }
}
