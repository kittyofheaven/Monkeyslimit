package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

// Matches colors from AuthComponents.kt
private val AuthPrimaryGreen = Color(0xFF6C8B08)
private val AuthCardBg = Color(0xFFF9F9F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonkeysDatePicker(
    show: Boolean,
    initialDate: Date,
    disableFutureDates: Boolean = false,
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    proceedText: String = "OK"
) {
    if (!show) return

    // Create the logic to restrict dates
    val selectableDates = remember(disableFutureDates) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return if (disableFutureDates) {
                    // DatePicker returns selected dates at midnight UTC.
                    // We need to calculate "Today" in the user's local timezone,
                    // then convert it to midnight UTC so they match perfectly!
                    val localToday = Calendar.getInstance()

                    val todayUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        set(Calendar.YEAR, localToday.get(Calendar.YEAR))
                        set(Calendar.MONTH, localToday.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, localToday.get(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    // Allow if the selected date is less than or equal to today
                    utcTimeMillis <= todayUtc
                } else {
                    true // Allow all dates if restriction is off
                }
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time,
        selectableDates = selectableDates
    )

    // 🔥 Match colors from AuthDatePickerDialog
    val pickerColors = DatePickerDefaults.colors(
        containerColor = Color.White,
        selectedDayContainerColor = AuthPrimaryGreen,
        selectedDayContentColor = Color.White,
        todayContentColor = AuthPrimaryGreen,
        todayDateBorderColor = AuthPrimaryGreen,
        titleContentColor = AuthPrimaryGreen,
        headlineContentColor = AuthPrimaryGreen,
        dayInSelectionRangeContainerColor = AuthPrimaryGreen.copy(alpha = 0.35f),
        dayInSelectionRangeContentColor = AuthPrimaryGreen
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onDateSelected(datePickerState.selectedDateMillis) },
                colors = ButtonDefaults.textButtonColors(contentColor = AuthPrimaryGreen)
            ) {
                Text(proceedText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        colors = DatePickerDefaults.colors(containerColor = Color.White),
        tonalElevation = 6.dp,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        DatePicker(
            state = datePickerState,
            colors = pickerColors,
            title = {
                Text(
                    text = "Select date",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp),
                    color = AuthPrimaryGreen
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonkeysTimePicker(
    show: Boolean,
    initialDate: Date,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    if (!show) return

    val cal = Calendar.getInstance().apply { time = initialDate }
    val timePickerState = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
        is24Hour = true
    )

    // Construct a Dialog manually to match the style of DatePickerDialog
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true),
    ) {
        Surface(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select time",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    color = AuthPrimaryGreen
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = AuthCardBg, // Slightly off-white/gray like Auth fields
                        selectorColor = AuthPrimaryGreen,
                        containerColor = Color.White,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Color.Black,

                        periodSelectorBorderColor = AuthPrimaryGreen,
                        periodSelectorSelectedContainerColor = AuthPrimaryGreen.copy(alpha = 0.2f),
                        periodSelectorSelectedContentColor = AuthPrimaryGreen,
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorUnselectedContentColor = Color.Gray,

                        timeSelectorSelectedContainerColor = AuthPrimaryGreen.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = AuthPrimaryGreen,
                        timeSelectorUnselectedContainerColor = AuthCardBg,
                        timeSelectorUnselectedContentColor = Color.Black,
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onTimeSelected(timePickerState.hour, timePickerState.minute)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = AuthPrimaryGreen)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}