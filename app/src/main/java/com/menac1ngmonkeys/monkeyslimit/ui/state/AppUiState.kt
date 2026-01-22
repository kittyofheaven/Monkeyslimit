package com.menac1ngmonkeys.monkeyslimit.ui.state

/**
 * Cross-screen totals for balance and expense.
 *
 * @property totalBalance sum of all budgets.
 * @property totalExpense sum of all expenses.
 */
data class AppUiState(
    val totalBalance: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0
)