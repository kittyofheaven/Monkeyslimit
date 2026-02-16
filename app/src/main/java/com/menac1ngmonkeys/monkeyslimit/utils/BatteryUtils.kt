package com.menac1ngmonkeys.monkeyslimit.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object BatteryUtils {

    /**
     * Checks if the app is currently ignored by Android's Battery Optimization.
     * Returns true if restrictions are OFF (which is what we want for WorkManager).
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        // Versions before Android M don't have this specific optimization
        return true
    }

    /**
     * Opens the system battery settings so the user can manually set to "No restrictions"
     */
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}