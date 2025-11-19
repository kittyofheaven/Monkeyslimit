package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

// A simple data class to hold all the information for the dashboard UI
data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val totalExpense: Double = 0.0,
    // You can add more fields here later, like a list of recent transactions
    // This creates an empty list ready to hold our transactions.
    val recentTransactions: List<TransactionItemData> = emptyList()
)