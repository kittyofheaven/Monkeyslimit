package com.menac1ngmonkeys.monkeyslimit.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.menac1ngmonkeys.monkeyslimit.utils.DateConverters
import java.util.Date

@Entity(tableName = "budgets")
@TypeConverters(DateConverters::class)
data class Budgets(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "limitAmount") val limitAmount: Double,
    @ColumnInfo(name = "startDate") val startDate: Date,
    @ColumnInfo(name = "endDate") val endDate: Date?,
    @ColumnInfo(name = "note") val note: String?
)