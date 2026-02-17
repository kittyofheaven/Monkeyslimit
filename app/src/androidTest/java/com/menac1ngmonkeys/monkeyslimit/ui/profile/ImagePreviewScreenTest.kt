package com.menac1ngmonkeys.monkeyslimit.ui.profile

import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ImagePreviewScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun imagePreview_rendersWithoutCrashing() {
        val mockUri = Uri.parse("content://test/image.jpg")
        
        composeTestRule.setContent {
            ImagePreviewScreen(
                navController = androidx.navigation.compose.rememberNavController(),
                imageUri = mockUri
            )
        }

        // Verify screen renders
        composeTestRule.waitForIdle()
    }

    @Test
    fun imagePreview_displaysImage() {
        val mockUri = Uri.parse("content://test/image.jpg")
        
        composeTestRule.setContent {
            ImagePreviewScreen(
                navController = androidx.navigation.compose.rememberNavController(),
                imageUri = mockUri
            )
        }

        // Basic rendering test for image preview
        composeTestRule.waitForIdle()
    }
}
