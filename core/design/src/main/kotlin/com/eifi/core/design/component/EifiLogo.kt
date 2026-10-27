package com.eifi.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.R
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/**
 * Small logo and "eifi" wordmark, at the top left of the main screens.
 * The logo is not chosen yet (see docs/design/90-logo-pistes.png): the contactless icon stands in.
 */
@Composable
fun EifiLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EifiIconTile(
            icon = painterResource(R.drawable.ic_eifi_contactless),
            containerColor = EifiColors.Primary,
            contentColor = EifiColors.OnPrimary,
            size = EifiDimens.IconTileSmall,
        )
        Text(
            text = stringResource(R.string.eifi_wordmark),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiLogoPreview() {
    EifiTheme { EifiLogo(modifier = Modifier.padding(EifiSpacing.Gutter)) }
}
