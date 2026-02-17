package com.menac1ngmonkeys.monkeyslimit.ui.budget

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetDetailViewModel
import org.junit.Rule
import org.junit.Test
import androidx.lifecycle.viewmodel.compose.viewModel
import AppViewModelProvider
import BudgetDetailViewModelFactory

class BudgetDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun budgetDetailScreen_rendersWithoutCrashing() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            // Using a dummy ID for testing
            val budgetId = 1
            val viewModel: BudgetDetailViewModel = viewModel(
                factory = BudgetDetailViewModelFactory(budgetId)
            )
            
            BudgetDetailWithHeader(
                viewModel = viewModel,
                onNavigateBack = { },
                navController = navController
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }
}
