package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetItemUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetDetailUiState
import com.menac1ngmonkeys.monkeyslimit.viewmodel.helpers.toFormattedString
import com.menac1ngmonkeys.monkeyslimit.viewmodel.helpers.toTransactionItemData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

class BudgetDetailViewModel(
//    savedStateHandle: SavedStateHandle,
    budgetId: Int,
    budgetsRepository: BudgetsRepository,
    transactionsRepository: TransactionsRepository,
    categoriesRepository: CategoriesRepository,
) : ViewModel() {

    // 1. Get the budgetId from the navigation arguments (this is standard practice)
//    private val budgetId: Int = checkNotNull(savedStateHandle["budgetId"])

    val uiState: StateFlow<BudgetDetailUiState> =
        combine(
            // Get the specific budget by its ID
            budgetsRepository.getBudgetById(budgetId).filterNotNull(),
            // Get all transactions for that specific budget
            transactionsRepository.getTransactionsByBudgetId(budgetId),
            // Get all categories so we can map names/icons
            categoriesRepository.getAllCategories()
        ) { budget, transactions, categories ->

            // Map categories for quick lookup
            val categoriesById = categories.associateBy { it.id }

            // Map the list of transactions to the UI model
            val uiTransactions = transactions.map { transaction ->
                val category = categoriesById[transaction.categoryId]
                transaction.toTransactionItemData(category)
            }

            // Sum actual transactions
            val totalSpent = uiTransactions.sumOf { it.amount }

            val percentage = if (budget.limitAmount > 0) {
                (totalSpent / budget.limitAmount).toFloat()
            } else {
                0f
            }

            BudgetDetailUiState(
                budget = budget.toBudgetItemUiState(totalSpent, percentage),
                relatedTransactions = uiTransactions
            )

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = BudgetDetailUiState()
        )
}

// We can reuse these helper functions from other ViewModels.
// It's a good idea to move them to a common utility file later.
private fun Budgets.toBudgetItemUiState(
    totalSpent: Double = 0.0,
    percentage: Float = 0f
): BudgetItemUiState {
    return BudgetItemUiState(
        id = this.id,
        name = this.name,
        amountUsed = totalSpent,
        limitAmount = this.limitAmount,
        percentage = percentage
    )
}
