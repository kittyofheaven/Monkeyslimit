package com.menac1ngmonkeys.monkeyslimit.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {
    const val CHANNEL_ID = "daily_reminder_channel"

    fun scheduleDailyReminders(context: Context) {
        // Schedule 12 PM Reminder
        scheduleSpecificReminder(context, "Reminder_12PM", 12)
        // Schedule 9 PM Reminder
        scheduleSpecificReminder(context, "Reminder_9PM", 21)
    }

    // In NotificationHelper.kt
    fun cancelAllReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork("Reminder_12PM")
        workManager.cancelUniqueWork("Reminder_9PM")
    }

    private fun scheduleSpecificReminder(context: Context, uniqueName: String, hour: Int) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(uniqueName)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.KEEP, // Use REPLACE to update the schedule immediately
            workRequest
        )
    }

    // Pattern: Sleep 0ms, Vibrate 100ms, Sleep 100ms, Vibrate 100ms, Sleep 100ms, Vibrate 300ms
    // This creates a "tap-tap-TAPP" feel.
    private val monkeyTapPattern = longArrayOf(0, 100, 100, 100, 100, 300)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH // HIGH makes it pop up/heads-up
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = "Reminds you to log daily transactions"
                // Ensure it makes sound/vibration to "pop up"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = monkeyTapPattern
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}