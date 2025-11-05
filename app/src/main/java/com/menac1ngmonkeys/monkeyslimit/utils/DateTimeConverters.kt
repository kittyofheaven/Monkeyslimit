package com.menac1ngmonkeys.monkeyslimit.utils

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class DateTimeConverters {
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    //    tanggal & waktu
    @TypeConverter
    fun fromStringToDateTime(value: String?): Date? {
        return value?.let { dateTimeFormat.parse(it) }
    }

    @TypeConverter
    fun fromDateTimeToString(date: Date?): String? {
        return date?.let { dateTimeFormat.format(it) }
    }
}