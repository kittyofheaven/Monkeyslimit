package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData

enum class DashboardFilter {
    ALL,
    INCOME,
    EXPENSE
}

/**
 * UI-facing snapshot of dashboard content.
 *
 * @property recentTransactions list of items to render in the dashboard feed.
 */
data class DashboardUiState(
    // You can add more fields here later, like a list of recent transactions
    // This creates an empty list ready to hold our transactions.
    val recentTransactions: List<TransactionItemData> = emptyList(),
    val currentFilter: DashboardFilter = DashboardFilter.ALL
)