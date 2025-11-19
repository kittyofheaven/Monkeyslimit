package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

import AppViewModelProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.ui.components.BalanceExpenseCard
import com.menac1ngmonkeys.monkeyslimit.ui.components.MainContentContainer
import com.menac1ngmonkeys.monkeyslimit.ui.components.SavingsGoalCard

// This is our new "dumb" composable. It just displays UI.
@Composable
fun DashboardScreenContent(
    modifier: Modifier = Modifier,
    dashboardUiState: DashboardUiState, // It accepts the UI state directly
) {
    val totalBalance = dashboardUiState.totalBalance
    val totalExpense = dashboardUiState.totalExpense

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
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Savings Box -- START
                SavingsGoalCard(
                    currentSavings = dashboardUiState.totalBalance,
                    savingsGoal = 50_000_000.0, // For now, we add it explicitly
                    revenueLastWeek = dashboardUiState.totalExpense,
                    foodExpenseLastWeek = dashboardUiState.totalExpense
                )
                // Savings Box -- END

                Spacer(Modifier.size(15.dp))
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.size(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Tell the LazyColumn to use the list from our ViewModel's state
                    items(dashboardUiState.recentTransactions) { transaction ->
                        TransactionRow(transaction = transaction)
                    }
                }
            }
        }
        // Main Container -- END --
    }
}

// This is the "smart" composable that your app's navigation will use.
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    // Use the factory to create an instance of our ViewModel
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Collect the state from the ViewModel. Every time the state changes,
    // this Composable will automatically recompose with the new data.
    val dashboardUiState by viewModel.dashboardUiState.collectAsState()

    // Call our "dumb" UI composable and pass the real state to it
    DashboardScreenContent(
        modifier = modifier,
        dashboardUiState = dashboardUiState
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    // 1. Create a fake state object with sample data.
    val fakeUiState = DashboardUiState(
        totalBalance = 15000000.0,
        totalExpense = 2750000.0
    )

    // 2. Call the stateless "Content" composable with the fake data.
    //    No ViewModel is created, so there will be no crash.
    DashboardScreenContent(dashboardUiState = fakeUiState)
}

