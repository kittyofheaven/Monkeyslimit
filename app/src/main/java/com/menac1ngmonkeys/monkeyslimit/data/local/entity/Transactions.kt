package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.menac1ngmonkeys.monkeyslimit.utils.DateTimeConverters
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Budgets::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Categories::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("budgetId"), Index("categoryId")]
)
@TypeConverters(DateTimeConverters::class)
data class Transactions(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "totalAmount") val totalAmount: Double,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "imagePath") val imagePath: String?,
    @ColumnInfo(name = "budgetId") val budgetId: Int,
    @ColumnInfo(name = "categoryId") val categoryId: Int
)
