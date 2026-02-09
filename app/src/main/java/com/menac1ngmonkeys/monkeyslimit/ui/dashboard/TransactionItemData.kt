package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

import androidx.annotation.DrawableRes

data class TransactionItemData(
    val id: Int, // Added ID
    @param:DrawableRes val iconResId: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val amount: Double,
    val isExpense: Boolean
)