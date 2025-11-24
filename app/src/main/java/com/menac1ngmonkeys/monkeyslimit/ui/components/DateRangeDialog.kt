package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onApply: (startDate: LocalDate?, endDate: LocalDate?) -> Unit,
    state: DateRangePickerState,
) {
    if (!show) return
    // 🔥 ONE source of truth for all picker colors
    val pickerColors = DatePickerDefaults.colors(
        headlineContentColor = MaterialTheme.colorScheme.onSurface,
        weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

        // selected start/end day (filled circle)
        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,

        // the bar BETWEEN start & end in a range
        dayInSelectionRangeContainerColor =
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSurface,

        // today outline
        todayDateBorderColor = MaterialTheme.colorScheme.primary,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startDate = state.selectedStartDateMillis.toLocalDateOrNull()
                    val endDate = state.selectedEndDateMillis.toLocalDateOrNull()
                    onApply(startDate, endDate)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                // Clear button only shown if there is a selection
                if (state.selectedStartDateMillis != null ||
                    state.selectedEndDateMillis != null
                ) {
                    TextButton(
                        onClick = {
                            // reset range selection
                            state.setSelection(null, null)
                        }
                    ) {
                        Text("Clear")
                    }
                    Spacer(Modifier.width(8.dp))
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = pickerColors,
        tonalElevation = 6.dp,
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        )
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.padding(bottom = 8.dp),
            colors = pickerColors,
            title = {
                Text(
                    text = "Select date range",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 16.dp, end = 24.dp)
                )
            },
            headline = {
                val startText = state.selectedStartDateMillis
                    ?.toLocalDateString()
                    ?: "Start date"
                val endText = state.selectedEndDateMillis
                    ?.toLocalDateString()
                    ?: "End date"

                Text(
                    text = "$startText \u2014 $endText",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            },
            showModeToggle = true,
        )
    }
}

private fun Long.toLocalDateString(): String {
    val date = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    return date.format(formatter)
}

private fun Long?.toLocalDateOrNull(): java.time.LocalDate? {
    if (this == null) return null
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

