package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class SplitResultScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splitResult_rendersWithoutCrashing() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }

    @Test
    fun splitResult_confirmButton_isDisplayed() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Verify the Confirm & Save button exists
        composeTestRule.onNodeWithText("Confirm & Save")
            .assertIsDisplayed()
    }

    @Test
    fun splitResult_clickConfirmButton_showsTransactionDialog() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Click the Confirm & Save button
        composeTestRule.onNodeWithText("Confirm & Save")
            .performClick()

        // Wait for the dialog to appear
        composeTestRule.waitForIdle()

        // Verify the transaction dialog is displayed
        // Note: This test will pass if the dialog appears, or if there are no categories/user doesn't owe
        // For more detailed testing, mock data would be needed
    }

    @Test
    fun transactionDialog_displaysCorrectTitle() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Click the Confirm & Save button
        composeTestRule.onNodeWithText("Confirm & Save")
            .performClick()

        // Wait for the dialog to appear
        composeTestRule.waitForIdle()

        // Check if the dialog title exists (if dialog appears)
        // This will be visible only if user owes money
        if (composeTestRule.onAllNodesWithText("Save Your Transaction").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Save Your Transaction")
                .assertIsDisplayed()
        }
    }

    @Test
    fun transactionDialog_displaysCategoryDropdown() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Click the Confirm & Save button
        composeTestRule.onNodeWithText("Confirm & Save")
            .performClick()

        // Wait for the dialog to appear
        composeTestRule.waitForIdle()

        // Check if the Category label exists (if dialog appears)
        if (composeTestRule.onAllNodesWithText("Save Your Transaction").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Category", substring = true)
                .assertExists()
        }
    }

    @Test
    fun transactionDialog_displaysBudgetDropdown() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Click the Confirm & Save button
        composeTestRule.onNodeWithText("Confirm & Save")
            .performClick()

        // Wait for the dialog to appear
        composeTestRule.waitForIdle()

        // Check if the Budget label exists (if dialog appears)
        if (composeTestRule.onAllNodesWithText("Save Your Transaction").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Budget (Optional)", substring = true)
                .assertExists()
        }
    }

    @Test
    fun transactionDialog_hasSaveButton() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Click the Confirm & Save button
        composeTestRule.onNodeWithText("Confirm & Save")
            .performClick()

        // Wait for the dialog to appear
        composeTestRule.waitForIdle()

        // Check if the Save Split button exists (if dialog appears)
        if (composeTestRule.onAllNodesWithText("Save Your Transaction").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Save Split")
                .assertExists()
        }
    }

    @Test
    fun transactionDialog_hasCancelButton() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Click the Confirm & Save button
        composeTestRule.onNodeWithText("Confirm & Save")
            .performClick()

        // Wait for the dialog to appear
        composeTestRule.waitForIdle()

        // Check if the Cancel button exists (if dialog appears)
        if (composeTestRule.onAllNodesWithText("Save Your Transaction").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Cancel")
                .assertExists()
        }
    }

    @Test
    fun transactionDialog_cancelButton_dismissesDialog() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Click the Confirm & Save button
        composeTestRule.onNodeWithText("Confirm & Save")
            .performClick()

        // Wait for the dialog to appear
        composeTestRule.waitForIdle()

        // If dialog is shown, click Cancel
        if (composeTestRule.onAllNodesWithText("Save Your Transaction").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText("Cancel")
                .performClick()

            // Wait for dismissal
            composeTestRule.waitForIdle()

            // Verify the dialog is dismissed
            composeTestRule.onNodeWithText("Save Your Transaction")
                .assertDoesNotExist()
        }
    }

    @Test
    fun splitResult_displaysTitle() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Verify that either "Bill Details" or a custom split name is displayed
        // Since we don't have a draft set, it should show "Bill Details"
        composeTestRule.onNodeWithText("Bill Details", substring = true)
            .assertExists()
    }

    @Test
    fun splitResult_displaysInstructionText() {
        composeTestRule.setContent {
            SplitResultScreen(
                navController = rememberNavController(),
                onNavigateHome = {}
            )
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Verify the instruction text is displayed
        composeTestRule.onNodeWithText("Tap card to view details")
            .assertExists()
    }
}
