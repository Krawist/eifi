package com.eifi.core.data

/** Remembers which permissions were already requested, so that a refusal is not asked again at each action. */
interface PermissionRequestHistory {
    suspend fun wasRequested(permission: AppPermission): Boolean

    suspend fun markRequested(permission: AppPermission)
}
