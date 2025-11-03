package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "icon") val icon: String?,
    @ColumnInfo(name = "description") val description: String?
)