package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.DashboardUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// The ViewModel takes the Repository as a dependency
class DashboardViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val budgetsRepository: BudgetsRepository,
) : ViewModel() {

    // This is a "cold flow" that will emit a new list whenever the database changes.
    val dashboardUiState: StateFlow<DashboardUiState> =
        // This `combine` function looks at both lists at once.
        combine(
            flow = transactionsRepository.getAllTransactions(),
            flow2 = categoriesRepository.getAllCategories(),
            flow3 = budgetsRepository.getAllBudgets()
        )
        { transactions, categories, budgets ->
                // Inside here, we have the list of `transactions`, `budgets`, and `categories`.

                // For now, let's assume all transactions are expenses.
                // Later, you might add a 'type' field to your Transactions entity
                // to distinguish between income and expense.
                val totalExpense = transactions.sumOf { it.totalAmount }

                // TODO: You would get the total balance from an AccountsRepository in the same way
                val totalBalance = budgets.sumOf { it.amount }

                // Create a fast way to look up categories by their ID
                val categoriesById = categories.associateBy { it.id }

                // --- Convert the list for the UI ---
                val uiTransactionList = transactions.map { singleTransaction ->

                    val category = categoriesById[singleTransaction.categoryId]
                    // For each transaction from the database, convert it to a UI-friendly format
                    singleTransaction.toTransactionItemData(category = category)
                }

            _root_ide_package_.com.menac1ngmonkeys.monkeyslimit.ui.dashboard.DashboardUiState(
                totalBalance = totalBalance,
                totalExpense = totalExpense,
                recentTransactions = uiTransactionList
            )
            }.stateIn( // 3. Convert the Flow into a StateFlow for the UI to collect
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = _root_ide_package_.com.menac1ngmonkeys.monkeyslimit.ui.dashboard.DashboardUiState() // The UI will show this initially
            )

}

// This is a "translator" function. It converts a database object to a UI object.
private fun Transactions.toTransactionItemData(category: Categories?): com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData {
    // If for some reason a category was not found, we'll use a default.
    val realCategory = category ?: Categories(
        id = 0,
        name = "Other",
        icon = null,
        description = null
    )
    val isExpense = (realCategory.name != "Salary") // Simple logic: assume anything not salary is an expense

    val icon = when (realCategory.name) {
        "Food and Beverages" -> R.drawable.food
        "Transport" -> R.drawable.directions_car_48px
        "Shopping" -> R.drawable.expense
        "Bills" -> R.drawable.receipt_long_48px
        "Entertainment" -> R.drawable.playing_cards_48px
        "Health" -> R.drawable.heart_check_48px
        "Education" -> R.drawable.expense
        "Salary" -> R.drawable.salary
        else -> R.drawable.savings_bold_48px
    }

//    Log.d("TransactionItemData", "iconResId: $icon")
//    Log.d("TransactionItemData", "realCategory.id: ${realCategory.id}")
//    Log.d("TransactionItemData", "realCategory.name: ${realCategory.name}")

    return _root_ide_package_.com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData(
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