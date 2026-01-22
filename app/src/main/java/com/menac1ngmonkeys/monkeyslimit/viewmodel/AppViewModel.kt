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
            val totalExpense = transactions.sumOf { tx ->
                if (tx.type == TransactionType.EXPENSE) tx.totalAmount else 0.0
            }
            val totalIncome = transactions.sumOf { tx ->
                if (tx.type == TransactionType.INCOME) tx.totalAmount else 0.0
            }
            val totalBalance = budgets.sumOf { it.amount }

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