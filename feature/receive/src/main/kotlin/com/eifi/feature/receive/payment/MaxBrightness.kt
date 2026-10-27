package com.eifi.feature.receive.payment

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/** Screen at full brightness while in the composition, so that a QR code reads well; then as it was before. */
@Composable
internal fun MaxBrightness() {
    val window = LocalActivity.current?.window ?: return
    DisposableEffect(window) {
        val initial = window.attributes.screenBrightness
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        onDispose {
            window.attributes = window.attributes.apply { screenBrightness = initial }
        }
    }
}
