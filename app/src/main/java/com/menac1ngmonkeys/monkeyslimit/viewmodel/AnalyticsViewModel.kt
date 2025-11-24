package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.AnalyticsUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.Timeframe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.Locale

/**
 * Represents a single aggregated data point for the analytics line chart.
 *
 * Each [ChartPoint] corresponds to one position on the chart’s x-axis and contains:
 *
 * - **label** — The formatted x-axis label representing a time bucket.
 *   This label changes depending on the selected [Timeframe]:
 *     - DAILY: `"18 Nov"`
 *     - WEEKLY: `"1–7 Jan"`
 *     - MONTHLY: `"Jan 2025"`
 *     - YEARLY: `"2025"`
 *
 * - **income** — The total income value for this bucket.
 *   (Currently uses placeholder values until real income transactions are implemented.)
 *
 * - **expense** — The total expense value for this bucket.
 *
 * This class is used internally by the ViewModel to transform raw transactions into
 * chart-ready series values (income/expense) and matching x-axis labels.
 *
 * Aggregation and label formatting are handled in [aggregateForTimeframe].
 *
 * @property label The display-ready x-axis label for this aggregated period.
 * @property income The total income amount for the given time bucket.
 * @property expense The total expense amount for the given time bucket.
 */

private data class ChartPoint(
    val label: String,   // x-axis label (day / week range / month / year)
    val income: Double,
    val expense: Double,
)

class AnalyticsViewModel(
    private val transactionsRepository: TransactionsRepository,
) : ViewModel() {
    // Internal mutable state for the selected timeframe.
    private val _selectedTimeframe = MutableStateFlow(Timeframe.MONTHLY)

    private val _customRange =
        MutableStateFlow<Pair<LocalDate?, LocalDate?>>(null to null)

    fun setCustomRange(start: LocalDate?, end: LocalDate?) {
        _customRange.value = start to end
    }

    // Public state for the entire screen. It combines data from the repository
    // with the selected timeframe to produce the final UI state.
    val analyticsUiState: StateFlow<AnalyticsUiState> =
        combine(
            _selectedTimeframe,
            _customRange,
            transactionsRepository.getAllTransactions(),
        ) { timeframe, (startDate, endDate), transactions ->
//            val chartPoints = aggregateForTimeframe(transactions, timeframe)
            val filtered = filterTransactionsByRange(transactions, startDate, endDate)
            val chartPoints = aggregateForTimeframe(filtered, timeframe)

            val totalIncome = chartPoints.sumOf { it.income }
            val totalExpense = chartPoints.sumOf { it.expense }

            AnalyticsUiState(
                selectedTimeframe = timeframe,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                incomeValues = chartPoints.map { it.income },
                expenseValues = chartPoints.map { it.expense },
                dateLabels = chartPoints.map { it.label },
                rangeStart = startDate,
                rangeEnd = endDate,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AnalyticsUiState()
        )

    /**
     * Updates the selected timeframe for the analytics screen.
     */
    fun selectTimeframe(timeframe: Timeframe) {
        _selectedTimeframe.update { timeframe }
    }

    // ----------------- Helpers -----------------

    /**
     * Aggregates raw transaction data into chart-ready buckets based on a selected [Timeframe].
     *
     * This function performs **time-based grouping** and **value accumulation** to produce
     * a list of [ChartPoint] objects, which contain:
     *  - A formatted x-axis label (`label`)
     *  - The total income for that bucket (`income`)
     *  - The total expense for that bucket (`expense`)
     *
     * ## Timeframe Behavior
     *
     * ### **DAILY**
     * - Groups transactions by exact calendar day (`LocalDate`).
     * - Each result represents one day.
     * - Label format: `"18 Nov"`, `"21 Nov"`, etc.
     *
     * ### **WEEKLY**
     * - Groups by week ranges.
     * - A “week” begins on **Monday** (ISO-8601 standard).
     * - For each week, the bucket key = Monday of that week.
     * - Label format: `"1–7 Jan"`, `"8–14 Jan"`, etc.
     *
     * ### **MONTHLY**
     * - Groups by `YearMonth` (e.g., January 2025).
     * - Each bucket contains all transactions within that month.
     * - Label format: `"Jan 2025"`, `"Feb 2025"`.
     *
     * ### **YEARLY**
     * - Groups all transactions by year (e.g., 2024, 2025).
     * - Label format: `"2025"`.
     *
     * ## Returned Data
     * The resulting list is **sorted chronologically** (ascending) to ensure the chart
     * receives properly ordered x-axis points, even if the database stores items unordered.
     *
     * ## Notes
     * - If the transaction list is empty, the function returns an empty list.
     * - Income values currently use a placeholder (`expense * 1.3`) until real income
     *   transactions are available in the database.
     *
     * @param transactions A list of raw transaction entities to aggregate.
     * @param timeframe The selected [Timeframe] that determines the aggregation granularity.
     * @return A chronologically sorted list of [ChartPoint] entries ready to be mapped
     *         into the chart’s series values and x-axis labels.
     */

    private fun aggregateForTimeframe(
        transactions: List<Transactions>,
        timeframe: Timeframe,
    ): List<ChartPoint> {
        if (transactions.isEmpty()) return emptyList()

        val locale = Locale.getDefault()
        return when (timeframe) {
            Timeframe.DAILY -> {
                // one point per calendar day
                val formatter = DateTimeFormatter.ofPattern("dd MMM", locale)

                transactions
                    .groupBy { it.date.toLocalDate() }
                    .toSortedMap()
                    .map { (date, txs) ->
                        val expense = txs.sumOf { it.totalAmount }
                        val income = expense * 1.3   // TODO: replace with real income logic
                        ChartPoint(
                            label = date.format(formatter),
                            income = income,
                            expense = expense,
                        )
                    }
            }

            Timeframe.WEEKLY -> {
                // one point per week, keyed by Monday of that week
                transactions
                    .groupBy {
                        val d = it.date.toLocalDate()
                        d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    }
                    .toSortedMap()
                    .map { (startOfWeek, txs) ->
                        val expense = txs.sumOf { it.totalAmount }
                        val income = expense * 1.3

                        val endOfWeek = startOfWeek.plusDays(6)
                        val monthName = startOfWeek.month.getDisplayName(TextStyle.SHORT, locale)
                        val label = "${startOfWeek.dayOfMonth}–${endOfWeek.dayOfMonth} $monthName"
                        // Example: "1–7 Jan"

                        ChartPoint(
                            label = label,
                            income = income,
                            expense = expense,
                        )
                    }
            }

            Timeframe.MONTHLY -> {
                // one point per YearMonth
                transactions
                    .groupBy {
                        val d = it.date.toLocalDate()
                        YearMonth.of(d.year, d.month)
                    }
                    .toSortedMap()
                    .map { (yearMonth, txs) ->
                        val expense = txs.sumOf { it.totalAmount }
                        val income = expense * 1.3

                        val monthName = yearMonth.month.getDisplayName(TextStyle.SHORT, locale)
                        val label = "$monthName ${yearMonth.year}"   // "Jan 2025"

                        ChartPoint(
                            label = label,
                            income = income,
                            expense = expense,
                        )
                    }
            }

            Timeframe.YEARLY -> {
                // one point per year
                transactions
                    .groupBy { it.date.toLocalDate().year }
                    .toSortedMap()
                    .map { (year, txs) ->
                        val expense = txs.sumOf { it.totalAmount }
                        val income = expense * 1.3

                        ChartPoint(
                            label = year.toString(),  // "2025"
                            income = income,
                            expense = expense,
                        )
                    }
            }
        }
    }

    /**
     * Filters a list of transactions so that only those whose dates fall within the
     * specified date range are returned.
     *
     * This function supports partial ranges:
     * - If both [startDate] and [endDate] are `null`, all transactions are returned unchanged.
     * - If only [startDate] is provided, all transactions on or after that date are included.
     * - If only [endDate] is provided, all transactions on or before that date are included.
     * - If both are provided, transactions must fall within the inclusive range
     *   `[startDate, endDate]`.
     *
     * Internally, each transaction's `Date` is converted to `LocalDate` using the device
     * timezone before comparisons are performed.
     *
     * @param transactions the complete list of [Transactions] to filter.
     * @param startDate the earliest allowed transaction date, or `null` to disable lower bound filtering.
     * @param endDate the latest allowed transaction date, or `null` to disable upper bound filtering.
     *
     * @return a list of transactions whose dates fall within the specified range.
     */
    private fun filterTransactionsByRange(
        transactions: List<Transactions>,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): List<Transactions> {
        if (startDate == null && endDate == null) return transactions

        return transactions.filter { tx ->
            val d = tx.date.toLocalDate()
            val afterStart = startDate?.let { !d.isBefore(it) } ?: true
            val beforeEnd = endDate?.let { !d.isAfter(it) } ?: true
            afterStart && beforeEnd
        }
    }


    /**
     * Convert java.util.Date -> LocalDate.
     */
    private fun Date.toLocalDate(): LocalDate =
        this.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
}

