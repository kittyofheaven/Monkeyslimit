package com.menac1ngmonkeys.monkeyslimit.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.ui.state.Timeframe
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun DateRangeSelector(
    selectedTimeframe: Timeframe,
    currentDate: LocalDate,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateLabel = getDateLabel(selectedTimeframe, currentDate)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Subtle background like design
                shape = RoundedCornerShape(50) // Pill shape
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = dateLabel,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getDateLabel(timeframe: Timeframe, date: LocalDate): String {
    val locale = Locale.getDefault()
    return when (timeframe) {
        Timeframe.DAILY -> {
            // "1 January 2026"
            date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
        }
        Timeframe.WEEKLY -> {
            // "1 Jan - 7 Jan 2026" or "28 Dec 2025 - 3 Jan 2026"
            val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            val startFmt = DateTimeFormatter.ofPattern("d MMM", locale)
            val endPattern = if (startOfWeek.year == endOfWeek.year) "d MMM yyyy" else "d MMM yyyy"
            val endFmt = DateTimeFormatter.ofPattern(endPattern, locale)

            "${startOfWeek.format(startFmt)} - ${endOfWeek.format(endFmt)}"
        }
        Timeframe.MONTHLY -> {
            // "January 2026"
            date.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
        }
        Timeframe.YEARLY -> {
            // "2026"
            date.format(DateTimeFormatter.ofPattern("yyyy", locale))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateRangeSelectorPreview() {
    MonkeyslimitTheme {
        DateRangeSelector(
            selectedTimeframe = Timeframe.MONTHLY,
            currentDate = LocalDate.of(2026, 1, 1),
            onPrevClick = {},
            onNextClick = {}
        )
    }
}