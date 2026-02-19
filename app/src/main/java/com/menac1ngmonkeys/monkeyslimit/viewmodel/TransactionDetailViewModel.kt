package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.TransactionDetailUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class TransactionDetailViewModel(
    private val transactionId: Int,
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val budgetsRepository: BudgetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState = _uiState.asStateFlow()

    // DELETED the hardcoded incomeCategoryNames list!

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val transaction = transactionsRepository.getTransactionById(transactionId).first()
            val allCategories = categoriesRepository.getAllCategories().first()
            val allBudgets = budgetsRepository.getAllBudgets().first()

            if (transaction != null) {
                val currentCategory = allCategories.find { it.id == transaction.categoryId }
                val currentBudget = allBudgets.find { it.id == transaction.budgetId }

                // Determine initial filtered list
                val initialFiltered = filterCategories(allCategories, transaction.type)

                _uiState.update {
                    it.copy(
                        transaction = transaction,
                        categoryName = currentCategory?.name ?: "Uncategorized",
                        budgetName = currentBudget?.name ?: "None",
                        allCategories = allCategories,
                        allBudgets = allBudgets,
                        availableCategories = initialFiltered, // Set initial list
                        isLoading = false,

                        editAmount = transaction.totalAmount.toBigDecimal().toPlainString(),
                        editNote = transaction.note ?: "",
                        editDate = transaction.date.time,
                        editCategoryId = transaction.categoryId,
                        editBudgetId = transaction.budgetId,
                        editType = transaction.type
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // UPDATED: Now dynamically filters based on the Category's actual database type!
    private fun filterCategories(all: List<Categories>, type: TransactionType): List<Categories> {
        return all.filter { category -> category.type == type }
    }

    fun toggleEditMode() {
        _uiState.update { state ->
            val isEditing = !state.isEditing
            val tx = state.transaction

            // Reset to original values when opening/closing
            val type = tx?.type ?: TransactionType.EXPENSE
            val filtered = filterCategories(state.allCategories, type)

            state.copy(
                isEditing = isEditing,
                editAmount = tx?.totalAmount?.toBigDecimal()?.toPlainString() ?: "",
                editNote = tx?.note ?: "",
                editDate = tx?.date?.time ?: System.currentTimeMillis(),
                editCategoryId = tx?.categoryId ?: 0,
                editBudgetId = tx?.budgetId,
                editType = type,
                availableCategories = filtered
            )
        }
    }

    fun onEditTypeChange(type: TransactionType) {
        _uiState.update { state ->
            // 1. Get new valid categories using the dynamic filter
            val newAvailable = filterCategories(state.allCategories, type)

            // 2. Check if current selection is still valid
            val currentId = state.editCategoryId
            val isCurrentValid = newAvailable.any { it.id == currentId }

            // 3. Reset to first valid option if current is invalid
            val newCategoryId = if (isCurrentValid) currentId else newAvailable.firstOrNull()?.id ?: 0

            // 4. Clear budget if switched to Income
            val newBudgetId = if (type == TransactionType.INCOME) null else state.editBudgetId

            state.copy(
                editType = type,
                availableCategories = newAvailable,
                editCategoryId = newCategoryId,
                editBudgetId = newBudgetId
            )
        }
    }

    fun onEditAmountChange(newAmount: String) {
        if (newAmount.all { it.isDigit() || it == '.' }) {
            _uiState.update { it.copy(editAmount = newAmount) }
        }
    }

    fun onEditNoteChange(newNote: String) {
        _uiState.update { it.copy(editNote = newNote) }
    }

    fun onEditDateChange(newTimestamp: Long?) {
        if (newTimestamp != null) {
            _uiState.update { it.copy(editDate = newTimestamp) }
        }
    }

    fun onEditCategoryChange(categoryId: Int) {
        _uiState.update { it.copy(editCategoryId = categoryId) }
    }

    fun onEditBudgetChange(budgetId: Int?) {
        _uiState.update { it.copy(editBudgetId = budgetId) }
    }

    fun deleteTransaction(onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionToDelete = _uiState.value.transaction
            if (transactionToDelete != null) {
                // Call your repository delete function
                transactionsRepository.delete(transactionToDelete)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        val currentTransaction = state.transaction ?: return
        val newAmount = state.editAmount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            val updatedTransaction = currentTransaction.copy(
                totalAmount = newAmount,
                note = state.editNote,
                date = Date(state.editDate),
                categoryId = state.editCategoryId,
                budgetId = state.editBudgetId,
                type = state.editType
            )

            transactionsRepository.update(updatedTransaction)

            val newCategoryName = state.allCategories.find { it.id == state.editCategoryId }?.name ?: "Unknown"
            val newBudgetName = state.allBudgets.find { it.id == state.editBudgetId }?.name ?: "None"

            _uiState.update {
                it.copy(
                    transaction = updatedTransaction,
                    categoryName = newCategoryName,
                    budgetName = newBudgetName,
                    isEditing = false
                )
            }
        }
    }
}