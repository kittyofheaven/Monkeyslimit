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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class ReviewItemUi(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val budgetId: Int,
    val quantity: Int,
    val pricePerUnit: Double
)

data class ReviewTransactionUiState(
    val budgets: List<Budgets> = emptyList(),
    val categories: List<Categories> = emptyList(),
    val imageUri: String? = null,
    val isLoading: Boolean = true
)

class ReviewTransactionViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val budgetsRepository: BudgetsRepository,
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    // Internal state for the passed image URI
    private val _imageUri = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReviewTransactionUiState> = combine(
        budgetsRepository.getAllBudgets(),
        categoriesRepository.getAllCategories(),
        _imageUri
    ) { budgets, categories, imageUri ->
        ReviewTransactionUiState(
            budgets = budgets,
            categories = categories,
            imageUri = imageUri,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReviewTransactionUiState()
    )

    // Call this from NavGraph to set the image
    fun setImageUri(uri: String?) {
        _imageUri.update { uri }
    }

    fun saveTransaction(
        date: Date,
        reviewItems: List<ReviewItemUi>,
        type: TransactionType, // <--- ADDED THIS PARAMETER
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val currentImage = _imageUri.value

            reviewItems.forEach { item ->
                val lineItemTotal = item.pricePerUnit * item.quantity

                // Determine budget ID: Incomes don't have a budget, so set to null
                val finalBudgetId = if (type == TransactionType.EXPENSE) {
                    item.budgetId
                } else {
                    null
                }

                val newTransaction = Transactions(
                    id = 0,
                    date = date,
                    totalAmount = lineItemTotal,
                    note = if (item.quantity > 1) "${item.name} (x${item.quantity})" else item.name,
                    imagePath = currentImage,
                    budgetId = finalBudgetId, // Use the calculated ID (or null)
                    categoryId = item.categoryId,
                    type = type // <--- Save the Type (Income/Expense)
                )

                transactionsRepository.insert(newTransaction)
            }
            onSuccess()
        }
    }
}