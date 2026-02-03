package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(
    tableName = "categories",
    // THIS IS THE FIX: unique = true ensures no duplicates
    indices = [Index(value = ["name", "type"], unique = true)]
)
data class Categories(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "icon") val icon: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "type") val type: TransactionType = TransactionType.EXPENSE,
)