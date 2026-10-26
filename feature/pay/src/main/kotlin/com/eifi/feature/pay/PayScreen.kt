package com.eifi.feature.pay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eifi.core.design.component.PlaceholderScreen

/** Écran vide du mode Payer (EIFI-010). */
@Composable
fun PayScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(R.string.pay_title),
        subtitle = stringResource(R.string.pay_subtitle),
        actionLabel = stringResource(R.string.pay_back),
        onAction = onBack,
        modifier = modifier,
    )
}
