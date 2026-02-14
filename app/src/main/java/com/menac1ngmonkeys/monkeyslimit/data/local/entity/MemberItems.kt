package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memberitems",
    foreignKeys = [
        ForeignKey(
            entity = Members::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Items::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId"), Index("itemId")]
)
data class MemberItems(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "memberId") val memberId: Int,
    @ColumnInfo(name = "itemId") val itemId: Int,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "userId") val userId: String = ""
)
