package com.menac1ngmonkeys.monkeyslimit.ui.budget

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class BudgetRecommendationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun budgetRecommendationScreen_rendersWithoutCrashing() {
        composeTestRule.setContent {
            BudgetRecommendationScreen(
                onNavigateBack = {}
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }

    @Test
    fun budgetRecommendationScreen_displaysContent() {
        composeTestRule.setContent {
            BudgetRecommendationScreen(
                onNavigateBack = {}
            )
        }

        // Basic rendering test
        composeTestRule.waitForIdle()
    }
}
