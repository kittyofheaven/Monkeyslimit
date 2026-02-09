package com.menac1ngmonkeys.monkeyslimit.ui.state

import androidx.compose.ui.graphics.Color
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import java.time.LocalDate

enum class Timeframe {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class CategoryExpense(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

data class AnalyticsUiState(
    val selectedTimeframe: Timeframe = Timeframe.MONTHLY,
    val currentDate: LocalDate = LocalDate.now(),

    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,

    val incomeValues: List<Double> = emptyList(),
    val expenseValues: List<Double> = emptyList(),
    val dateLabels: List<String> = emptyList(),

    val rangeStart: LocalDate? = null,
    val rangeEnd: LocalDate? = null,

    val topExpenses: List<Transactions> = emptyList(),

    val incomeTransactions: List<Transactions> = emptyList(),
    val expenseTransactions: List<Transactions> = emptyList(),

    val totalAccumulatedSavings: Double = 0.0,

    val categoryMap: Map<Int, Categories> = emptyMap(),

    val categoryExpenses: List<CategoryExpense> = emptyList()
)