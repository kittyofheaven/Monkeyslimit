package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class TransactionDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun transactionDetail_rendersWithoutCrashing() {
        composeTestRule.setContent {
            TransactionDetailScreen(
                transactionId = 1,
                onNavigateBack = {}
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }
}
