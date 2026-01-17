package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetItemUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class BudgetViewModel(
    private val budgetsRepository: BudgetsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val transactionsRepository: TransactionsRepository,
) : ViewModel() {

    /**
     * Live UI state for the Budget screen.
     * Combines Budgets and Transactions to calculate real-time totals.
     */
    val uiState: StateFlow<BudgetUiState> =
        combine(
            budgetsRepository.getAllBudgets(),
            transactionsRepository.getAllTransactions() // ✅ Fetch all transactions
        ) { budgets, transactions ->

            // 1. Group transactions by their budgetId for fast lookup
            //    Map<Int, List<Transaction>>
            val transactionsByBudget = transactions.groupBy { it.budgetId }

            // 2. Map budgets to UI state using the calculated sums
            val budgetItems = budgets.map { budget ->

                // Get transactions for this specific budget
                val budgetTransactions = transactionsByBudget[budget.id] ?: emptyList()

                // ✅ Calculate Total Spent dynamically
                val totalSpent = budgetTransactions.sumOf { it.totalAmount }

                // ✅ Calculate Percentage dynamically
                val percentage = if (budget.limitAmount > 0) {
                    (totalSpent / budget.limitAmount).toFloat()
                } else {
                    0f
                }

                BudgetItemUiState(
                    id = budget.id,
                    name = budget.name,
                    amountUsed = totalSpent, // Use the calculated sum
                    limitAmount = budget.limitAmount,
                    percentage = percentage
                )
            }

            BudgetUiState(budgetItems = budgetItems)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = BudgetUiState()
            )

    /**
     * Creates and inserts a new budget into the database.
     */
    fun addBudget(
        name: String,
        limitAmount: Double,
        note: String?,
        startDate: Date,
        endDate: Date?
    ) {
        viewModelScope.launch {
            val newBudget = Budgets(
                id = 0,
                name = name,
                amount = 0.0, // Initial amount is 0, but UI will calculate real usage
                limitAmount = limitAmount,
                startDate = startDate,
                endDate = endDate,
                note = note
            )
            budgetsRepository.insert(newBudget)
        }
    }
}