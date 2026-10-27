package com.eifi.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.eifi.core.design.R
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Decorative icon on a rounded colour square, as in front of cards and list rows. */
@Composable
fun EifiIconTile(
    icon: Painter,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = EifiColors.Ink,
    size: Dp = EifiDimens.IconTile,
) {
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(size * TILE_RADIUS_RATIO),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(size * ICON_SIZE_RATIO))
        }
    }
}

private const val TILE_RADIUS_RATIO = 0.28f
private const val ICON_SIZE_RATIO = 0.45f

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiIconTilePreview() {
    EifiTheme {
        Row(
            modifier = Modifier.padding(EifiSpacing.Gutter),
            horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
        ) {
            EifiIconTile(icon = painterResource(R.drawable.ic_eifi_contactless), containerColor = EifiColors.Soft)
            EifiIconTile(icon = painterResource(R.drawable.ic_eifi_wallet), containerColor = EifiColors.AccentSoft)
            EifiIconTile(icon = painterResource(R.drawable.ic_eifi_shield), containerColor = EifiColors.PrimarySoft)
            EifiIconTile(
                icon = painterResource(R.drawable.ic_eifi_store),
                containerColor = EifiColors.Accent,
                size = EifiDimens.IconTileLarge,
            )
        }
    }
}
