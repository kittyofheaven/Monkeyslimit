package com.menac1ngmonkeys.monkeyslimit.ui.analytics

import AppViewModelProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.ui.components.BalanceExpenseCard
import com.menac1ngmonkeys.monkeyslimit.ui.components.DateRangeDialog
import com.menac1ngmonkeys.monkeyslimit.ui.components.IncomeExpenseLegend
import com.menac1ngmonkeys.monkeyslimit.ui.components.IncomeExpenseLineChart
import com.menac1ngmonkeys.monkeyslimit.ui.components.MainContentContainer
import com.menac1ngmonkeys.monkeyslimit.ui.state.AnalyticsUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.Timeframe
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AnalyticsViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Stateless analytics UI; shows chart, timeframe selector, and summaries.
 *
 * @param appUiState shared totals.
 * @param analyticsUiState analytics data/series to render.
 * @param onTimeframeSelected callback for timeframe changes.
 * @param onDateRangeApplied callback for applying custom date filters.
 */
@Composable
fun AnalyticsScreenContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState, // UI for all of the screens
    analyticsUiState: AnalyticsUiState,
    onTimeframeSelected: (Timeframe) -> Unit,
    onDateRangeApplied: (LocalDate?, LocalDate?) -> Unit,
) {
    val totalBalance = appUiState.totalBalance
    val totalExpense = appUiState.totalExpense

    // ✨ Date range picker state
    var showDateRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    // If filtered date is chosen, show a label
    val filterLabel = remember(
        analyticsUiState.rangeStart,
        analyticsUiState.rangeEnd
    ) {
        val start = analyticsUiState.rangeStart
        val end = analyticsUiState.rangeEnd

        if (start == null && end == null) {
            null // 👈 no filter, don't show anything
        } else {
            val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val startText = start?.format(fmt) ?: "Start"
            val endText = end?.format(fmt) ?: "End"
            "$startText – $endText"
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        BalanceExpenseCard(
            totalBalance = totalBalance,
            totalExpense = totalExpense,
        )
        Spacer(Modifier.size(15.dp))
        // Main Container -- START --
        MainContentContainer(
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp) // or Top, etc.
            ) {
                // 1. Timeframe Selector Buttons
                item {
                    TimeframeSelector(
                        selectedTimeframe = analyticsUiState.selectedTimeframe,
                        onTimeframeSelected = onTimeframeSelected
                    )
                }

                // 2. Main Analytics Box
                item {
                    AnalyticsBox(
                        onDateRangeClick = {
                            showDateRangePicker = true
                        },
                        onRefreshClick = {
                            if (filterLabel != null) {
                                onDateRangeApplied(null, null)
                            }
                        },
                        filterLabel = filterLabel,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val incomeColor = MaterialTheme.colorScheme.primary
                            val expenseColor = MaterialTheme.colorScheme.tertiary

                            IncomeExpenseLineChart(
                                incomeValues = analyticsUiState.incomeValues,
                                expenseValues = analyticsUiState.expenseValues,
                                dateLabels = analyticsUiState.dateLabels,
                                incomeColor = incomeColor,
                                expenseColor = expenseColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            )

                            Spacer(Modifier.height(8.dp))

                            // legend under chart
                            IncomeExpenseLegend(
                                incomeColor = incomeColor,
                                expenseColor = expenseColor,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            )
                        }
                    }
                }

                // 3. Income and Expense Summary
                item {
                    IncomeExpenseSummary(
                        income = analyticsUiState.totalIncome,
                        expense = analyticsUiState.totalExpense
                    )
                }
            }
        }
        // Main Container -- END --
    }

    DateRangeDialog(
        show = showDateRangePicker,
        onDismiss = { showDateRangePicker = false },
        onApply = { startDate, endDate ->
            onDateRangeApplied(startDate, endDate)
        },
        state = dateRangePickerState
    )

}

/**
 * ViewModel-backed analytics entry; wires callbacks to ViewModel actions.
 *
 * @param appViewModel provides shared totals.
 * @param analyticsViewModel provides analytics data and actions.
 */
@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory),
    analyticsViewModel: AnalyticsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val appUiState by appViewModel.appUiState.collectAsState()
    val analyticsUiState by analyticsViewModel.analyticsUiState.collectAsState()


    AnalyticsScreenContent(
        modifier = modifier,
        appUiState = appUiState,
        analyticsUiState = analyticsUiState,
        onTimeframeSelected = { timeframe ->
            analyticsViewModel.selectTimeframe(timeframe)
        },
        onDateRangeApplied = { startDate, endDate ->
            analyticsViewModel.setCustomRange(startDate, endDate)
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    AnalyticsScreen()
}