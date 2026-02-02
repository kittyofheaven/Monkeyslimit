package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.*
import com.menac1ngmonkeys.monkeyslimit.utils.DateTimeConverters
import  java.util.Date

@Entity(tableName = "smartsplits")
@TypeConverters(DateTimeConverters::class)
data class SmartSplits(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "amountOwed") val amountOwed: Double,

    @ColumnInfo(name = "imagePath") val imagePath: String? = null,

    @ColumnInfo(name = "tax") val tax: Double = 0.0,
    @ColumnInfo(name = "service") val service: Double = 0.0,
    @ColumnInfo(name = "discount") val discount: Double = 0.0,
    @ColumnInfo(name = "others") val others: Double = 0.0,

    @ColumnInfo(name = "isPaid") val isPaid: Boolean = false,
    @ColumnInfo(name = "createdDate") val createDate: Date
)
