package com.eifi.core.permission

import com.eifi.core.data.AppPermission

/** Current status of a permission; check it again when the user comes back from the system settings. */
interface PermissionChecker {
    fun isGranted(permission: AppPermission): Boolean
}
