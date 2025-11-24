package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
            val totalExpense = transactions.sumOf { it.totalAmount }
            val totalBalance = budgets.sumOf { it.amount }

            AppUiState(
                totalExpense = totalExpense,
                totalBalance = totalBalance,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState()
        )
}