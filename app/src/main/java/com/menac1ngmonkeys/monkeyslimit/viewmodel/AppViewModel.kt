package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date

/**
 * Aggregates cross-screen app metrics (balance, expense) from budgets and transactions.
 *
 * Exposes a hot [StateFlow] for UI collection.
 *
 * @param transactionsRepository source of transaction data.
 * @param budgetsRepository source of budget data.
 * @property appUiState combined UI-facing totals for balance and expense.
 */
class AppViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val budgetsRepository: BudgetsRepository
) : ViewModel() {
    val appUiState: StateFlow<AppUiState> =
        combine(
            flow = transactionsRepository.getAllTransactions(),
            flow2 = budgetsRepository.getAllBudgets()
        )
        { transactions, budgets ->
            // 1. Get Current Month & Year
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            // Helper to check if a date is in the current month
            fun isCurrentMonth(date: Date): Boolean {
                val itemCal = Calendar.getInstance()
                itemCal.time = date
                return itemCal.get(Calendar.MONTH) == currentMonth &&
                        itemCal.get(Calendar.YEAR) == currentYear
            }

            // 2. Filter Transactions for Current Month
            val filteredTransactions = transactions.filter { isCurrentMonth(it.date) }

            // 3. Filter Budgets for Current Month (based on startDate)
            val filteredBudgets = budgets.filter { isCurrentMonth(it.startDate) }

            // 4. Calculate Totals based on Filtered Data
            val totalExpense = filteredTransactions.sumOf { tx ->
                if (tx.type == TransactionType.EXPENSE) tx.totalAmount else 0.0
            }

            val totalIncome = filteredTransactions.sumOf { tx ->
                if (tx.type == TransactionType.INCOME) tx.totalAmount else 0.0
            }

            // Note: This sums the 'amount' (spent) of budgets started this month.
            val totalBalance = filteredBudgets.sumOf { it.amount }

            AppUiState(
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                totalBalance = totalBalance,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState()
        )
}