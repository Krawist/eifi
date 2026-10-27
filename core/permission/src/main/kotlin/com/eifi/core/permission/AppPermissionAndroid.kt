package com.eifi.core.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.eifi.core.data.AppPermission

/** Runtime permissions requested with the system dialog; empty for the permissions granted in the settings. */
internal val AppPermission.manifestPermissions: List<String>
    get() = when (this) {
        AppPermission.NotificationAccess, AppPermission.BatteryOptimisation -> emptyList()
        AppPermission.PhoneCalls -> listOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
        AppPermission.Camera -> listOf(Manifest.permission.CAMERA)
    }

/**
 * System screen where the user grants or revokes the permission: the notification access list, the battery
 * optimisation list (no `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, restricted by the Play Store), or the app details.
 */
internal fun AppPermission.settingsIntent(context: Context): Intent = when (this) {
    AppPermission.NotificationAccess -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    AppPermission.BatteryOptimisation -> Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    AppPermission.PhoneCalls, AppPermission.Camera ->
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
}
