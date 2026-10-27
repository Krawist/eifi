package com.eifi.core.data

import kotlinx.serialization.Serializable

/** Permissions Eifi asks for, each just before the first action that needs it. */
@Serializable
enum class AppPermission {
    /** Reading the operators' SMS notifications, to confirm payments received (Receive mode). */
    NotificationAccess,

    /** Exemption from battery optimisation, so that the notification listener keeps running (Receive mode). */
    BatteryOptimisation,

    /** Placing the USSD call on the right SIM (`CALL_PHONE`, `READ_PHONE_STATE`), at the first payment. */
    PhoneCalls,

    /** Scanning the payment QR code, at the first scan. */
    Camera,
}
