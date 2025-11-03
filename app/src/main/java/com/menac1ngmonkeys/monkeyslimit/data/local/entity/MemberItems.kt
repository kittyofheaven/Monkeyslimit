package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.*

@Entity(
    tableName = "memberitems",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Item::class,
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
    @ColumnInfo(name = "quantity") val quantity: Int
)
