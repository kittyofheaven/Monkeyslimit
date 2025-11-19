package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import kotlin.math.roundToInt

/**
 * A Text composable that scrolls horizontally when its content overflows.
 * It pauses at the start, scrolls to the end, pauses, and then reverses.
 *
 * @param text The text to be displayed.
 * @param modifier The modifier to be applied to the component's container.
 * @param style The text style to be applied.
 * @param color The color of the text.
 * @param fontSize The font size to apply. Defaults to Unspecified (uses style's size).
 */
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = style.fontSize
) {
    val textLayoutInfo = remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val isOverflowing by remember {
        derivedStateOf {
            val info = textLayoutInfo.value
            info != null && info.first > info.second
        }
    }

    val animationSpec: InfiniteRepeatableSpec<Float> = if (isOverflowing) {
        val diff = textLayoutInfo.value?.let { it.first - it.second } ?: 0
        val duration = 2000 // constant duration so that all animations start and ends at the same time
        val delay = 1500
        val targetValue = -diff.toFloat()

        infiniteRepeatable(
            animation = keyframes {
                durationMillis = delay + duration + delay

                // Start at 0, wait for delay
                0f at 0 using LinearEasing
                0f at delay using LinearEasing

                // Move to target over 'duration'
                targetValue at (delay + duration) using LinearEasing

                // Hold at target for halfDelay
                targetValue at (delay + duration + delay) using LinearEasing
            },
            repeatMode = RepeatMode.Reverse
        )
    } else {
        infiniteRepeatable(animation = keyframes { durationMillis = 1 })
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MarqueeTransition")

    // We don't use 'targetValue' in animateFloat here because the keyframes
    // define the exact values at specific times.
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isOverflowing) {
            (textLayoutInfo.value!!.second - textLayoutInfo.value!!.first).toFloat()
        } else {
            0f
        },
        animationSpec = animationSpec,
        label = "MarqueeOffsetX"
    )

    Layout(
        content = {
            Text(
                text = text,
                style = style,
                color = color,
                maxLines = 1,
                softWrap = false,
                fontSize = fontSize
            )
        },
        modifier = modifier.clipToBounds()
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            return@Layout layout(0, 0) {}
        }
        val textMeasurable = measurables.first()

        // 1. Measure the Text with infinite width to get its true width.
        val textPlaceable = textMeasurable.measure(constraints.copy(maxWidth = Constraints.Infinity))

        // 2. Store the text width and container width for animation.
        textLayoutInfo.value = Pair(textPlaceable.width, constraints.maxWidth)

        // 3. The Layout itself takes up the originally constrained space.
        layout(width = constraints.maxWidth, height = textPlaceable.height) {
            // 4. Place the fully-drawn Text inside the layout, applying the animated offset.
            textPlaceable.placeRelative(x = offsetX.roundToInt(), y = 0)
        }
    }
}