package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetItemUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetDetailUiState
import com.menac1ngmonkeys.monkeyslimit.viewmodel.helpers.toTransactionItemData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetDetailViewModel(
    private val budgetId: Int,
    private val budgetsRepository: BudgetsRepository,
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository,
) : ViewModel() {

    private val _filterDate = MutableStateFlow(Calendar.getInstance())

    val uiState: StateFlow<BudgetDetailUiState> =
        combine(
            budgetsRepository.getBudgetById(budgetId).filterNotNull(),
            transactionsRepository.getTransactionsByBudgetId(budgetId),
            categoriesRepository.getAllCategories(),
            budgetsRepository.getAllBudgets(), // 1. Observe all budgets
            _filterDate
        ) { budget, allTransactions, categories, allBudgets, filterDate ->

            val selectedMonth = filterDate.get(Calendar.MONTH)
            val selectedYear = filterDate.get(Calendar.YEAR)

            val filteredTransactions = allTransactions.filter { transaction ->
                val tCal = Calendar.getInstance().apply { time = transaction.date }
                tCal.get(Calendar.MONTH) == selectedMonth &&
                        tCal.get(Calendar.YEAR) == selectedYear
            }

            val categoriesById = categories.associateBy { it.id }

            val uiTransactions = filteredTransactions.map { transaction ->
                val category = categoriesById[transaction.categoryId]
                transaction.toTransactionItemData(category)
            }

            val totalSpent = uiTransactions.sumOf { it.amount }

            val percentage = if (budget.limitAmount > 0) {
                (totalSpent / budget.limitAmount).toFloat()
            } else {
                0f
            }

            // 2. Filter out current budget to get names for validation
            val otherNames = allBudgets
                .filter { it.id != budgetId }
                .map { it.name }

            BudgetDetailUiState(
                budget = budget.toBudgetItemUiState(totalSpent, percentage),
                relatedTransactions = uiTransactions,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                otherBudgetNames = otherNames // 3. Pass to state
            )

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = BudgetDetailUiState()
        )

    fun setDateFilter(month: Int, year: Int) {
        _filterDate.update { current ->
            (current.clone() as Calendar).apply {
                set(Calendar.MONTH, month)
                set(Calendar.YEAR, year)
            }
        }
    }

    fun updateBudget(newName: String, newLimit: Double) {
        viewModelScope.launch {
            val currentBudget = budgetsRepository.getBudgetById(budgetId).firstOrNull()
            if (currentBudget != null) {
                val updatedBudget = currentBudget.copy(name = newName, limitAmount = newLimit)
                budgetsRepository.update(updatedBudget)
            }
        }
    }

    fun deleteBudget(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentBudget = budgetsRepository.getBudgetById(budgetId).firstOrNull()
            if (currentBudget != null) {
                budgetsRepository.delete(currentBudget)
                onSuccess()
            }
        }
    }
}

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