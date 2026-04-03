package com.igniteai.app.feature.session

import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Enables Do Not Disturb during active sessions.
 *
 * Uses NotificationManager's interruption filter to silence
 * all notifications while a session is running. Restores
 * the previous filter state when the session ends.
 *
 * Requires ACCESS_NOTIFICATION_POLICY permission.
 */
class NotificationSuppressor(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var previousFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL

    fun suppress() {
        if (!notificationManager.isNotificationPolicyAccessGranted) return

        previousFilter = notificationManager.currentInterruptionFilter
        notificationManager.setInterruptionFilter(
            NotificationManager.INTERRUPTION_FILTER_NONE,
        )
    }

    fun restore() {
        if (!notificationManager.isNotificationPolicyAccessGranted) return

        notificationManager.setInterruptionFilter(previousFilter)
    }

    companion object {
        fun hasPermission(context: Context): Boolean {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return nm.isNotificationPolicyAccessGranted
        }
    }
}
