package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
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
data class Members(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "smartSplitId") val smartSplitId: Int? = null,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "contact") val contact: String? = null,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "userId") val userId: String = ""
)
