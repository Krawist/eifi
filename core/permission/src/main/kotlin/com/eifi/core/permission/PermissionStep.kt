package com.eifi.core.permission

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.eifi.core.data.AppPermission

/**
 * Explains [permission], then asks for it: system dialog for a runtime permission, system settings otherwise.
 * [onDone] receives whether it is granted, also when the user chooses the alternative ("Plus tard").
 */
@Composable
fun PermissionStep(
    permission: AppPermission,
    onDone: (granted: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val checker = rememberPermissionChecker()
    val currentOnDone by rememberUpdatedState(onDone)
    var waitingForSettings by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        currentOnDone(results.isNotEmpty() && results.values.all { it })
    }
    // Only a real return to the screen counts, not the composition.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (waitingForSettings) {
            waitingForSettings = false
            currentOnDone(checker.isGranted(permission))
        }
    }
    PermissionExplanationScreen(
        permission = permission,
        onAllow = {
            val runtimePermissions = permission.manifestPermissions
            if (runtimePermissions.isEmpty()) {
                waitingForSettings = true
                context.openSettings(permission)
            } else {
                launcher.launch(runtimePermissions.toTypedArray())
            }
        },
        onLater = { onDone(false) },
        onBack = onBack,
        modifier = modifier,
    )
}

/** Opens the settings screen of [permission], or the app details when a brand removed that screen. */
internal fun Context.openSettings(permission: AppPermission) {
    try {
        startActivity(permission.settingsIntent(this))
    } catch (ignored: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
    }
}
