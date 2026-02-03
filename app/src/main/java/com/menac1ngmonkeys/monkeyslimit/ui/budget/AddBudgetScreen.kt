package com.menac1ngmonkeys.monkeyslimit.ui.budget

import AppViewModelProvider
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@Composable
fun AddBudgetScreen(
    navController: NavHostController,
    budgetViewModel: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Collect UI state to check for duplicates
    val uiState by budgetViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var limitAmount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Validation States
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    // Default to current month
    var selectedMonthDate by remember { mutableStateOf(LocalDate.now()) }
    var showMonthPicker by remember { mutableStateOf(false) }

    // Formatter e.g. "January 2026"
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    // --- Custom Month/Year Picker Dialog ---
    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialDate = selectedMonthDate,
            onDismiss = { showMonthPicker = false },
            onDateSelected = { newDate ->
                selectedMonthDate = newDate
                showMonthPicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Budget Name
        BottomLineTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError != null) nameError = null // Clear error on type
            },
            label = "Budget Name",
            placeholder = "e.g. Groceries",
            isError = nameError != null,
            errorMessage = nameError
        )

        // 2. Amount with "Rp" prefix logic
        BottomLineTextField(
            value = limitAmount,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    limitAmount = newValue
                    if (amountError != null) amountError = null // Clear error on type
                }
            },
            label = "Amount",
            placeholder = "0",
            prefix = {
                Text(
                    text = "Rp ",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = amountError != null,
            errorMessage = amountError
        )

        // 3. Starting Month (Clickable Field)
        Box(modifier = Modifier.clickable { showMonthPicker = true }) {
            BottomLineTextField(
                value = selectedMonthDate.format(monthFormatter),
                onValueChange = {},
                label = "Starting Month",
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                }
            )
        }

        // 4. Note (Optional)
        BottomLineTextField(
            value = note,
            onValueChange = { note = it },
            label = "Note (Optional)",
            placeholder = "Add description..."
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = {
                // --- VALIDATION LOGIC ---
                val cleanName = name.trim()
                val amountValue = limitAmount.toDoubleOrNull() ?: 0.0
                var hasError = false

                // 1. Validate Name
                if (cleanName.isBlank()) {
                    nameError = "Budget name cannot be empty"
                    hasError = true
                } else if (uiState.budgetItems.any { it.name.equals(cleanName, ignoreCase = true) }) {
                    nameError = "Budget name already exists"
                    hasError = true
                } else {
                    nameError = null
                }

                // 2. Validate Amount
                if (amountValue <= 0) {
                    amountError = "Amount must be greater than 0"
                    hasError = true
                } else {
                    amountError = null
                }

                // Proceed if valid
                if (!hasError) {
                    val startDateLegacy = Date.from(
                        selectedMonthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                    )

                    budgetViewModel.addBudget(
                        name = cleanName,
                        limitAmount = amountValue,
                        note = note.ifBlank { null },
                        startDate = startDateLegacy,
                        endDate = null
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

@Composable
fun BottomLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        // Label Text
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isError) MaterialTheme.colorScheme.error else Color.Gray,
            fontWeight = FontWeight.Medium
        )

        // Input Field
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            enabled = enabled,
            isError = isError,
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(fontSize = 18.sp, color = Color.LightGray)
                )
            },
            prefix = prefix,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,

                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.LightGray,
                disabledIndicatorColor = Color.LightGray,
                errorIndicatorColor = MaterialTheme.colorScheme.error,

                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error,
                errorTrailingIconColor = MaterialTheme.colorScheme.error
            ),
            singleLine = true
        )

        // Error Message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Custom Dialog for selecting Month and Year.
 * Now supports Animated Transition for Month Grid on Year Change.
 */
@Composable
fun MonthYearPickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialDate.year) }
    var selectedMonthIndex by remember { mutableIntStateOf(initialDate.monthValue - 1) } // 0-11

    // For gesture tracking
    var totalDrag by remember { mutableFloatStateOf(0f) }

    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    // Swipe detection logic on the container
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (totalDrag > 50) {
                                    // Swipe Right -> Decrease Year (Prev)
                                    selectedYear--
                                } else if (totalDrag < -50) {
                                    // Swipe Left -> Increase Year (Next)
                                    selectedYear++
                                }
                                totalDrag = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            }
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Year Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Year")
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of Months with Animation
                AnimatedContent(
                    targetState = selectedYear,
                    transitionSpec = {
                        if (targetState > initialState) {
                            // Year Increased: Slide In from Right, Out to Left
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            // Year Decreased: Slide In from Left, Out to Right
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "MonthGridAnimation"
                ) { _ -> // The content inside recomposes when year changes
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(months) { index, monthName ->
                            val isSelected = index == selectedMonthIndex
                            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                            Surface(
                                onClick = { selectedMonthIndex = index },
                                shape = RoundedCornerShape(24.dp),
                                color = containerColor,
                                modifier = Modifier.height(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = monthName,
                                        color = contentColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val newDate = LocalDate.of(selectedYear, selectedMonthIndex + 1, 1)
                        onDateSelected(newDate)
                    }) {
                        Text("OK")
                    }
                }
            }
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