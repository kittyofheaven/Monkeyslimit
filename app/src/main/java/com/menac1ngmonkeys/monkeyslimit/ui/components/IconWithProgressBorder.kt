package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.R

/**
 * A composable that displays an icon inside a circular progress border.
 *
 * @param modifier The modifier to be applied to the component.
 * @param iconResId The resource ID of the icon to be displayed.
 * @param progress The progress value between 0.0f and 1.0f.
 * @param size The total size of the component.
 * @param strokeWidth The thickness of the progress border.
 * @param progressColor The color of the progress arc.
 * @param trackColor The color of the background arc (the track).
 * @param iconTint The tint color for the icon.
 */
@Composable
fun IconWithProgressBorder(
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int,
    progress: Float,
    size: Dp = 64.dp,
    strokeWidth: Dp = 5.dp,
    progressColor: Color = Color.White,
    trackColor: Color = Color.White.copy(alpha = 0.3f), // Faint track color
    iconTint: Color = Color.White
) {
    // We need the stroke width in pixels for the Canvas drawing
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // The Canvas is used for custom drawing of the arcs
        Canvas(modifier = Modifier.matchParentSize()) {
            // Calculate the sweep angle based on progress. A full circle is 360 degrees.
            // We clamp the progress value between 0 and 1 to prevent drawing errors.
            val sweepAngle = 360 * progress.coerceIn(0f, 1f)

            // 1. Draw the background track (a full, faint circle)
            drawArc(
                color = trackColor,
                startAngle = -90f, // Start at the top (12 o'clock)
                sweepAngle = 360f,
                useCenter = false, // This is crucial for drawing a stroke, not a pie slice
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 2. Draw the progress arc on top of the track
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // 3. The Icon is placed in the center, on top of the Canvas
        Icon(
            // Padding ensures the icon doesn't touch the border
            modifier = Modifier.padding(strokeWidth + 4.dp),
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconTint
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun IconWithProgressBorderPreview() {
    IconWithProgressBorder(
        iconResId = R.drawable.savings_48px,
        progress = 0.75f // Show it 75% complete
    )
}