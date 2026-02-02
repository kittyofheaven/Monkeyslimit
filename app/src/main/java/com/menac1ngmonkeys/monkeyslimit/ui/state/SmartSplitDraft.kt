package com.menac1ngmonkeys.monkeyslimit.ui.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SmartSplitDraft(
    val splitName: String,
    val total: Double,
    val tax: Double,
    val service: Double,
    val discount: Double,
    val others: Double,
    val imageUri: String?,
    val members: List<DraftMember>,
    val items: List<DraftItem>
) : Parcelable

@Parcelize
data class DraftMember(
    val id: Int,
    val name: String,
    val contact: String?,
    val note: String?
) : Parcelable

@Parcelize
data class DraftItem(
    val name: String,
    val price: Double,
    val quantity: Int,
    val assignedMemberIds: List<Int>
) : Parcelable