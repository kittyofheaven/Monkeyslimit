package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

import AppViewModelProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.ui.components.BalanceExpenseCard
import com.menac1ngmonkeys.monkeyslimit.ui.components.MainContentContainer
import com.menac1ngmonkeys.monkeyslimit.ui.components.SavingsGoalCard
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardFilter
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardUiState
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.DashboardViewModel

// This is our new "dumb" composable. It just displays UI.
/**
 * Stateless dashboard UI; renders cards and lists from provided UI state.
 *
 * @param appUiState shared app-wide totals.
 * @param dashboardUiState dashboard-specific feed content.
 */
@Composable
fun DashboardScreenContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState, // UI for all of the screens
    dashboardUiState: DashboardUiState, // It accepts the UI state directly
    onFilterSelected: (DashboardFilter) -> Unit = {},
    onTransactionClick: (Int) -> Unit = {}
) {
    val totalBalance = appUiState.totalBalance
    val totalExpense = appUiState.totalExpense
    val totalIncome = appUiState.totalIncome

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
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Savings Box -- START
                SavingsGoalCard(
                    currentSavings = appUiState.totalBalance,
                    savingsGoal = 50_000_000.0, // For now, we add it explicitly
                    revenueLastWeek = appUiState.totalExpense,
                    foodExpenseLastWeek = appUiState.totalExpense
                )
                // Savings Box -- END

                Spacer(Modifier.size(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    DashboardFilterRow(
                        currentFilter = dashboardUiState.currentFilter,
                        onFilterSelected = onFilterSelected
                    )
                }
                Spacer(Modifier.size(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Tell the LazyColumn to use the list from our ViewModel's state
                    items(dashboardUiState.recentTransactions) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            onClick = onTransactionClick,
                        )
                    }
                    item { Spacer(Modifier.size(20.dp)) }
                }

            }
        }
        // Main Container -- END --
    }
}

@Composable
fun DashboardFilterRow(
    currentFilter: DashboardFilter,
    onFilterSelected: (DashboardFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FilterChipItem(
            text = "All",
            selected = currentFilter == DashboardFilter.ALL,
            onClick = { onFilterSelected(DashboardFilter.ALL) }
        )
        FilterChipItem(
            text = "Income",
            selected = currentFilter == DashboardFilter.INCOME,
            onClick = { onFilterSelected(DashboardFilter.INCOME) }
        )
        FilterChipItem(
            text = "Expense",
            selected = currentFilter == DashboardFilter.EXPENSE,
            onClick = { onFilterSelected(DashboardFilter.EXPENSE) }
        )
    }
}

@Composable
fun FilterChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// This is the "smart" composable that your app's navigation will use.
/**
 * ViewModel-backed dashboard entry; collects state and delegates to stateless content.
 *
 * @param dashboardViewModel injected ViewModel supplying dashboard state.
 * @param appViewModel injected ViewModel supplying shared totals.
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    // Use the factory to create an instance of our ViewModel
    dashboardViewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
    appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: androidx.navigation.NavController,
) {
    // Collect the state from the ViewModel. Every time the state changes,
    // this Composable will automatically recompose with the new data.
    val dashboardUiState by dashboardViewModel.dashboardUiState.collectAsState()
    val appUiState by appViewModel.appUiState.collectAsState()


    // Call our "dumb" UI composable and pass the real state to it
    DashboardScreenContent(
        modifier = modifier,
        appUiState = appUiState,
        dashboardUiState = dashboardUiState,
        onFilterSelected = dashboardViewModel::updateFilter,
        onTransactionClick = { transactionId ->
            // Navigate to detail
            navController.navigate("transaction_detail/$transactionId")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    // 1. Create a fake state object with sample data.
    val fakeDashboardUiState = DashboardUiState()

    val fakeAppUiState = AppUiState(
        totalBalance = 150.0,
        totalExpense = 275.0
    )

    // 2. Call the stateless "Content" composable with the fake data.
    //    No ViewModel is created, so there will be no crash.
    DashboardScreenContent(
        dashboardUiState = fakeDashboardUiState,
        appUiState = fakeAppUiState
    )
}

