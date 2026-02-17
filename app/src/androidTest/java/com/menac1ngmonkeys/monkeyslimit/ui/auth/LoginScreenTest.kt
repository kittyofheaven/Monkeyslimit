package com.menac1ngmonkeys.monkeyslimit.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun login_inputsText_and_clicksButton() {
        // 1. Render the LoginScreen
        composeTestRule.setContent {
            LoginScreen(
                onGoogleSignIn = {},
                onEmailSignIn = { email, password ->
                    // Verify the inputs were passed correctly
                    assert(email == "test@monkeys.com")
                    assert(password == "password123")
                },
                onNavigateToSignUp = {}
            )
        }

        // 2. Type into Email field
        // Note: effectively finds the TextField with label "Email"
        composeTestRule.onNodeWithTag("Email").performTextInput("test@monkeys.com")

        // 3. Type into Password field
        composeTestRule.onNodeWithTag("Password").performTextInput("password123")

        // 4. Click the "Log In" button
        // Find the node that has the text "Log In" AND is clickable
        composeTestRule.onNode(hasText("Log In") and hasClickAction()).performClick()
    }

    @Test
    fun forgotPassword_clicks_showsDialog() {
        composeTestRule.setContent {
            LoginScreen(
                onGoogleSignIn = {},
                onEmailSignIn = { _, _ -> },
                onNavigateToSignUp = {}
            )
        }

        // 1. Find "Forgot Password?" text and click it
        composeTestRule.onNodeWithText("Forgot Password?").performClick()

        // 2. Assert that the "Reset Password" dialog title appears
        // (This confirms your dialog state logic works!)
        composeTestRule.onNodeWithText("Reset Password").assertIsDisplayed()
    }
}