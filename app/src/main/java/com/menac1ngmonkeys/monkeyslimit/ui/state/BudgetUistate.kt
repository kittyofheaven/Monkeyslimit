package com.menac1ngmonkeys.monkeyslimit.ui.state

import java.util.Calendar

/**
 * Represents the state for a single budget item shown in the list.
 *
 * @param name The name of the budget (e.g., "Cigarettes", "Investing").
 * @param amountUsed The amount currently spent or allocated for this budget.
 * @param limitAmount The total limit for this budget.
 * @param percentage The calculated percentage of the budget used (amountUsed / limitAmount).
 */
data class BudgetItemUiState(
    val id: Int,
    val name: String,
    val amountUsed: Double,
    val limitAmount: Double,
    val percentage: Float,
)

enum class SortType {
    NAME,
    AMOUNT_LIMIT,
    AMOUNT_USED
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

/**
 * Represents the overall state for the Budget screen.
 * Added totals to support the header summary view.
 */
data class BudgetUiState(
    val budgetItems: List<BudgetItemUiState> = emptyList(),
    val totalLimit: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalLeft: Double = 0.0,
    val overallPercentage: Float = 0.0f,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-11
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val sortType: SortType = SortType.NAME,
    val sortDirection: SortDirection = SortDirection.ASCENDING
)
