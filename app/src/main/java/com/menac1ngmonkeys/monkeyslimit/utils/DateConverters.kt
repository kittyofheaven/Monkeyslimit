package com.menac1ngmonkeys.monkeyslimit.utils

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class DateConverters{
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    //    tanggal only
    @TypeConverter
    fun fromStringToDate(value: String?): Date? {
        return value?.let { dateFormat.parse(it) }
    }

    @TypeConverter
    fun fromDateToString(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }
}