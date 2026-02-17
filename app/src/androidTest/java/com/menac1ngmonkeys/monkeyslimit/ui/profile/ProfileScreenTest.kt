package com.menac1ngmonkeys.monkeyslimit.ui.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.menac1ngmonkeys.monkeyslimit.ui.state.ProfileAuthStatus
import com.menac1ngmonkeys.monkeyslimit.ui.state.ProfileUiState
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_displaysUserInfo() {
        val uiState = ProfileUiState(
            name = "John Doe",
            email = "john@example.com",
            photoUrl = null,
            status = ProfileAuthStatus.Verified(
                user = com.menac1ngmonkeys.monkeyslimit.data.local.entity.User(
                    firstName = "John",
                    lastName = "Doe",
                    email = "john@example.com",
                    mobileNumber = "081234567890",
                    job = "Student",
                    birthDate = java.util.Date(),
                    gender = "Male",
                    income = "Rp 1.000.000 - Rp 3.000.000",
                    isMarried = false,
                    photoUrl = null,
                    isNotificationEnabled = true
                )
            )
        )

        composeTestRule.setContent {
            ProfileHeader(
                uiState = uiState,
                onEditClick = {}
            )
        }

        // Verify user info is displayed
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
    }

    @Test
    fun profileScreen_clicksEditProfile_triggersNavigation() {
        val uiState = ProfileUiState(
            name = "John Doe",
            email = "john@example.com"
        )
        var editClicked = false

        composeTestRule.setContent {
            ProfileHeader(
                uiState = uiState,
                onEditClick = { editClicked = true }
            )
        }

        // Click Edit Profile button
        composeTestRule.onNode(hasText("Edit Profile") and hasClickAction()).performClick()

        // Verify callback was triggered
        assert(editClicked)
    }

    @Test
    fun profileScreen_displaysLogoutButton() {
        composeTestRule.setContent {
            SettingItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Log Out",
                onClick = {}
            )
        }

        // Verify Log Out option is displayed
        composeTestRule.onNodeWithText("Log Out").assertIsDisplayed()
    }
}
