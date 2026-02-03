package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData
import java.util.Calendar

/**
 * Represents the UI state for the Budget Detail screen.
 *
 * @param budget The details of the selected budget being viewed.
 * @param relatedTransactions The list of transactions associated with this budget.
 * @param otherBudgetNames Names of all other budgets (used for uniqueness validation).
 */
data class BudgetDetailUiState(
    val budget: BudgetItemUiState? = null,
    val relatedTransactions: List<TransactionItemData> = emptyList(),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val otherBudgetNames: List<String> = emptyList() // Added for validation
)