package com.eifi.feature.receive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eifi.core.data.AppPermission
import com.eifi.core.design.component.PlaceholderScreen
import com.eifi.core.permission.rememberPermissionGranted

/** Empty screen of the Receive mode (EIFI-010), showing how payments will be confirmed. */
@Composable
internal fun ReceiveScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val automaticConfirmation by rememberPermissionGranted(AppPermission.NotificationAccess)
    val confirmation = stringResource(
        if (automaticConfirmation) R.string.receive_confirmation_automatic else R.string.receive_confirmation_manual,
    )
    PlaceholderScreen(
        title = stringResource(R.string.receive_title),
        subtitle = stringResource(R.string.receive_subtitle) + "\n" + confirmation,
        actionLabel = stringResource(R.string.receive_back),
        onAction = onBack,
        modifier = modifier,
    )
}
