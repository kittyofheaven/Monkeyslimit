package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.*
import com.menac1ngmonkeys.monkeyslimit.utils.DateTimeConverters
import java.util.Date

@Entity(
    tableName = "transaction",
    foreignKeys = [
        ForeignKey(
            entity = Budget::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("budgetId"), Index("categoryId")]
)
@TypeConverters(DateTimeConverters::class)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "totalAmount") val totalAmount: Double,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "imagePath") val imagePath: String?,
    @ColumnInfo(name = "budgetId") val budgetId: Int,
    @ColumnInfo(name = "categoryId") val categoryId: Int
)
