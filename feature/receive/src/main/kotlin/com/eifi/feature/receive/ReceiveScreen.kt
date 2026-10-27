package com.eifi.feature.receive

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eifi.core.design.component.PlaceholderScreen

/** Empty screen of the Receive mode (EIFI-010). */
@Composable
internal fun ReceiveScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.receive_title),
        subtitle = stringResource(R.string.receive_subtitle),
        actionLabel = stringResource(R.string.receive_back),
        onAction = onBack,
        modifier = modifier,
    )
}
