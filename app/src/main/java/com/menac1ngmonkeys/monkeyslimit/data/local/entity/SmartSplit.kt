package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.*
import com.menac1ngmonkeys.monkeyslimit.utils.DateTimeConverters
import  java.util.Date

@Entity(tableName = "smartsplit")
@TypeConverters(DateTimeConverters::class)
data class SmartSplit(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "amountOwed") val amountOwed: Double,
    @ColumnInfo(name = "isPaid") val isPaid: Boolean = false,
    @ColumnInfo(name = "createdDate") val createDate: Date
)
