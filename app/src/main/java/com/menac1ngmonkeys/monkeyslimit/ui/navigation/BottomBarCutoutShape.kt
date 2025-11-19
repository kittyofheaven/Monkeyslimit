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
    cradleVerticalOffset: Dp = 0.dp
) = with(LocalDensity.current) {
    // Convert all Dp to Px HERE (inside @Composable, outside GenericShape)
    val fabDiameterPx = fabDiameter.toPx()
    val cradleMarginPx = cradleMargin.toPx()
    val cradleVerticalOffsetPx = cradleVerticalOffset.toPx()

    // Now create the shape using the pixel values
    GenericShape { size, _ ->
        val fabRadiusPx = fabDiameterPx / 2f

        moveTo(0f, 0f)

        val fabCenterX = size.width / 2f

        val cradleMarginStart = fabCenterX - fabRadiusPx - cradleMarginPx
        val cradleMarginEnd = fabCenterX + fabRadiusPx + cradleMarginPx

        val arcStart = fabCenterX - fabRadiusPx
        val arcEnd = fabCenterX + fabRadiusPx

        lineTo(cradleMarginStart, 0f)

        val cradleRect = Rect(
            left = arcStart,
            top = -fabRadiusPx + cradleVerticalOffsetPx,
            right = arcEnd,
            bottom = fabRadiusPx + cradleVerticalOffsetPx
        )

        arcTo(
            rect = cradleRect,
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )

        lineTo(cradleMarginEnd, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
}