package com.eifi.feature.pay.summary

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.eifi.core.design.component.PlaceholderScreen
import com.eifi.feature.pay.R

/**
 * The phone has no screen lock, which the biometric prompt needs: opens the security settings to set one up, then
 * goes back to the summary, where the payer confirms again.
 */
@Composable
internal fun ScreenLockNeededScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    PlaceholderScreen(
        title = stringResource(R.string.screen_lock_title),
        subtitle = stringResource(R.string.screen_lock_body),
        actionLabel = stringResource(R.string.screen_lock_action),
        onAction = {
            context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            onBack()
        },
        modifier = modifier,
    )
}
