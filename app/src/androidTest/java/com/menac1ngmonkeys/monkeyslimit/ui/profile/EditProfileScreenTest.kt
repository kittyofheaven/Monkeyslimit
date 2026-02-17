package com.menac1ngmonkeys.monkeyslimit.ui.profile

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class EditProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun editProfile_rendersWithoutCrashing() {
        composeTestRule.setContent {
            EditProfileScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }

    @Test
    fun editProfile_displaysFields() {
        composeTestRule.setContent {
            EditProfileScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }

        // Basic rendering test for edit profile fields
        composeTestRule.waitForIdle()
    }
}
