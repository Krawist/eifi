package com.eifi.core.design.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.R
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Round white back button at the top left of a screen. */
@Composable
fun EifiBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(EifiDimens.RoundButton),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = EifiColors.Surface,
            contentColor = EifiColors.Ink,
        ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_eifi_back),
            contentDescription = stringResource(R.string.navigation_back),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiBackButtonPreview() {
    EifiTheme {
        EifiBackButton(onClick = {}, modifier = Modifier.padding(EifiSpacing.Gutter))
    }
}
