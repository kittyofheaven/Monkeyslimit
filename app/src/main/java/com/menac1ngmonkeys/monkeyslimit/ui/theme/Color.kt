package com.menac1ngmonkeys.monkeyslimit.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun Color.lighten(factor: Float = 0.2f): Color {
    // factor: 0.0 stays same, 1.0 becomes white
    val argb = this.toArgb()
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(argb, hsl)

    // Increase lightness
    // hsl[2] is lightness (0..1)
    hsl[2] = (hsl[2] + (1f - hsl[2]) * factor).coerceIn(0f, 1f)

    return Color(ColorUtils.HSLToColor(hsl))
}

//Light mode
val LightWhite = Color(0xFFFFFFFF)
val LightGrey = Color(0xFFF0F0F0)
val LightBlack= Color(0xFF052224)
val LightGreen400 = Color(0xFF2B5D54)
val LightGreen300 = Color(0xFF447069)
val LightGreen200 = Color(0xFF809E98)
val LightGreen100 = Color(0xFFDFF7E2)
val LightYellow = Color(0xFFFCBD09)
val LightRed = Color(0xFFBF0003)

val Wheat = Color(0xFFF9E2B6)
val Sunglow = Color(0xFFFACF69)
val GoldenRod = Color(0xFFD5A007)
val Avocado = Color(0xFF6C8B08)
val DrabDarkBrown = Color(0xFF1E1400)

//Dark mode
val DarkBlack = Color(0xFF000000)
val DarkGrey = Color(0xFF232121)
val DarkOrange = Color(0xFFFCBD09)
val DarkYellow = Color(0xFFE2E326)
val DarkRed = Color(0xFFFF0707)

val PinkRed = Color(0xFFF35450)
