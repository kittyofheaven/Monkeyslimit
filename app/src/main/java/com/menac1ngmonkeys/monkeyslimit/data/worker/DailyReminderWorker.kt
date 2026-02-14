package com.menac1ngmonkeys.monkeyslimit.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.menac1ngmonkeys.monkeyslimit.MainActivity
import com.menac1ngmonkeys.monkeyslimit.R

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create the Intent to open MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            // These flags ensure that if the app is already open,
            // it doesn't create a second "duplicate" version of the app.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 2. Wrap it in a PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0, // Request code
            intent,
            // Starting in Android 12+, you MUST specify FLAG_IMMUTABLE or FLAG_MUTABLE
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Important: Use Variant 9 (Hugging the Piggy Bank) for a positive vibe!
        val largeIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.positive_5)

        // 3. Build the notification
        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setContentTitle("Don't Forget! 🐒")
            .setContentText("Have you recorded your expenses today? Stay on top of your budget!")
            .setSmallIcon(R.drawable.logo_monkeys_limit) // Ensure this is a transparent-background PNG/SVG
            .setLargeIcon(largeIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            // 4. ATTACH THE INTENT
            .setContentIntent(pendingIntent)
            // 5. AUTO CANCEL: Removes the notification from the tray once clicked
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}