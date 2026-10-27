package com.eifi.core.sms

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Listens to the SMS notifications, once the user granted the notification access.
 *
 * TODO(EIFI-105, EIFI-206): filter the operators' senders and parse the credit and debit SMS.
 */
internal class EifiNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(notification: StatusBarNotification) = Unit
}
