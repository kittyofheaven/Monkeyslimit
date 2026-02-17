package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ReviewSmartSplitScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reviewSmartSplit_rendersWithoutCrashing() {
        composeTestRule.setContent {
            ReviewSmartSplitScreen(
                imageUri = null,
                navController = androidx.navigation.compose.rememberNavController(),
                onNavigateBack = {}
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }
}
