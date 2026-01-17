package com.menac1ngmonkeys.monkeyslimit.ui.state

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

/**
 * Represents the overall state for the Budget screen.
 *
 * @param budgetItems The list of all budget items to be displayed.
 */
data class BudgetUiState(
    val budgetItems: List<BudgetItemUiState> = emptyList()
)
