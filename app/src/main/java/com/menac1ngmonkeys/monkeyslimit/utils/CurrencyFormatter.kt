package com.menac1ngmonkeys.monkeyslimit.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats a BigDecimal into a Rupiah currency string (e.g., "Rp7,783.00").
 * This is the recommended version for financial data to avoid precision errors.
 */
fun BigDecimal.toRupiahFormat(): String {
    // 1. Use the Locale.Builder to create the Indonesian locale
    //    This is the non-deprecated way.
    val indonesianLocale = Locale.Builder()
        .setLanguage("in")  // "in" is the code for Indonesian
        .setRegion("ID")    // "ID" is the code for Indonesia
        .build()

    // 2. Get the symbols from the locale you just built
    val symbols = DecimalFormatSymbols(indonesianLocale)

    // 3. Note that the pattern here is not the ACTUAL pattern but acts as a symbol
    // the ',' symbol is for Grouping (thousands, millions, billions, etc.)
    // the '.' symbol is for Decimal Places
    val formatter = DecimalFormat("Rp#,##0.00", symbols)

    return formatter.format(this)
}

fun Double.toRupiahFormat(): String {
    // Using NumberFormat is a more robust way to handle currency formatting.
    // It automatically handles locale-specific symbols, grouping, and currency symbols.
    val format = java.text.NumberFormat.getCurrencyInstance(
        Locale.Builder()
            .setLanguage("in")
            .setRegion("ID")
            .build()
    )

    // Check if the number has 6 or more digits before the decimal point (i.e., >= 100,000)
    // In an extension function, 'this' refers to the Double value itself.
    if (this >= 100_000) {
        // For large numbers, remove decimals and set rounding mode to DOWN (truncate)
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        format.roundingMode = RoundingMode.DOWN // <-- THE KEY CHANGE
    } else {
        // For smaller numbers, keep two decimals and use standard rounding
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
        // It's good practice to set this explicitly too for consistency
        format.roundingMode = RoundingMode.HALF_UP
    }

    // Replace "Rp" with "Rp" to ensure consistency, as some Java versions might use "IDR".
    return format.format(this).replace("IDR", "Rp")
}