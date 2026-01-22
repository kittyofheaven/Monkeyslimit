package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardFilter
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// The ViewModel takes the Repository as a dependency
/**
 * Produces dashboard UI state by combining transactions, categories, and budgets.
 *
 * Sorts transactions, maps them to UI-friendly items, and emits them as [StateFlow].
 *
 * @param transactionsRepository source of transactions.
 * @param categoriesRepository source of categories for labeling/icon mapping.
 * @param budgetsRepository source of budgets (used for totals if needed).
 * @property dashboardUiState live dashboard state consumed by the UI.
 */
class DashboardViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val budgetsRepository: BudgetsRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(DashboardFilter.ALL)

    // This is a "cold flow" that will emit a new list whenever the database changes.
    val dashboardUiState: StateFlow<DashboardUiState> =
        // This `combine` function looks at both lists at once.
        combine(
            flow = transactionsRepository.getAllTransactions(),
            flow2 = categoriesRepository.getAllCategories(),
            flow3 = budgetsRepository.getAllBudgets(),
            flow4 = _filter
        )
        { transactions, categories, budgets, currentFilter ->
                // Inside here, we have the list of `transactions`, `budgets`, and `categories`.

                // Create a fast way to look up categories by their ID
                val categoriesById = categories.associateBy { it.id }

                val filteredTransactions = when (currentFilter) {
                    DashboardFilter.ALL -> transactions
                    DashboardFilter.INCOME -> transactions.filter { it.type == TransactionType.INCOME }
                    DashboardFilter.EXPENSE -> transactions.filter { it.type == TransactionType.EXPENSE }
                }

                val sortedTransactions = filteredTransactions.sortedByDescending { it.date }

                // --- Convert the list for the UI ---
                val uiTransactionList = sortedTransactions.map { singleTransaction ->

                    val category = categoriesById[singleTransaction.categoryId]
                    // For each transaction from the database, convert it to a UI-friendly format
                    singleTransaction.toTransactionItemData(category = category)
                }

                DashboardUiState(
                    recentTransactions = uiTransactionList,
                    currentFilter = currentFilter
                )
            }.stateIn( // 3. Convert the Flow into a StateFlow for the UI to collect
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = DashboardUiState() // The UI will show this initially
            )

    fun updateFilter(newFilter: DashboardFilter) {
        _filter.update { newFilter }
    }

}

/**
 * Maps a database transaction to a UI model with formatted fields and icon.
 * This is a "translator" function. It converts a database object to a UI object.
 * 
 * @param category category matched by transaction.categoryId (nullable).
 * @return UI-ready transaction item for display.
 */
private fun Transactions.toTransactionItemData(category: Categories?): TransactionItemData {
    // If for some reason a category was not found, we'll use a default.
    val realCategory = category ?: Categories(
        id = 0,
        name = "Other",
        icon = null,
        description = null
    )
    val isExpense = (this.type == TransactionType.EXPENSE) // Simple logic: assume anything not salary is an expense

    val icon = when (realCategory.name) {
        "Food and Beverages" -> R.drawable.food
        "Transport" -> R.drawable.directions_car_48px
        "Shopping" -> R.drawable.shopping_bag_48px
        "Bills" -> R.drawable.receipt_long_48px
        "Entertainment" -> R.drawable.playing_cards_48px
        "Health" -> R.drawable.heart_check_48px
        "Education" -> R.drawable.cognition_2_48px
        "Salary" -> R.drawable.salary
        else -> R.drawable.paid_48px
    }

//    Log.d("TransactionItemData", "iconResId: $icon")
//    Log.d("TransactionItemData", "realCategory.id: ${realCategory.id}")
//    Log.d("TransactionItemData", "realCategory.name: ${realCategory.name}")

    return TransactionItemData(
        iconResId = icon,
        title = this.note ?: "Transaction",
        subtitle = this.date.toFormattedString(), // We use a helper for the date
        category = realCategory.name,
        amount = this.totalAmount,
        isExpense = isExpense
    )
}

// A helper function to make the date look nice, like "17:00 - Apr 24"
private fun Date.toFormattedString(): String {
    val formatter = SimpleDateFormat("HH:mm - MMM dd", Locale.getDefault())
    return formatter.format(this)
}