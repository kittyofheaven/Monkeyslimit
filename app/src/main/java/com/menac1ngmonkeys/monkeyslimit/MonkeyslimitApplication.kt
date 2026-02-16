package com.menac1ngmonkeys.monkeyslimit

import android.app.Application
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.data.AppContainer
import com.menac1ngmonkeys.monkeyslimit.data.DefaultAppContainer
import com.menac1ngmonkeys.monkeyslimit.data.worker.NotificationHelper
import kotlinx.coroutines.launch

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

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        if (uid != null) {
            // Use a Coroutine to check the local DB preference
            kotlinx.coroutines.MainScope().launch {
                container.usersRepository.getUser(uid).collect { user ->
                    if (user?.isNotificationEnabled == true) {
                        NotificationHelper.scheduleDailyReminders(this@MonkeyslimitApplication)
                    } else {
                        NotificationHelper.cancelAllReminders(this@MonkeyslimitApplication)
                    }
                }
            }
        }
    }
}
