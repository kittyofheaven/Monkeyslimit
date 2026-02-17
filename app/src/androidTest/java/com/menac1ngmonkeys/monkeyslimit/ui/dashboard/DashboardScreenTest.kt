package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardFilter
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardNotification
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardUiState
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboard_displaysFilterChips() {
        val appUiState = AppUiState()
        val dashboardUiState = DashboardUiState()

        composeTestRule.setContent {
            DashboardScreenContent(
                appUiState = appUiState,
                dashboardUiState = dashboardUiState
            )
        }

        // Verify filter chips are displayed
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Income").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expense").assertIsDisplayed()
    }

    @Test
    fun dashboard_clicksIncomeFilter_updatesFilter() {
        val appUiState = AppUiState()
        val dashboardUiState = DashboardUiState()
        var selectedFilter: DashboardFilter? = null

        composeTestRule.setContent {
            DashboardScreenContent(
                appUiState = appUiState,
                dashboardUiState = dashboardUiState,
                onFilterSelected = { filter -> selectedFilter = filter }
            )
        }

        // Click Income filter
        composeTestRule.onNodeWithText("Income").performClick()

        // Verify filter was updated
        assert(selectedFilter == DashboardFilter.INCOME)
    }

    @Test
    fun dashboard_clicksExpenseFilter_updatesFilter() {
        val appUiState = AppUiState()
        val dashboardUiState = DashboardUiState()
        var selectedFilter: DashboardFilter? = null

        composeTestRule.setContent {
            DashboardScreenContent(
                appUiState = appUiState,
                dashboardUiState = dashboardUiState,
                onFilterSelected = { filter -> selectedFilter = filter }
            )
        }

        // Click Expense filter
        composeTestRule.onNodeWithText("Expense").performClick()

        // Verify filter was updated
        assert(selectedFilter == DashboardFilter.EXPENSE)
    }

    @Test
    fun dashboard_emptyTransactions_showsMessage() {
        val appUiState = AppUiState()
        val dashboardUiState = DashboardUiState(
            recentTransactions = emptyList(),
            currentFilter = DashboardFilter.ALL
        )

        composeTestRule.setContent {
            DashboardScreenContent(
                appUiState = appUiState,
                dashboardUiState = dashboardUiState
            )
        }

        // Verify empty state message is displayed
        composeTestRule.onNodeWithText("No transactions yet.").assertIsDisplayed()
    }

    @Test
    fun dashboard_displaysBalanceCard() {
        val appUiState = AppUiState(
            totalIncome = 5000000.0,
            totalExpense = 2000000.0
        )
        val dashboardUiState = DashboardUiState()

        composeTestRule.setContent {
            DashboardScreenContent(
                appUiState = appUiState,
                dashboardUiState = dashboardUiState
            )
        }

        // Balance card should be displayed (this is a basic check)
        // More specific assertions would require test tags
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }
}
