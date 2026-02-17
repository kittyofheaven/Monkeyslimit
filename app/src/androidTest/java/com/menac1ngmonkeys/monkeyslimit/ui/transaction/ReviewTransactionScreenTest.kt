package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ReviewTransactionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reviewTransaction_rendersWithoutCrashing() {
        composeTestRule.setContent {
            ReviewTransactionScreen(
                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = AppViewModelProvider.Factory
                ),
                onNavigateBack = {}
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }
}
