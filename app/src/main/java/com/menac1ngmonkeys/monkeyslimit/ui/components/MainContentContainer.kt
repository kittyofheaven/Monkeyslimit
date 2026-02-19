package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A styled container for the main content area of a screen.
 *
 * This component provides a consistent look with rounded top corners, a themed
 * background color, and standard internal padding. It is designed to sit below
 * other elements like a top bar or a summary card.
 *
 * The content of the container is provided via a composable lambda, allowing for
 * flexible use across different screens.
 *
 * @param modifier The modifier to be applied to the container. It is recommended
 *                 to use modifiers like `fillMaxSize()` to ensure the container
 *                 takes up the appropriate space.
 * @param containerColor The background color of the container. Defaults to the
 *                       theme's background color. You could also pass a Brush here
 *                       if you change the parameter type.
 * @param content The composable content to be placed inside the container. This
 *                content will be aligned to the top-start by default.
 */
@Composable
fun MainContentContainer(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    val containerShape = RoundedCornerShape(
        topStartPercent = 15,
        topEndPercent = 15,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 8.dp,
                shape = containerShape,
                clip = false // Set to false so the shadow itself isn't clipped
            )
            .clip(containerShape)
            .background(containerColor)
            .padding(
                top = 20.dp,
                start = 25.dp,
                end = 25.dp,
//                bottom = 30.dp
            ),
        contentAlignment = Alignment.TopStart
    ) {
        // The provided content is placed here
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun MainContentContainerPreview() {
    // This preview demonstrates how to use the container
    MainContentContainer {
        Column {
            Text(
                text = "Content Title",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = "This is the content that goes inside the container.")
            Button(onClick = { /*TODO*/ }) {
                Text("A Button")
            }
        }
    }
}