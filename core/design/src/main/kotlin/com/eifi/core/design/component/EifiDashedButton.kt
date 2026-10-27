package com.eifi.core.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eifi.core.design.R
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** "+ Ajouter…" action inside a dashed outline, for an optional block that is not there yet. */
@Composable
fun EifiDashedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = EifiDimens.ButtonHeight)
            .clip(EifiRadius.CardLarge)
            .drawBehind {
                drawRoundRect(
                    color = EifiColors.Muted.copy(alpha = OUTLINE_ALPHA),
                    cornerRadius = CornerRadius(EifiRadius.CardLargeCorner.toPx()),
                    style = Stroke(
                        width = OUTLINE_WIDTH.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH.toPx(), DASH.toPx())),
                    ),
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(EifiSpacing.Large),
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(R.drawable.ic_eifi_plus), contentDescription = null, tint = EifiColors.Ink)
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = EifiColors.Ink)
    }
}

private const val OUTLINE_ALPHA = 0.5f
private val OUTLINE_WIDTH = 1.5.dp
private val DASH = 6.dp

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiDashedButtonPreview() {
    EifiTheme {
        EifiDashedButton(text = "Ajouter MTN MoMo", onClick = {}, modifier = Modifier.padding(EifiSpacing.Gutter))
    }
}
