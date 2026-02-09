package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.AnalyticsUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.CategoryExpense
import com.menac1ngmonkeys.monkeyslimit.ui.state.Timeframe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

private data class ChartPoint(
    val label: String,
    val income: Double,
    val expense: Double,
    val date: LocalDateTime
)

class AnalyticsViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    private val _selectedTimeframe = MutableStateFlow(Timeframe.MONTHLY)
    private val _currentDate = MutableStateFlow(LocalDate.now())

    val analyticsUiState: StateFlow<AnalyticsUiState> =
        combine(
            _selectedTimeframe,
            _currentDate,
            transactionsRepository.getAllTransactions(),
            categoriesRepository.getAllCategories()
        ) { timeframe, currentDate, transactions, categories ->

            val categoryMap = categories.associateBy { it.id }

            val (startDate, endDate) = calculateDateRange(timeframe, currentDate)
            val filteredTransactions = filterTransactionsByRange(transactions, startDate, endDate)
            val chartPoints = aggregateForTimeframe(filteredTransactions, timeframe, startDate, endDate)

            val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
            val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }

            // --- NEW: Separate Lists ---
            val incomeTransactions = filteredTransactions
                .filter { it.type == TransactionType.INCOME }
                .sortedByDescending { it.date }

            val expenseTransactions = filteredTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sortedByDescending { it.date }

            // "Use data before [current period] to accumulate"
            val accumulatedSavings = transactions
                .filter {
                    // Filter all transactions that happened BEFORE or ON the rangeEndDate
                    val txDate = it.date.toLocalDate()
                    !txDate.isAfter(endDate)
                }
                .sumOf {
                    if (it.type == TransactionType.INCOME) it.totalAmount else -it.totalAmount
                }

            val topExpenses = expenseTransactions.sortedByDescending { it.totalAmount }.take(5)

            val categoryExpenses = calculateCategoryExpenses(expenseTransactions, totalExpense, categoryMap)

            AnalyticsUiState(
                selectedTimeframe = timeframe,
                currentDate = currentDate,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                incomeValues = chartPoints.map { it.income },
                expenseValues = chartPoints.map { it.expense },
                dateLabels = chartPoints.map { it.label },
                rangeStart = startDate,
                rangeEnd = endDate,
                topExpenses = topExpenses,
                incomeTransactions = incomeTransactions,
                expenseTransactions = expenseTransactions,
                totalAccumulatedSavings = accumulatedSavings,
                categoryMap = categoryMap,
                categoryExpenses = categoryExpenses
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AnalyticsUiState()
        )

    private fun calculateCategoryExpenses(
        transactions: List<Transactions>,
        totalExpense: Double,
        categoryMap: Map<Int, Categories>
    ): List<CategoryExpense> {
        if (totalExpense == 0.0) return emptyList()

        val chartColors = listOf(
            Color(0xFF81C784), Color(0xFFFFF176), Color(0xFFFF8A65),
            Color(0xFFBA68C8), Color(0xFF64B5F6), Color(0xFFE57373)
        )

        return transactions
            .groupBy { it.categoryId }
            .entries.toList()
            .mapIndexed { index, entry ->
                val catId = entry.key
                val txs = entry.value
                val sum = txs.sumOf { it.totalAmount }
                val percentage = ((sum / totalExpense) * 100).toFloat()

                val name = categoryMap[catId]?.name ?: "Unknown"

                CategoryExpense(
                    categoryName = name,
                    amount = sum,
                    percentage = percentage,
                    color = chartColors[index % chartColors.size]
                )
            }
            .sortedByDescending { it.percentage }
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _selectedTimeframe.update { timeframe }
        _currentDate.update { LocalDate.now() }
    }

    fun incrementDate() { moveDate(1) }
    fun decrementDate() { moveDate(-1) }

    fun setCustomRange(start: LocalDate?, end: LocalDate?) {
        if (start != null) {
            _currentDate.update { start }
        }
    }

    private fun moveDate(amount: Long) {
        val timeframe = _selectedTimeframe.value
        _currentDate.update { date ->
            when (timeframe) {
                Timeframe.DAILY -> date.plusDays(amount)
                Timeframe.WEEKLY -> date.plusWeeks(amount)
                Timeframe.MONTHLY -> date.plusMonths(amount)
                Timeframe.YEARLY -> date.plusYears(amount)
            }
        }
    }

    private fun calculateDateRange(timeframe: Timeframe, date: LocalDate): Pair<LocalDate, LocalDate> {
        return when (timeframe) {
            Timeframe.DAILY -> date to date
            Timeframe.WEEKLY -> {
                val start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val end = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                start to end
            }
            Timeframe.MONTHLY -> {
                val start = date.withDayOfMonth(1)
                val end = date.with(TemporalAdjusters.lastDayOfMonth())
                start to end
            }
            Timeframe.YEARLY -> {
                val start = date.withDayOfYear(1)
                val end = date.with(TemporalAdjusters.lastDayOfYear())
                start to end
            }
        }
    }

    private fun filterTransactionsByRange(
        transactions: List<Transactions>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Transactions> {
        return transactions.filter { tx ->
            val d = tx.date.toLocalDate()
            !d.isBefore(startDate) && !d.isAfter(endDate)
        }
    }

    private fun aggregateForTimeframe(
        transactions: List<Transactions>,
        timeframe: Timeframe,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ChartPoint> {
        if (transactions.isEmpty()) return emptyList()

        val lastTxDate = transactions.maxOf { it.date.toLocalDateTime() }
        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(LocalTime.MAX)
        val effectiveEndDateTime = if (lastTxDate.isBefore(endDateTime)) lastTxDate else endDateTime

        return when (timeframe) {
            Timeframe.DAILY -> aggregateBuckets(transactions, startDateTime, effectiveEndDateTime, ChronoUnit.HOURS, 6)
            Timeframe.MONTHLY -> aggregateBuckets(transactions, startDateTime, effectiveEndDateTime, ChronoUnit.DAYS, 6)
            Timeframe.WEEKLY -> aggregateBuckets(transactions, startDateTime, effectiveEndDateTime, ChronoUnit.DAYS, 7)
            Timeframe.YEARLY -> aggregateBuckets(transactions, startDateTime, effectiveEndDateTime, ChronoUnit.MONTHS, 6)
        }
    }

    private fun aggregateBuckets(
        transactions: List<Transactions>,
        start: LocalDateTime,
        end: LocalDateTime,
        unit: ChronoUnit,
        targetBuckets: Int
    ): List<ChartPoint> {
        val points = mutableListOf<ChartPoint>()
        val locale = Locale.getDefault()
        val totalUnits = unit.between(start, end)
        val step = ceil((totalUnits + 1).toDouble() / targetBuckets.toDouble()).toLong().coerceAtLeast(1)

        var currentStart = start
        while (!currentStart.isAfter(end)) {
            var currentEnd = currentStart.plus(step - 1, unit)
            if (currentEnd.isAfter(end)) currentEnd = end
            val queryEnd = if (unit == ChronoUnit.HOURS) currentEnd.plusMinutes(59) else currentEnd.toLocalDate().atTime(LocalTime.MAX)

            val bucketTxs = transactions.filter {
                val txTime = it.date.toLocalDateTime()
                !txTime.isBefore(currentStart) && !txTime.isAfter(queryEnd)
            }

            val income = bucketTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
            val expense = bucketTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }
            val label = formatBucketLabel(currentStart, currentEnd, unit, locale)

            points.add(ChartPoint(label, income, expense, currentStart))
            currentStart = currentStart.plus(step, unit)
            if (step <= 0) break
        }
        return points
    }

    private fun formatBucketLabel(start: LocalDateTime, end: LocalDateTime, unit: ChronoUnit, locale: Locale): String {
        return when (unit) {
            ChronoUnit.HOURS -> {
                val fmt = DateTimeFormatter.ofPattern("HH:00", locale)
                if (start.hour == end.hour) start.format(fmt) else "${start.format(fmt)}-${end.format(fmt)}"
            }
            ChronoUnit.DAYS -> {
                val fmt = DateTimeFormatter.ofPattern("d MMM", locale)
                if (start.toLocalDate() == end.toLocalDate()) start.format(fmt) else "${start.format(DateTimeFormatter.ofPattern("d", locale))}-${end.format(fmt)}"
            }
            ChronoUnit.MONTHS -> {
                val fmt = DateTimeFormatter.ofPattern("MMM", locale)
                if (start.month == end.month && start.year == end.year) start.format(fmt) else "${start.format(fmt)}-${end.format(fmt)}"
            }
            else -> start.toLocalDate().toString()
        }
    }

    private fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    private fun Date.toLocalDateTime(): LocalDateTime = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}