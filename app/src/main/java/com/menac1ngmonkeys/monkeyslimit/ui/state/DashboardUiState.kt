package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData

// A simple data class to hold all the information for the dashboard UI
data class DashboardUiState(
    // You can add more fields here later, like a list of recent transactions
    // This creates an empty list ready to hold our transactions.
    val recentTransactions: List<TransactionItemData> = emptyList(),
)