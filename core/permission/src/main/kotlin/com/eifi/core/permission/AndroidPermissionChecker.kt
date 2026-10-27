package com.eifi.core.permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.eifi.core.data.AppPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidPermissionChecker @Inject constructor(@ApplicationContext private val context: Context) :
    PermissionChecker {

    override fun isGranted(permission: AppPermission): Boolean = when (permission) {
        AppPermission.NotificationAccess ->
            context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)
        AppPermission.BatteryOptimisation ->
            context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)
        AppPermission.PhoneCalls, AppPermission.Camera -> permission.manifestPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
