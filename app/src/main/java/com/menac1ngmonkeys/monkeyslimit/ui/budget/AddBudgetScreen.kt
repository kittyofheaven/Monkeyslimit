package com.menac1ngmonkeys.monkeyslimit.ui.budget

import AppViewModelProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

private enum class DatePickerTarget {
    START, END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    navController: NavHostController,
    budgetViewModel: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var name by remember { mutableStateOf("") }
    var limitAmount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // --- State for the Date Pickers ---
    var startDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    // We track WHICH field triggered the dialog
    var datePickerTarget by remember { mutableStateOf(DatePickerTarget.START) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Formatter
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    // --- Date Picker Logic ---
    if (showDatePicker) {
        val initialDate = if (datePickerTarget == DatePickerTarget.START) startDate else endDate

        // 1. Calculate the Start Date in UTC Milliseconds (for validation)
        // DatePicker works in UTC, so we must compare against UTC midnight of the start date.
        val startDateMillis = remember(startDate) {
            startDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: Long.MIN_VALUE
        }

        // 2. Define the Validator
        val dateValidator = remember(datePickerTarget, startDateMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // If picking End Date, disable dates before Start Date
                    return if (datePickerTarget == DatePickerTarget.END) {
                        utcTimeMillis >= startDateMillis
                    } else {
                        // No restrictions when picking Start Date
                        true
                    }
                }

                // Optional: You can also restrict years if needed, defaulting to true here
                override fun isSelectableYear(year: Int): Boolean = true
            }
        }

        // 3. Create State with the Validator
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli(),
            selectableDates = dateValidator // <--- Apply the validator here
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        if (datePickerTarget == DatePickerTarget.START) {
                            startDate = selectedDate
                            // Optional: If Start Date is moved AFTER the current End Date,
                            // you might want to clear End Date to avoid invalid states.
                            if (endDate != null && selectedDate.isAfter(endDate)) {
                                endDate = null
                            }
                        } else {
                            endDate = selectedDate
                        }
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Row {
                    if (datePickerTarget == DatePickerTarget.START) {
                        TextButton(onClick = {
                            startDate = LocalDate.now()
                            // Also check if resetting to today invalidates the end date
                            if (endDate != null && LocalDate.now().isAfter(endDate)) {
                                endDate = null
                            }
                            showDatePicker = false
                        }) {
                            Text("Today")
                        }
                    } else {
                        TextButton(onClick = {
                            endDate = null
                            showDatePicker = false
                        }) {
                            Text("Clear")
                        }
                    }
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Budget Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = limitAmount,
            onValueChange = { limitAmount = it },
            label = { Text("Limit Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        // --- Row for Date Fields ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Start Date Field
            OutlinedTextField(
                value = startDate?.format(dateFormatter) ?: "Select Date",
                onValueChange = {},
                readOnly = true,
                label = { Text("Start Date") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(onClick = {
                        datePickerTarget = DatePickerTarget.START
                        showDatePicker = true
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Start Date")
                    }
                }
            )

            // 2. End Date Field
            OutlinedTextField(
                value = endDate?.format(dateFormatter) ?: "Optional",
                onValueChange = {},
                readOnly = true,
                label = { Text("End Date") },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(onClick = {
                        datePickerTarget = DatePickerTarget.END
                        showDatePicker = true
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select End Date")
                    }
                }
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val limit = limitAmount.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && limit > 0 && startDate != null) {
                    budgetViewModel.addBudget(
                        name = name,
                        limitAmount = limit,
                        note = note,
                        startDate = Date.from(startDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        endDate = endDate?.let { Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant()) }
                    )
                    navController.navigateUp()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save Budget")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddBudgetScreenPreview() {
    MonkeyslimitTheme {
        AddBudgetScreen(navController = rememberNavController())
    }
}