package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import java.util.Calendar
import java.util.Date

object SeedUtils {
    fun firstDayOfCurrentMonth(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    fun daysAgo(n: Int): Date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -n) }.time

    fun daysFromNow(n: Int): Date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, n) }.time
}

