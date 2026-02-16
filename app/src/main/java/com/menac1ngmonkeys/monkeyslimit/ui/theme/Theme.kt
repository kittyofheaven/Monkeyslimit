package com.menac1ngmonkeys.monkeyslimit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    // 1. PRIMARY: The Green color seen on the FAB and Income amounts
    primary = Avocado,
    onPrimary = LightWhite, // Text/Icon on top of primary (White on Green)

    // 2. PRIMARY CONTAINER: The large Yellow background for the dashboard card
    primaryContainer = Sunglow,
    onPrimaryContainer = LightBlack, // Text inside the yellow card

    // 3. SECONDARY: Used for Chips/Pills (like "Monthly", "Pantry" in the screenshot)
    secondary = Sunglow, // Or LightYellow if you want it brighter
    onSecondary = LightBlack,

    // 4. SECONDARY CONTAINER: Lighter elements
    secondaryContainer = Wheat,
    onSecondaryContainer = LightBlack,

    // 5. TERTIARY: Used for Expenses (Red text)
    tertiary = PinkRed,
    onTertiary = LightWhite,

    // 6. BACKGROUND & SURFACE
    background = LightWhite,
    onBackground = LightBlack, // Main text color

    surface = LightWhite, // Card backgrounds
    onSurface = LightBlack, // Text on cards

    // 7. INVERSE (For the Bottom Navigation Bar which is dark)
    inverseSurface = DrabDarkBrown,
    inverseOnSurface = LightWhite,
    inversePrimary = Avocado, // Primary color when on top of dark surface

    // 8. OUTLINES (Dividers, borders)
    outline = LightGrey,
    outlineVariant = LightGreen200
)

private val DarkColorScheme = darkColorScheme(
    background = DarkBlack,
    onBackground =  LightWhite,

    surface = DarkBlack,

    primary = DarkOrange,
    onPrimary = DarkBlack,

    secondary = LightWhite,
    tertiary = PinkRed

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MonkeyslimitTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
    darkTheme: Boolean = false, // Force light mode for now
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Do not change colors based on the user's device settings
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}