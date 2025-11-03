package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.*

@Entity(
    tableName = "member",
    foreignKeys = [
        ForeignKey(
            entity = SmartSplit::class,
            parentColumns = ["id"],
            childColumns = ["smartSplitId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("smartSplitId")]
)
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "smartSplitId") val smartSplitId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "contact") val contact: String?,
    @ColumnInfo(name = "note") val note: String?
)
