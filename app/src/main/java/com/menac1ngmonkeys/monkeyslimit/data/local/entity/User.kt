package com.menac1ngmonkeys.monkeyslimit.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Keep
@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String = "", // Use the Firebase User ID
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val job: String = "",
    val birthDate: Date = Date(0),
    val gender: String = "",
    val income: String = "",
    val isMarried: Boolean = false,
    val photoUrl: String? = null,
    val isSynced: Boolean = true
)