package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.*

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = SmartSplits::class,
            parentColumns = ["id"],
            childColumns = ["smartSplitId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("smartSplitId")]
)
data class Items(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "smartSplitId") val smartSplitId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "totalPrice") val totalPrice: Double,
    @ColumnInfo(name = "userId") val userId: String = ""
)
