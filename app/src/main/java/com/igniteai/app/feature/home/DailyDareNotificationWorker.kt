package com.igniteai.app.feature.home

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.igniteai.app.MainActivity
import com.igniteai.app.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that fires the daily dare notification.
 *
 * Scheduled as a PeriodicWorkRequest (24-hour interval).
 * The notification is deliberately discreet — no mention of
 * the app's intimate nature. Uses the decoy app name if
 * decoy mode is enabled.
 *
 * Privacy design:
 * - Channel name: "Reminders" (generic)
 * - Notification text: "You have something waiting" (vague)
 * - No lock screen preview (VISIBILITY_PRIVATE)
 * - Tapping opens the app normally (no deep link to specific content)
 */
class DailyDareNotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "daily_dare"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "daily_dare_notification"

        /**
         * Schedule the daily notification.
         *
         * @param context Application context
         * @param hour Hour of day (0-23) for notification
         * @param minute Minute of hour (0-59)
         */
        fun schedule(context: Context, hour: Int = 20, minute: Int = 0) {
            // Calculate initial delay until next occurrence of the target time
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_MONTH, 1) // Schedule for tomorrow
                }
            }
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyDareNotificationWorker>(
                24, TimeUnit.HOURS,
                15, TimeUnit.MINUTES, // Flex window
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        /**
         * Cancel the daily notification schedule.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Create the notification channel (required for Android 8+).
         */
        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Daily reminders"
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result {
        createChannel(applicationContext)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reminder")
            .setContentText("You have something waiting")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success() // Can't show — permission not granted
            }
        }

        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID, notification)

        return Result.success()
    }
}
