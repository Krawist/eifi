package com.eifi.feature.collect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eifi.core.design.component.PlaceholderScreen

/** Empty screen of the Collect mode (EIFI-010). */
@Composable
fun CollectScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.collect_title),
        subtitle = stringResource(R.string.collect_subtitle),
        actionLabel = stringResource(R.string.collect_back),
        onAction = onBack,
        modifier = modifier,
    )
}
