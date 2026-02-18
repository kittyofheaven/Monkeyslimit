package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets // <-- ADDED IMPORT
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions

data class TransactionDetailUiState(
    val transaction: Transactions? = null,
    val categoryName: String = "",
    val budgetName: String? = null, // <-- ADDED: Holds the name of the budget for view mode
    val isLoading: Boolean = true,

    // Full list from DB
    val allCategories: List<Categories> = emptyList(),
    val allBudgets: List<Budgets> = emptyList(), // <-- ADDED: Holds all budgets for the dropdown

    // Filtered list based on current editType (Income vs Expense)
    val availableCategories: List<Categories> = emptyList(),

    // --- Edit Mode State ---
    val isEditing: Boolean = false,
    val editAmount: String = "",
    val editNote: String = "",
    val editDate: Long = System.currentTimeMillis(),
    val editCategoryId: Int = 0,
    val editBudgetId: Int? = null, // <-- ADDED: Holds the selected Budget ID during editing
    val editType: TransactionType = TransactionType.EXPENSE
)