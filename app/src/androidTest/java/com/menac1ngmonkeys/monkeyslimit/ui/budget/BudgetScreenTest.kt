package com.menac1ngmonkeys.monkeyslimit.ui.budget

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.SortDirection
import com.menac1ngmonkeys.monkeyslimit.ui.state.SortType
import org.junit.Rule
import org.junit.Test

class BudgetScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun budgetScreen_displaysYearSelector() {
        val uiState = BudgetUiState(selectedYear = 2026)

        composeTestRule.setContent {
            BudgetListScreenContent(
                uiState = uiState,
                onMonthSelected = {},
                onYearChanged = {},
                onSortChange = { _, _ -> },
                onBudgetClick = {},
                onAiRecommendationClick = {}
            )
        }

        // Verify year is displayed
        composeTestRule.onNodeWithText("2026").assertIsDisplayed()
    }

    @Test
    fun budgetScreen_clicksAiRecommendation_triggersNavigation() {
        val uiState = BudgetUiState()
        var aiRecommendationClicked = false

        composeTestRule.setContent {
            BudgetListScreenContent(
                uiState = uiState,
                onMonthSelected = {},
                onYearChanged = {},
                onSortChange = { _, _ -> },
                onBudgetClick = {},
                onAiRecommendationClick = { aiRecommendationClicked = true }
            )
        }

        // Click AI Recommendation button
        composeTestRule.onNode(hasText("Budget Recommendation") and hasClickAction()).performClick()

        // Verify callback was triggered
        assert(aiRecommendationClicked)
    }

    @Test
    fun budgetScreen_clicksAddBudget_triggersNavigation() {
        val uiState = BudgetUiState()
        var addBudgetClicked = false

        composeTestRule.setContent {
            BudgetListScreenContent(
                uiState = uiState,
                onMonthSelected = {},
                onYearChanged = {},
                onSortChange = { _, _ -> },
                onBudgetClick = {},
                onAddBudgetClick = { addBudgetClicked = true },
                onAiRecommendationClick = {}
            )
        }

        // Click Add A New Budget button
        composeTestRule.onNode(hasText("Add A New Budget") and hasClickAction()).performClick()

        // Verify callback was triggered
        assert(addBudgetClicked)
    }

    @Test
    fun budgetScreen_displaysBudgetsHeader() {
        val uiState = BudgetUiState()

        composeTestRule.setContent {
            BudgetListScreenContent(
                uiState = uiState,
                onMonthSelected = {},
                onYearChanged = {},
                onSortChange = { _, _ -> },
                onBudgetClick = {},
                onAiRecommendationClick = {}
            )
        }

        // Verify "Budgets" header is displayed
        composeTestRule.onNodeWithText("Budgets").assertIsDisplayed()
    }

    @Test
    fun budgetScreen_displaysTotalSummary() {
        val uiState = BudgetUiState(
            totalLeft = 1000000.0,
            totalSpent = 500000.0,
            totalLimit = 1500000.0
        )

        composeTestRule.setContent {
            BudgetListScreenContent(
                uiState = uiState,
                onMonthSelected = {},
                onYearChanged = {},
                onSortChange = { _, _ -> },
                onBudgetClick = {},
                onAiRecommendationClick = {}
            )
        }

        // Verify summary elements are displayed
        composeTestRule.onNodeWithText("left").assertIsDisplayed()
    }
}
