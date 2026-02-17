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

class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signUp_fillsAllFields_and_clicksRegister() {
        var signUpCalled = false
        
        // 1. Render the SignUpScreen
        composeTestRule.setContent {
            SignUpScreen(
                onNavigateToLogin = {},
                onEmailSignUp = { email, password, firstName, lastName, mobile, job, birthDate, gender, income, isMarried ->
                    // Verify the inputs were passed correctly
                    assert(email == "test@monkeys.com")
                    assert(password == "password123")
                    assert(firstName == "John")
                    assert(lastName == "Doe")
                    assert(mobile == "081234567890")
                    signUpCalled = true
                }
            )
        }

        // 2. Fill in First Name
        composeTestRule.onNodeWithTag("First Name").performTextInput("John")

        // 3. Fill in Last Name
        composeTestRule.onNodeWithTag("Last Name").performTextInput("Doe")

        // 4. Fill in Email
        composeTestRule.onNodeWithTag("Email Address").performTextInput("test@monkeys.com")

        // 5. Fill in Mobile Number
        composeTestRule.onNodeWithTag("Mobile Number").performTextInput("081234567890")

        // 6. Fill in Password
        composeTestRule.onNodeWithTag("Password").performTextInput("password123")

        // 7. Click Register button - but it should show validation error for missing fields
        composeTestRule.onNode(hasText("Register") and hasClickAction()).performClick()
        
        // The signup should not be called because other required fields (Job, Birth Date, etc.) are missing
        assert(!signUpCalled) { "Sign up should not be called when required fields are missing" }
    }

    @Test
    fun signUp_emptyFields_showsInlineErrors() {
        composeTestRule.setContent {
            SignUpScreen(
                onNavigateToLogin = {},
                onEmailSignUp = { _, _, _, _, _, _, _, _, _, _ -> }
            )
        }

        // Scroll to Register button and click it without filling fields
        composeTestRule.onNode(hasText("Register") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Wait a bit for the UI to update
        composeTestRule.waitForIdle()

        // Check for inline error messages under the fields (not dialog)
        // Based on the SignUpScreen.kt code, it should show "Required" errors
        composeTestRule.onNodeWithText("Please check the highlighted fields below.", substring = true)
            .assertExists()
    }

    @Test
    fun signUp_clicksAlreadyHaveAccount_navigatesToLogin() {
        var navigatedToLogin = false

        composeTestRule.setContent {
            SignUpScreen(
                onNavigateToLogin = { navigatedToLogin = true },
                onEmailSignUp = { _, _, _, _, _, _, _, _, _, _ -> }
            )
        }

        // Scroll to the link and click it
        composeTestRule.onNodeWithText("Already Have An Account? Log In")
            .performScrollTo()
            .performClick()

        // Wait for the UI to process
        composeTestRule.waitForIdle()

        // Verify navigation was triggered
        assert(navigatedToLogin) { "Navigation to login should have been triggered" }
    }

    @Test
    fun signUp_invalidEmail_showsInlineError() {
        composeTestRule.setContent {
            SignUpScreen(
                onNavigateToLogin = {},
                onEmailSignUp = { _, _, _, _, _, _, _, _, _, _ -> }
            )
        }

        // Fill with invalid email
        composeTestRule.onNodeWithTag("First Name").performTextInput("John")
        composeTestRule.onNodeWithTag("Email Address").performTextInput("invalid-email")
        composeTestRule.onNodeWithTag("Mobile Number").performTextInput("081234567890")
        composeTestRule.onNodeWithTag("Password").performTextInput("password123")

        // Click Register
        composeTestRule.onNode(hasText("Register") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Wait for UI to update
        composeTestRule.waitForIdle()

        // Check for validation error - either inline or in dialog
        composeTestRule.onNodeWithText("Invalid email format", substring = true)
            .assertExists()
    }
}
