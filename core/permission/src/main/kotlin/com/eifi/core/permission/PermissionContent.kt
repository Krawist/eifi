package com.eifi.core.permission

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.eifi.core.data.AppPermission
import com.eifi.core.design.R as DesignR

/** Texts of a permission: explanation screen and settings row. */
internal data class PermissionContent(
    @StringRes val name: Int,
    @StringRes val title: Int,
    @StringRes val body: Int,
    val points: List<Int>,
    @StringRes val allow: Int,
    @StringRes val later: Int,
    @DrawableRes val icon: Int,
)

internal val AppPermission.content: PermissionContent
    get() = when (this) {
        AppPermission.NotificationAccess -> PermissionContent(
            name = R.string.permission_name_notification,
            title = R.string.permission_notification_title,
            body = R.string.permission_notification_body,
            points = listOf(
                R.string.permission_notification_point_operator,
                R.string.permission_notification_point_local,
                R.string.permission_point_revocable,
            ),
            allow = R.string.permission_notification_allow,
            later = R.string.permission_notification_later,
            icon = DesignR.drawable.ic_eifi_message,
        )
        AppPermission.BatteryOptimisation -> PermissionContent(
            name = R.string.permission_name_battery,
            title = R.string.permission_battery_title,
            body = R.string.permission_battery_body,
            points = listOf(
                R.string.permission_battery_point_list,
                R.string.permission_battery_point_usage,
                R.string.permission_point_revocable,
            ),
            allow = R.string.permission_battery_allow,
            later = R.string.permission_battery_later,
            icon = DesignR.drawable.ic_eifi_battery,
        )
        AppPermission.PhoneCalls -> PermissionContent(
            name = R.string.permission_name_phone,
            title = R.string.permission_phone_title,
            body = R.string.permission_phone_body,
            points = listOf(
                R.string.permission_phone_point_pin,
                R.string.permission_phone_point_calls,
                R.string.permission_point_revocable,
            ),
            allow = R.string.permission_phone_allow,
            later = R.string.permission_phone_later,
            icon = DesignR.drawable.ic_eifi_phone_tap,
        )
        AppPermission.Camera -> PermissionContent(
            name = R.string.permission_name_camera,
            title = R.string.permission_camera_title,
            body = R.string.permission_camera_body,
            points = listOf(
                R.string.permission_camera_point_photos,
                R.string.permission_camera_point_nfc,
                R.string.permission_point_revocable,
            ),
            allow = R.string.permission_camera_allow,
            later = R.string.permission_camera_later,
            icon = DesignR.drawable.ic_eifi_scan,
        )
    }
