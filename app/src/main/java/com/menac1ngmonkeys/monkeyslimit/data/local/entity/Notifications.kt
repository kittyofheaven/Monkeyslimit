package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.*
import com.menac1ngmonkeys.monkeyslimit.utils.DateTimeConverters
import java.util.Date

@Entity(tableName = "notifications")
@TypeConverters(DateTimeConverters::class)
data class Notifications(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "isCompleted") val isCompleted: Boolean = false,
    @ColumnInfo(name = "userId") val userId: String = ""
)
