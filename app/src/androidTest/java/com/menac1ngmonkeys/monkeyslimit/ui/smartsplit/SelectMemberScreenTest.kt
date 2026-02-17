package com.menac1ngmonkeys.monkeyslimit.ui.smartsplit

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class SelectMemberScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun selectMember_rendersWithoutCrashing() {
        composeTestRule.setContent {
            SelectMemberScreen(
                excludedNames = emptyList(),
                onNavigateBack = {},
                onSelectionConfirmed = {}
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }

    @Test
    fun selectMember_displaysContent() {
        composeTestRule.setContent {
            SelectMemberScreen(
                excludedNames = listOf("John"),
                onNavigateBack = {},
                onSelectionConfirmed = {}
            )
        }

        // Basic rendering test
        composeTestRule.waitForIdle()
    }
}
