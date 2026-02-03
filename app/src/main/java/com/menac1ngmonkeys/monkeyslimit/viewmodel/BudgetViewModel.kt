package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetItemUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.SortDirection
import com.menac1ngmonkeys.monkeyslimit.ui.state.SortType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class BudgetViewModel(
    private val budgetsRepository: BudgetsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val transactionsRepository: TransactionsRepository,
) : ViewModel() {

    // Internal state for Date Selection
    private val _selectedDateState = MutableStateFlow(Calendar.getInstance())
    private val _sortType = MutableStateFlow(SortType.NAME)
    private val _sortDirection = MutableStateFlow(SortDirection.ASCENDING)

    val uiState: StateFlow<BudgetUiState> =
        combine(
            budgetsRepository.getAllBudgets(),
            transactionsRepository.getAllTransactions(),
            _selectedDateState,
            _sortType,
            _sortDirection
        ) { budgets, allTransactions, selectedDateCal, sortType, sortDirection ->

            val selectedMonth = selectedDateCal.get(Calendar.MONTH)
            val selectedYear = selectedDateCal.get(Calendar.YEAR)

            // 1. Filter Transactions by Month/Year
            val filteredTransactions = allTransactions.filter { transaction ->
                val transCal = Calendar.getInstance().apply { time = transaction.date }
                transCal.get(Calendar.MONTH) == selectedMonth &&
                        transCal.get(Calendar.YEAR) == selectedYear
            }

            // 2. Group filtered transactions by budgetId
            val transactionsByBudget = filteredTransactions.groupBy { it.budgetId }

            // 3. Map budgets to UI state
            var budgetItems = budgets.map { budget ->
                val budgetTransactions = transactionsByBudget[budget.id] ?: emptyList()
                val totalSpent = budgetTransactions.sumOf { it.totalAmount }

                val percentage = if (budget.limitAmount > 0) {
                    (totalSpent / budget.limitAmount).toFloat()
                } else {
                    0f
                }

                BudgetItemUiState(
                    id = budget.id,
                    name = budget.name,
                    amountUsed = totalSpent,
                    limitAmount = budget.limitAmount,
                    percentage = percentage
                )
            }

            // 4. Apply Sorting
            budgetItems = when (sortType) {
                SortType.NAME -> {
                    if (sortDirection == SortDirection.ASCENDING) budgetItems.sortedBy { it.name.lowercase() }
                    else budgetItems.sortedByDescending { it.name.lowercase() }
                }
                SortType.AMOUNT_LIMIT -> {
                    if (sortDirection == SortDirection.ASCENDING) budgetItems.sortedBy { it.limitAmount }
                    else budgetItems.sortedByDescending { it.limitAmount }
                }
                SortType.AMOUNT_USED -> {
                    if (sortDirection == SortDirection.ASCENDING) budgetItems.sortedBy { it.amountUsed }
                    else budgetItems.sortedByDescending { it.amountUsed }
                }
            }

            // 5. Calculate Global Totals
            val totalLimit = budgets.sumOf { it.limitAmount }
            val totalSpentGlobal = budgetItems.sumOf { it.amountUsed }
            val totalLeft = totalLimit - totalSpentGlobal
            val overallPercentage = if (totalLimit > 0) {
                (totalSpentGlobal / totalLimit).toFloat()
            } else {
                0f
            }

            BudgetUiState(
                budgetItems = budgetItems,
                totalLimit = totalLimit,
                totalSpent = totalSpentGlobal,
                totalLeft = totalLeft,
                overallPercentage = overallPercentage,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                sortType = sortType,
                sortDirection = sortDirection
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = BudgetUiState()
            )

    fun updateMonth(monthIndex: Int) {
        _selectedDateState.update { current ->
            (current.clone() as Calendar).apply {
                set(Calendar.MONTH, monthIndex)
            }
        }
    }

    fun updateYear(increment: Int) {
        _selectedDateState.update { current ->
            (current.clone() as Calendar).apply {
                add(Calendar.YEAR, increment)
            }
        }
    }

    fun updateSort(type: SortType, direction: SortDirection) {
        _sortType.value = type
        _sortDirection.value = direction
    }

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
                amount = 0.0,
                limitAmount = limitAmount,
                startDate = startDate,
                endDate = endDate,
                note = note
            )
            budgetsRepository.insert(newBudget)
        }
    }
}