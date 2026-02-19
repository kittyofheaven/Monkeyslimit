package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.UsersRepository
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardFilter
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardNotification
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardUiState
import com.menac1ngmonkeys.monkeyslimit.ui.transaction.formatCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository,
    private val budgetsRepository: BudgetsRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(DashboardFilter.ALL)
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis()) // <-- ADDED

    // Group the UI states to avoid exceeding the `combine` flow parameter limits
    private val _uiControlsFlow = combine(_filter, _selectedDate) { filter, date -> Pair(filter, date) }

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val dashboardUiState: StateFlow<DashboardUiState> =
        combine(
            transactionsRepository.getAllTransactions(),
            categoriesRepository.getAllCategories(),
            budgetsRepository.getAllBudgets(),
            _uiControlsFlow, // <-- Use the grouped flow here
            usersRepository.getUser(currentUserId)
        )
        { transactions, categories, budgets, uiControls, user ->
            val currentFilter = uiControls.first
            val selectedDateMillis = uiControls.second

            // 1. Determine Selected Date Info (For the Transaction List)
            val selectedCalendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            val selDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)
            val selMonth = selectedCalendar.get(Calendar.MONTH)
            val selYear = selectedCalendar.get(Calendar.YEAR)

            val monthList = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthStr = monthList[selMonth]

            // 2. Filter Transactions for the exact Selected Date
            val selectedDateTransactions = transactions.filter { transaction ->
                val txCalendar = Calendar.getInstance().apply { time = transaction.date }
                txCalendar.get(Calendar.MONTH) == selMonth &&
                        txCalendar.get(Calendar.YEAR) == selYear &&
                        txCalendar.get(Calendar.DAY_OF_MONTH) == selDay
            }

            // 3. Filter Transactions for the ENTIRE Month (For Budget Notifications)
            val todayCalendar = Calendar.getInstance()
            val currMonth = todayCalendar.get(Calendar.MONTH)
            val currYear = todayCalendar.get(Calendar.YEAR)

            val currentMonthTransactions = transactions.filter { transaction ->
                val txCalendar = Calendar.getInstance().apply { time = transaction.date }
                txCalendar.get(Calendar.MONTH) == currMonth && txCalendar.get(Calendar.YEAR) == currYear
            }

            val categoriesById = categories.associateBy { it.id }

            // 4. Apply Dashboard Filters to the Selected Date
            val filteredTransactions = when (currentFilter) {
                DashboardFilter.ALL -> selectedDateTransactions
                DashboardFilter.INCOME -> selectedDateTransactions.filter { it.type == TransactionType.INCOME }
                DashboardFilter.EXPENSE -> selectedDateTransactions.filter { it.type == TransactionType.EXPENSE }
            }

            // 5. Generate Notification (Using the entire month's transactions, not just today's!)
            val userName = if (!user?.firstName.isNullOrBlank()) user!!.firstName else "Friend"
            val notification = generateNotification(currentMonthTransactions, budgets, userName)

            val sortedTransactions = filteredTransactions.sortedByDescending { it.date }

            val uiTransactionList = sortedTransactions.map { singleTransaction ->
                val category = categoriesById[singleTransaction.categoryId]
                singleTransaction.toTransactionItemData(category = category)
            }

            DashboardUiState(
                recentTransactions = uiTransactionList,
                currentFilter = currentFilter,
                currentMonth = monthStr,
                notification = notification,
                selectedDateMillis = selectedDateMillis // <-- ADDED
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = DashboardUiState()
        )

    fun updateFilter(newFilter: DashboardFilter) {
        _filter.update { newFilter }
    }

    // <-- ADDED FUNCTION FOR THE DATE SWITCHER -->
    fun updateDate(dateMillis: Long) {
        _selectedDate.update { dateMillis }
    }
}

// --- RANDOMIZED NOTIFICATION LOGIC ---
private fun generateNotification(
    transactions: List<Transactions>,
    budgets: List<Budgets>,
    userName: String
): DashboardNotification {

    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }

    // 1. Check for OVER BUDGET (Critical Alert)
    val spendingByBudget = transactions
        .filter { it.type == TransactionType.EXPENSE && it.budgetId != null }
        .groupBy { it.budgetId }
        .mapValues { (_, txs) -> txs.sumOf { it.totalAmount } }

    var worstBudget: Budgets? = null
    var maxOverPercentage = 0.0

    for (budget in budgets) {
        val spent = spendingByBudget[budget.id] ?: 0.0
        if (budget.limitAmount > 0) {
            val percentage = spent / budget.limitAmount
            if (percentage > 1.0 && percentage > maxOverPercentage) {
                maxOverPercentage = percentage
                worstBudget = budget
            }
        }
    }

    if (worstBudget != null) {
        val percentStr = formatCurrency((maxOverPercentage * 100).minus(100)) + "%"

        val alerts = listOf(
            "Budget Exceeded!" to "You've exceeded your ${worstBudget.name} limit by $percentStr. Time to cut back, $userName!",
            "Uh oh, $userName!" to "You went over your ${worstBudget.name} budget by $percentStr. Pump the brakes!",
            "Red Alert!" to "Your ${worstBudget.name} spending is $percentStr over limit. Let's get back on track.",
            "Spending Spike!" to "You're $percentStr over on ${worstBudget.name}. Review your expenses, $userName."
        )
        val (title, msg) = alerts.random()
        return DashboardNotification.Alert(title, msg)
    }

    // 2. Check for WARNING (Near Limit > 85%)
    val nearLimitBudget = budgets.firstOrNull { budget ->
        val spent = spendingByBudget[budget.id] ?: 0.0
        val percentage = if (budget.limitAmount > 0) spent / budget.limitAmount else 0.0
        percentage in 0.85..1.0
    }

    if (nearLimitBudget != null) {
        val warnings = listOf(
            "Watch Out!" to "You're close to the limit on ${nearLimitBudget.name}, $userName.",
            "Heads Up" to "You've used over 85% of your ${nearLimitBudget.name} budget. Spend wisely!",
            "Careful Now" to "Your ${nearLimitBudget.name} bucket is almost full. Keep an eye on it.",
            "Approaching Limit" to "You are nearing the cap for ${nearLimitBudget.name}. Just a friendly reminder, $userName!"
        )
        val (title, msg) = warnings.random()
        return DashboardNotification.Warning(title, msg)
    }

    // 3. Check for SAVINGS ACHIEVEMENT (Good Job)
    val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    if (totalIncome > 0 && totalExpense < totalIncome && dayOfMonth > 5) {
        val savedAmount = totalIncome - totalExpense
        val savedStr = formatCurrency(savedAmount)

        val achievements = listOf(
            "Hooray!" to "You've saved $savedStr so far this month. Keep it up, $userName!",
            "Great Job, $userName!" to "Your savings are looking good at $savedStr. Financial freedom, here we come!",
            "On Fire!" to "You have $savedStr in savings this month. You're doing great!",
            "Smart Moves" to "By spending less, you've saved $savedStr. Proud of you, $userName.",
            "Money Magnet" to "You attracted $savedStr in savings this month. Keep the momentum going!",
            "Wise Choice" to "Every penny saved is a penny earned. You have $savedStr saved up."
        )
        val (title, msg) = achievements.random()
        return DashboardNotification.Achievement(title, msg)
    }

    // 4. Default / Motivational (Fallback if nothing else matches)
    val quotes = listOf(
        "Motivation" to "Do not save what is left after spending, but spend what is left after saving. - Warren Buffett",
        "Tip of the Day" to "Small daily improvements are the key to staggering long-term results.",
        "Focus, $userName" to "A budget is telling your money where to go instead of wondering where it went.",
        "Stay Strong" to "Financial freedom is available to those who learn about it and work for it."
    )
    val (title, msg) = quotes.random()
    return DashboardNotification.Achievement("Daily Wisdom", msg)
}

private fun Transactions.toTransactionItemData(category: Categories?): TransactionItemData {
    val realCategory = category ?: Categories(0, "Other", null, null)
    val isExpense = (this.type == TransactionType.EXPENSE)

    val icon = when (realCategory.name) {
        "Food and Beverages" -> R.drawable.food
        "Transport" -> R.drawable.directions_car_48px
        "Shopping" -> R.drawable.shopping_bag_48px
        "Bills" -> R.drawable.receipt_long_48px
        "Entertainment" -> R.drawable.playing_cards_48px
        "Health" -> R.drawable.heart_check_48px
        "Education" -> R.drawable.cognition_2_48px
        "Salary" -> R.drawable.salary
        "Bonus" -> R.drawable.paid_48px
        "Investment" -> R.drawable.trending_up_40dp
        else -> R.drawable.paid_48px
    }

    return TransactionItemData(
        id = this.id,
        iconResId = icon,
        title = this.note ?: "Transaction",
        subtitle = this.date.toFormattedString(),
        category = realCategory.name.substringBefore(" "),
        amount = this.totalAmount,
        isExpense = isExpense
    )
}

private fun Date.toFormattedString(): String {
    val formatter = SimpleDateFormat("HH:mm - MMM dd", Locale.getDefault())
    return formatter.format(this)
}