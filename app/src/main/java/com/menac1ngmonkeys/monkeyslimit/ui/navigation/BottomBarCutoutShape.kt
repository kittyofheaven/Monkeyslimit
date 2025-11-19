package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.xr.compose.testing.toDp

/**
 * Creates a shape for a bottom bar with a circular cutout for a FAB.
 *
 * @param fabDiameter The diameter of the Floating Action Button.
 * @param cradleMargin The margin between the FAB and the cutout shape.
 * @param cradleVerticalOffset The vertical offset of the cutout. A positive value moves it down.
 */
fun BottomBarCutoutShape(
    fabDiameter: Dp,
    cradleMargin: Dp = 8.dp,
    cradleVerticalOffset: Dp = 0.dp
) = GenericShape { size, _ ->
    // The path that defines the shape
    moveTo(0f, 0f)

    // Convert all Dp values to Px first
    val density = size.width / size.width.toDp().value
    val fabRadiusPx = (fabDiameter.value / 2f) * density
    val marginPx = cradleMargin.value * density
    val verticalOffsetPx = cradleVerticalOffset.value * density

    val fabCenterX = size.width / 2f

    // 1. Define the start and end of the horizontal line segments (the margin area)
    val cradleMarginStart = fabCenterX - fabRadiusPx - marginPx
    val cradleMarginEnd = fabCenterX + fabRadiusPx + marginPx

    // 2. Define the start and end of the PERFECTLY CIRCULAR arc.
    // Notice this does NOT include the margin.
    val arcStart = fabCenterX - fabRadiusPx
    val arcEnd = fabCenterX + fabRadiusPx

    // 3. Draw the first horizontal line up to the margin start
    lineTo(cradleMarginStart, 0f)

    // 4. Create the bounding box for the arc. This box is now a perfect square
    //    because its width (arcEnd - arcStart) is equal to 2 * fabRadiusPx,
    //    and its height is also 2 * fabRadiusPx.
    val cradleRect = Rect(
        left = arcStart,
        top = -fabRadiusPx + verticalOffsetPx,
        right = arcEnd,
        bottom = fabRadiusPx + verticalOffsetPx
    )

    // 5. Draw the arc. This will now be a perfect half-circle.
    arcTo(
        rect = cradleRect,
        startAngleDegrees = 180f,
        sweepAngleDegrees = -180f,
        forceMoveTo = false
    )

    // 6. Draw the second horizontal line from the margin end to the corner
    lineTo(size.width, 0f)

    // The rest of the shape is the same
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}