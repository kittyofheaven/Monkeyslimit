package com.menac1ngmonkeys.monkeyslimit.ui.splash

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splashScreen_rendersWithoutCrashing() {
        composeTestRule.setContent {
            SplashScreenContent()
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }

    @Test
    fun splashScreen_displaysContent() {
        composeTestRule.setContent {
            SplashScreenContent()
        }

        // Basic rendering test for splash screen
        // Splash screens typically auto-navigate after a delay
        composeTestRule.waitForIdle()
    }
}
