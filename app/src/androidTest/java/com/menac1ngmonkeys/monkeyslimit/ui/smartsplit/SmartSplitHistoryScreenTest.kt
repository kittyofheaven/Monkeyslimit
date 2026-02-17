package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class SmartSplitHistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun smartSplitHistory_rendersWithoutCrashing() {
        composeTestRule.setContent {
            SmartSplitHistoryScreen(
                onItemClick = {}
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }

    @Test
    fun smartSplitHistory_displaysContent() {
        composeTestRule.setContent {
            SmartSplitHistoryScreen(
                onItemClick = {}
            )
        }

        // Basic rendering test
        composeTestRule.waitForIdle()
    }
}
