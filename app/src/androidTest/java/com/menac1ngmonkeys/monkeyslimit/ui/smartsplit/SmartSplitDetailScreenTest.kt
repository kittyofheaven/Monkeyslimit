package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class SmartSplitDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun smartSplitDetail_rendersWithoutCrashing() {
        composeTestRule.setContent {
            SmartSplitDetailScreen(
                splitId = 1,
                onNavigateBack = {}
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }
}
