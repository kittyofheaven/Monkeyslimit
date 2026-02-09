package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.TransactionDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class TransactionDetailViewModel(
    private val transactionId: Int,
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState = _uiState.asStateFlow()

    // Hardcoded list of Income Categories
    private val incomeCategoryNames = listOf("Salary", "Allowance", "Bonus", "Investment", "Petty Cash")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val transaction = transactionsRepository.getTransactionById(transactionId).first()
            val allCategories = categoriesRepository.getAllCategories().first()

            if (transaction != null) {
                val currentCategory = allCategories.find { it.id == transaction.categoryId }

                // Determine initial filtered list
                val initialFiltered = filterCategories(allCategories, transaction.type)

                _uiState.update {
                    it.copy(
                        transaction = transaction,
                        categoryName = currentCategory?.name ?: "Uncategorized",
                        allCategories = allCategories,
                        availableCategories = initialFiltered, // Set initial list
                        isLoading = false,

                        editAmount = transaction.totalAmount.toString(),
                        editNote = transaction.note ?: "",
                        editDate = transaction.date.time,
                        editCategoryId = transaction.categoryId,
                        editType = transaction.type
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Helper to filter categories based on type
    private fun filterCategories(all: List<Categories>, type: TransactionType): List<Categories> {
        return all.filter { category ->
            if (type == TransactionType.INCOME) category.name in incomeCategoryNames
            else category.name !in incomeCategoryNames
        }
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
                editAmount = tx?.totalAmount?.toString() ?: "",
                editNote = tx?.note ?: "",
                editDate = tx?.date?.time ?: System.currentTimeMillis(),
                editCategoryId = tx?.categoryId ?: 0,
                editType = type,
                availableCategories = filtered
            )
        }
    }

    fun onEditTypeChange(type: TransactionType) {
        _uiState.update { state ->
            // 1. Get new valid categories
            val newAvailable = filterCategories(state.allCategories, type)

            // 2. Check if current selection is still valid
            val currentId = state.editCategoryId
            val isCurrentValid = newAvailable.any { it.id == currentId }

            // 3. Reset to first valid option if current is invalid
            val newCategoryId = if (isCurrentValid) currentId else newAvailable.firstOrNull()?.id ?: 0

            state.copy(
                editType = type,
                availableCategories = newAvailable,
                editCategoryId = newCategoryId
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
                type = state.editType
            )

            transactionsRepository.update(updatedTransaction)

            val newCategoryName = state.allCategories.find { it.id == state.editCategoryId }?.name ?: "Unknown"

            _uiState.update {
                it.copy(
                    transaction = updatedTransaction,
                    categoryName = newCategoryName,
                    isEditing = false
                )
            }
        }
    }
}