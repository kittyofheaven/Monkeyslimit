package com.menac1ngmonkeys.monkeyslimit

import android.app.Application
import androidx.work.WorkManager
import com.menac1ngmonkeys.monkeyslimit.data.AppContainer
import com.menac1ngmonkeys.monkeyslimit.data.DefaultAppContainer
import com.menac1ngmonkeys.monkeyslimit.data.worker.NotificationHelper

class MonkeyslimitApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        // Start the reminder logic as soon as the app process begins
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.scheduleDailyReminders(this)
    }
}
