package com.eifi.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.eifi.core.data.AppPermission

/** Whether [permission] is granted, checked again each time the screen resumes (e.g. back from the settings). */
@Composable
fun rememberPermissionGranted(permission: AppPermission): State<Boolean> {
    val checker = rememberPermissionChecker()
    val granted = remember(permission) { mutableStateOf(checker.isGranted(permission)) }
    LifecycleResumeEffect(permission) {
        granted.value = checker.isGranted(permission)
        onPauseOrDispose {}
    }
    return granted
}

@Composable
internal fun rememberPermissionChecker(): PermissionChecker {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidPermissionChecker(context) }
}
