package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

data class ManualTransactionUiState(
    val budgets: List<Budgets> = emptyList(),
    val categories: List<Categories> = emptyList(),
    val isLoading: Boolean = true
)

class ManualTransactionViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val budgetsRepository: BudgetsRepository,
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    // Combine flows to get real-time updates for Dropdowns
    val uiState: StateFlow<ManualTransactionUiState> = combine(
        budgetsRepository.getAllBudgets(),
        categoriesRepository.getAllCategories()
    ) { budgets, categories ->
        ManualTransactionUiState(
            budgets = budgets,
            categories = categories,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ManualTransactionUiState()
    )

    fun saveTransaction(
        date: Date,
        amount: Double,
        name: String,
        categoryId: Int,
        budgetId: Int?,
        type: TransactionType,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Logic: Incomes don't have a budget, so force null if Income
            val finalBudgetId = if (type == TransactionType.EXPENSE) {
                budgetId
            } else {
                null
            }

            val newTransaction = Transactions(
                id = 0, // Auto-generate
                date = date,
                totalAmount = amount,
                note = name,
                imagePath = null, // Manual transactions usually don't have an image
                budgetId = finalBudgetId,
                categoryId = categoryId,
                type = type
            )

            transactionsRepository.insert(newTransaction)
            onSuccess()
        }
    }
}