package com.menac1ngmonkeys.monkeyslimit.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats a BigDecimal into a Rupiah currency string.
 * Automatically compacts numbers >= 100,000,000 to shorter formats (e.g., Rp1.5B, Rp10T)
 */
fun BigDecimal.toRupiahFormat(compactLargeNumbers: Boolean = true): String {
    val absValue = this.abs()

    // 1. Automatically compact huge numbers (>= 100 Million, automatically scales up to Trillions!)
    if (compactLargeNumbers && absValue >= BigDecimal("100000000")) {
        val prefix = if (this < BigDecimal.ZERO) "-" else ""
        return "${prefix}Rp${compactNumber(absValue.toDouble())}"
    }

    // 2. Standard formatting for smaller numbers
    val indonesianLocale = Locale.Builder()
        .setLanguage("in")
        .setRegion("ID")
        .build()

    val symbols = DecimalFormatSymbols(indonesianLocale)
    val formatter = DecimalFormat("Rp#,##0.00", symbols)

    return formatter.format(this)
}

/**
 * Formats a Double into a Rupiah currency string.
 * Automatically compacts numbers >= 100,000,000 to shorter formats (e.g., Rp1.5B, Rp10T)
 */
fun Double.toRupiahFormat(compactLargeNumbers: Boolean = true): String {
    val absValue = kotlin.math.abs(this)

    // 1. Automatically compact huge numbers (>= 100 Million, automatically scales up to Trillions!)
    if (compactLargeNumbers && absValue >= 100_000_000.0) {
        val prefix = if (this < 0) "-" else ""
        return "${prefix}Rp${compactNumber(absValue)}"
    }

    // 2. Standard formatting for smaller numbers
    val format = java.text.NumberFormat.getCurrencyInstance(
        Locale.Builder()
            .setLanguage("in")
            .setRegion("ID")
            .build()
    )

    if (absValue >= 100_000.0) {
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        format.roundingMode = RoundingMode.DOWN
    } else {
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        format.roundingMode = RoundingMode.HALF_UP
    }

    // Replace "IDR" with "Rp" and clean up any weird spaces Java might add
    return format.format(this)
        .replace("IDR", "Rp")
        .replace("Rp\\s+".toRegex(), "Rp")
}