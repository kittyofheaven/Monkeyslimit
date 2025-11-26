package com.menac1ngmonkeys.monkeyslimit.ui.state

import java.time.LocalDate

enum class Timeframe {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class IncomeExpensePoint(
    val date: LocalDate,
    val income: Double,
    val expense: Double,
)

/**
 * Analytics screen data model.
 *
 * @property selectedTimeframe current aggregation granularity.
 * @property totalIncome aggregated income total (placeholder until real income).
 * @property totalExpense aggregated expense total.
 * @property incomeValues series values for charting.
 * @property expenseValues series values for charting.
 * @property dateLabels x-axis labels matching the series.
 * @property rangeStart optional filter lower bound.
 * @property rangeEnd optional filter upper bound.
 */
data class AnalyticsUiState(
    // The state for the top button selector
    val selectedTimeframe: Timeframe = Timeframe.MONTHLY,

    // The state for the bottom summary cards
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,

    // Chart data
    val incomeValues: List<Double> = emptyList(),
    val expenseValues: List<Double> = emptyList(),
    val dateLabels: List<String> = emptyList(),

    // Filtered Date Range (only if exist)
    val rangeStart: LocalDate? = null,
    val rangeEnd: LocalDate? = null,
)