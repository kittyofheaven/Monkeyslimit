package com.menac1ngmonkeys.monkeyslimit.utils

import java.util.Locale
import kotlin.math.abs

/**
 * Formats a raw numeric value into a compact, human-readable string for UI display.
 *
 * This function converts large numbers into abbreviated forms commonly used in
 * analytics dashboards, such as:
 *
 * - `950`       → `"950"`
 * - `1_500`     → `"1.5K"`
 * - `170_000`   → `"170K"`
 * - `1_250_000` → `"1.25M"`
 * - `1_300_000_000` → `"1.3B"`
 * - `10_000_000_000_000` → `"10T"`
 *
 * It supports Indonesian number formatting (`,` as decimal separator)
 * by using an Indonesian `Locale` when generating the numeric portion.
 *
 * ---
 *
 * ## Ranges and Units
 *
 * | Range                          | Example Input  | Output   |
 * |-------------------------------|----------------|----------|
 * | `< 1_000`                     | `950`          | `"950"`  |
 * | `>= 1_000` and `< 1_000_000`  | `12_400`       | `"12.4K"` |
 * | `>= 1_000_000` and `< 1B`     | `5_080_000`    | `"5.08M"` |
 * | `>= 1_000_000_000` and `< 1T` | `1_300_000_000` | `"1.3B"` |
 * | `>= 1_000_000_000_000`        | `10_000_000_000_000` | `"10T"` |
 *
 * ---
 *
 * ## Trailing Zero Clean-up
 *
 * The output is cleaned using a **regex that removes trailing `.0` or `,0`**
 * *only when it appears immediately before the unit letter*:
 *
 * Examples:
 *
 * - `"1.0M"` → `"1M"`
 * - `"1,00T"` → `"1T"`
 * - `"3.0K"` → `"3K"`
 *
 * This avoids the common bug where naive replacements do:
 *
 * ```kotlin
 * replace(",0", "")
 * ```
 *
 * which incorrectly transforms values like `"5,08M"` → `"58M"`.
 *
 * The regex solution ensures **only trailing zero decimals** are removed,
 * while preserving meaningful fractional digits:
 *
 * - `"5,08M"` stays `"5,08M"` (correct)
 * - `"12,40B"` stays `"12,40B"` (correct)
 *
 * ---
 *
 * ## Locale Behavior
 *
 * This formatter uses an Indonesian locale (`"in", "ID"`) to honor:
 *
 * - `,` for decimal separator
 * - `.` for thousands grouping
 *
 * This produces values consistent with Indonesian financial expectations.
 *
 * ---
 *
 * @param value The numeric value to format.
 * @return A human-readable compact string such as `"1.2K"`, `"5.08M"`, `"3B"`, or `"10T"`.
 */

fun compactNumber(value: Double): String {
    val abs = abs(value)
    val indonesianLocale = Locale.Builder()
        .setLanguage("in")  // "in" is the code for Indonesian
        .setRegion("ID")    // "ID" is the code for Indonesia
        .build()

    // Note: We use .0 for the thresholds to ensure they evaluate as Doubles
    // because 1_000_000_000_000 exceeds the maximum limit of standard integers.
    val raw = when {
        abs >= 1_000_000_000_000.0 -> String.format(indonesianLocale, "%.2fT", value / 1_000_000_000_000.0)
        abs >= 1_000_000_000.0     -> String.format(indonesianLocale, "%.2fB", value / 1_000_000_000.0)
        abs >= 1_000_000.0         -> String.format(indonesianLocale, "%.2fM", value / 1_000_000.0)
        abs >= 1_000.0             -> String.format(indonesianLocale, "%.2fK", value / 1_000.0)
        else -> value.toInt().toString()
    }

    // Include 'T' in the regex character class so it also cleans up Trillions
    return raw.replace(Regex("(,0+)([TBMK])$")) { match ->
        match.groupValues[2] // keep only letter (T/B/M/K)
    } // remove trailing ,0
}