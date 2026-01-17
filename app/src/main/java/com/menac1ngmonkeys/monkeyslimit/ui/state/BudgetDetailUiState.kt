package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData

/**
 * Represents the UI state for the Budget Detail screen.
 *
 * @param budget The details of the selected budget being viewed.
 * @param relatedTransactions The list of transactions associated with this budget.
 */
data class BudgetDetailUiState(
    val budget: BudgetItemUiState? = null,
    val relatedTransactions: List<TransactionItemData> = emptyList()
)
