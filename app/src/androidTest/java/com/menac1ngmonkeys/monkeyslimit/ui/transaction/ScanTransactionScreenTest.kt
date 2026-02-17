package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ScanTransactionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun scanTransaction_rendersWithoutCrashing() {
        composeTestRule.setContent {
            ScanTransactionScreen(
                onNavigateToManual = {},
                onImagePicked = { _, _ -> }
            )
        }

        // Verify screen renders (camera permission handling required)
        composeTestRule.waitForIdle()
    }
}
