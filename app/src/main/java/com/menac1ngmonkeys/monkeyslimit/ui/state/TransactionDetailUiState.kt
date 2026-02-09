package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions

data class TransactionDetailUiState(
    val transaction: Transactions? = null,
    val categoryName: String = "",
    val isLoading: Boolean = true,

    // Full list from DB
    val allCategories: List<Categories> = emptyList(),

    // Filtered list based on current editType (Income vs Expense)
    val availableCategories: List<Categories> = emptyList(),

    // --- Edit Mode State ---
    val isEditing: Boolean = false,
    val editAmount: String = "",
    val editNote: String = "",
    val editDate: Long = System.currentTimeMillis(),
    val editCategoryId: Int = 0,
    val editType: TransactionType = TransactionType.EXPENSE
)