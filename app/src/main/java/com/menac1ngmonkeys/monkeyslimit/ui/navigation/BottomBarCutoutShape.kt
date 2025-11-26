package com.menac1ngmonkeys.monkeyslimit.ui.navigation

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Creates a shape for a bottom bar with a circular cutout for a FAB.
 *
 * @param fabDiameter The diameter of the Floating Action Button.
 * @param cradleMargin The margin between the FAB and the cutout shape.
 * @param cradleVerticalOffset The vertical offset of the cutout. A positive value moves it down.
 */
@Composable
fun bottomBarCutoutShape(
    fabDiameter: Dp,
    cradleMargin: Dp = 8.dp,
    cradleVerticalOffset: Dp = 0.dp,
) = with(LocalDensity.current) {
    val fabDiameterPx = fabDiameter.toPx()
    val fabRadiusPx = fabDiameterPx / 2f
    val cradleMarginPx = cradleMargin.toPx()
    val cradleVerticalOffsetPx = cradleVerticalOffset.toPx()

    // Cutout radius = FAB radius + margin → actual gap around FAB
    val cradleRadiusPx = fabRadiusPx + cradleMarginPx

    GenericShape { size: Size, _: LayoutDirection ->
        val fabCenterX = size.width / 2f

        // Where the arc starts and ends on the top edge
        val arcStartX = fabCenterX - cradleRadiusPx
        val arcEndX = fabCenterX + cradleRadiusPx

        // Rect that defines the cutout circle
        val cradleRect = Rect(
            left = arcStartX,
            top = -cradleRadiusPx + cradleVerticalOffsetPx,
            right = arcEndX,
            bottom = cradleRadiusPx + cradleVerticalOffsetPx,
        )

        // Start top-left
        moveTo(0f, 0f)

        // Flat segment until the left of the cutout
        lineTo(arcStartX, 0f)

        // Draw the semicircular cutout
        arcTo(
            rect = cradleRect,
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false,
        )

        // Flat segment after the cutout
        lineTo(arcEndX, 0f)

        // Complete the rectangle downwards
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
}
