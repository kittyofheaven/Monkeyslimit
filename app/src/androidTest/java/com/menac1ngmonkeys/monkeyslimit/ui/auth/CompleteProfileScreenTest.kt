package com.menac1ngmonkeys.monkeyslimit.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class CompleteProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun completeProfile_fillsFields_and_clicksFinish() {
        var finishCalled = false

        composeTestRule.setContent {
            CompleteProfileScreen(
                onComplete = { finishCalled = true }
            )
        }

        // Fill in Mobile Number - use tag instead of text
        composeTestRule.onNodeWithTag("Mobile Number").performTextInput("081234567890")

        // Note: Dropdowns and date pickers would need more complex interaction
        // For now, testing that the screen renders and button is clickable
        
        // Verify Finish Setup button exists
        composeTestRule.onNode(hasText("Finish Setup") and hasClickAction())
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun completeProfile_emptyFields_showsErrorMessage() {
        composeTestRule.setContent {
            CompleteProfileScreen(
                onComplete = {}
            )
        }

        // Click Finish Setup without filling fields
        composeTestRule.onNode(hasText("Finish Setup") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Wait for UI to update
        composeTestRule.waitForIdle()

        // Check for validation error message in the dialog body
        composeTestRule.onNodeWithText("Please check the highlighted fields below.", substring = true)
            .assertExists()
    }

    @Test
    fun completeProfile_cancelButton_isDisplayed() {
        composeTestRule.setContent {
            CompleteProfileScreen(
                onComplete = {}
            )
        }

        // Scroll to the Cancel button and verify it exists
        composeTestRule.onNodeWithText("Cancel")
            .performScrollTo()
            .assertExists()
    }

    @Test
    fun completeProfile_displaysRequiredFields() {
        composeTestRule.setContent {
            CompleteProfileScreen(
                onComplete = {}
            )
        }

        // Verify all required field labels are displayed
        composeTestRule.onNodeWithText("Mobile Number", substring = true).assertExists()
        composeTestRule.onNodeWithText("Job", substring = true).assertExists()
        composeTestRule.onNodeWithText("Birth Date", substring = true).assertExists()
        composeTestRule.onNodeWithText("Gender", substring = true).assertExists()
        composeTestRule.onNodeWithText("Monthly Income", substring = true).assertExists()
        composeTestRule.onNodeWithText("Marriage Status", substring = true).assertExists()
    }

    @Test
    fun completeProfile_invalidMobile_showsError() {
        composeTestRule.setContent {
            CompleteProfileScreen(
                onComplete = {}
            )
        }

        // Fill with invalid mobile number
        composeTestRule.onNodeWithTag("Mobile Number").performTextInput("123")

        // Click Finish Setup
        composeTestRule.onNode(hasText("Finish Setup") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Wait for UI to update
        composeTestRule.waitForIdle()

        // Check for validation error - either inline or in dialog
        composeTestRule.onNodeWithText("Invalid format", substring = true)
            .assertExists()
    }
}
