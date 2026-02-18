package com.menac1ngmonkeys.monkeyslimit.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        // 1. Find if there is a decimal separator (. or ,)
        val separatorIndex = originalText.indexOfAny(charArrayOf('.', ','))
        val hasDecimal = separatorIndex >= 0

        // 2. Safely separate the Integer part and the Fractional (Decimal) part
        val integerPart = if (hasDecimal) {
            originalText.substring(0, separatorIndex).filter { it.isDigit() }
        } else {
            originalText.filter { it.isDigit() }
        }

        val fractionalDigits = if (hasDecimal) {
            originalText.substring(separatorIndex + 1).filter { it.isDigit() }
        } else {
            ""
        }

        // 3. Format the Integer part (add a dot every 3 digits)
        val formattedInteger = integerPart.reversed().chunked(3).joinToString(".").reversed()

        // 4. Combine them back together, FORCING the decimal separator to be a comma!
        val formattedText = if (hasDecimal) {
            "$formattedInteger,$fractionalDigits"
        } else {
            formattedInteger
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (originalText.isEmpty()) return 0
                val validOffset = offset.coerceIn(0, originalText.length)

                if (!hasDecimal || validOffset <= separatorIndex) {
                    // Cursor is in the whole number part
                    var transformedOffset = 0
                    var originalCount = 0
                    for (char in formattedInteger) {
                        if (originalCount == validOffset) break
                        if (char.isDigit()) originalCount++
                        transformedOffset++
                    }
                    return transformedOffset
                } else {
                    // Cursor is in the decimal part
                    var transformedOffset = formattedInteger.length + 1 // Start after the comma
                    for (i in (separatorIndex + 1) until validOffset) {
                        if (originalText[i].isDigit()) {
                            transformedOffset++
                        }
                    }
                    return transformedOffset
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (formattedText.isEmpty()) return 0
                val validOffset = offset.coerceIn(0, formattedText.length)

                if (!hasDecimal || validOffset <= formattedInteger.length) {
                    // Cursor is in the whole number part
                    var originalOffset = 0
                    for (i in 0 until validOffset) {
                        if (formattedInteger[i].isDigit()) originalOffset++
                    }
                    return originalOffset
                } else {
                    // Cursor is in the decimal part
                    var originalOffset = separatorIndex + 1 // Start after the original separator
                    var transformedCount = 0
                    val targetTransformed = validOffset - (formattedInteger.length + 1)

                    for (i in (separatorIndex + 1) until originalText.length) {
                        if (transformedCount == targetTransformed) break
                        if (originalText[i].isDigit()) {
                            transformedCount++
                        }
                        originalOffset++
                    }
                    return originalOffset
                }
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}