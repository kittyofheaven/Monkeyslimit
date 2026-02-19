package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import TransactionDetailViewModelFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeysDatePicker
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeysTimePicker
import com.menac1ngmonkeys.monkeyslimit.utils.CurrencyVisualTransformation
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat
import com.menac1ngmonkeys.monkeyslimit.viewmodel.TransactionDetailViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    onNavigateBack: () -> Unit,
    viewModel: TransactionDetailViewModel = viewModel(
        factory = TransactionDetailViewModelFactory(transactionId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val transaction = uiState.transaction

    // --- DATE & TIME PICKER STATE ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempSelectedDate by remember { mutableStateOf<Long?>(null) }

    // --- DELETE DIALOG STATE ---
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 1. Date Picker
    MonkeysDatePicker(
        show = showDatePicker,
        initialDate = Date(uiState.editDate),
        onDismiss = { showDatePicker = false },
        onDateSelected = { dateMillis ->
            if (dateMillis != null) {
                tempSelectedDate = dateMillis
                showDatePicker = false
                showTimePicker = true // Proceed to Time Picker
            }
        },
        proceedText = "Next"
    )

    // 2. Time Picker
    MonkeysTimePicker(
        show = showTimePicker,
        initialDate = Date(uiState.editDate),
        onDismiss = { showTimePicker = false },
        onTimeSelected = { hour, minute ->
            val dateBase = tempSelectedDate ?: uiState.editDate

            // Combine Date + Time
            val cal = Calendar.getInstance().apply {
                timeInMillis = dateBase
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

            viewModel.onEditDateChange(cal.timeInMillis)
            showTimePicker = false
        }
    )

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Delete Transaction",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this transaction? This action cannot be undone.",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTransaction(onSuccess = onNavigateBack)
                    },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isEditing) "Edit Transaction" else "Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (transaction != null) {
                    if (uiState.isEditing) {
                        Row {
                            IconButton(onClick = { viewModel.toggleEditMode() }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                            }
                            IconButton(onClick = { viewModel.saveChanges() }) {
                                Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else {
                        Row {
                            IconButton(onClick = { viewModel.toggleEditMode() }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (transaction != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- AMOUNT ---
                    if (uiState.isEditing) {
                        // Strips trailing zeroes dynamically for clean UI editing
                        val displayAmount = uiState.editAmount
                            .removeSuffix(".0")
                            .replace('.', ',')

                        OutlinedTextField(
                            value = uiState.editAmount,
                            onValueChange = { newValue ->
                                val cleanText = newValue.replace(Regex("[^0-9.,]"), "").replace('.', ',')
                                val parts = cleanText.split(',')
                                val finalStr = if (parts.size > 1) "${parts[0]},${parts[1]}" else parts[0]

                                viewModel.onEditAmountChange(finalStr)
                            },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = CurrencyVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    } else {
                        val isExpense = transaction.type == TransactionType.EXPENSE
                        val amountColor = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                        val prefix = if (isExpense) "-" else "+"

                        Text(
                            text = prefix + transaction.totalAmount.toRupiahFormat(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // --- CATEGORY ---
                    if (uiState.isEditing) {
                        var expanded by remember { mutableStateOf(false) }
                        // Look up name in allCategories to ensure we find it even if filter somehow missed it (safety)
                        val selectedCategoryName = uiState.allCategories.find { it.id == uiState.editCategoryId }?.name ?: "Select Category"

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCategoryName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                // Use the Pre-Filtered list from ViewModel
                                if (uiState.availableCategories.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No categories available") },
                                        onClick = { expanded = false }
                                    )
                                } else {
                                    uiState.availableCategories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = {
                                                viewModel.onEditCategoryChange(category.id)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = uiState.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // --- DETAILS CARD ---
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // DATE FIELD
                            if (uiState.isEditing) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePicker = true } // Triggers the MonkeysDatePicker
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconBox(Icons.Default.CalendarToday)
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text("Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        Text(formatDate(Date(uiState.editDate)), style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                DetailRow(Icons.Default.CalendarToday, "Date", formatDate(transaction.date))
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                            // NOTE FIELD
                            if (uiState.isEditing) {
                                OutlinedTextField(
                                    value = uiState.editNote,
                                    onValueChange = viewModel::onEditNoteChange,
                                    label = { Text("Note") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            } else {
                                DetailRow(Icons.Default.Description, "Note", transaction.note ?: "-")
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                            // TYPE FIELD
                            if (uiState.isEditing) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconBox(Icons.Default.AttachMoney)
                                    Spacer(Modifier.width(16.dp))
                                    Row(modifier = Modifier.weight(1f)) {
                                        FilterChip(
                                            selected = uiState.editType == TransactionType.INCOME,
                                            onClick = { viewModel.onEditTypeChange(TransactionType.INCOME) },
                                            label = { Text("Income") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        FilterChip(
                                            selected = uiState.editType == TransactionType.EXPENSE,
                                            onClick = { viewModel.onEditTypeChange(TransactionType.EXPENSE) },
                                            label = { Text("Expense") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            } else {
                                val typeLabel = if (transaction.type == TransactionType.EXPENSE) "Expense" else "Income"
                                DetailRow(Icons.Default.AttachMoney, "Type", typeLabel)
                            }

                            // BUDGET FIELD (Only visible if the current selected type is EXPENSE)
                            val isCurrentTypeExpense = if (uiState.isEditing) uiState.editType == TransactionType.EXPENSE else transaction.type == TransactionType.EXPENSE

                            if (isCurrentTypeExpense) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                                if (uiState.isEditing) {
                                    var budgetExpanded by remember { mutableStateOf(false) }
                                    // Use uiState.allBudgets and uiState.editBudgetId from your ViewModel
                                    val selectedBudgetName = uiState.allBudgets.find { it.id == uiState.editBudgetId }?.name ?: "None"

                                    ExposedDropdownMenuBox(
                                        expanded = budgetExpanded,
                                        onExpandedChange = { budgetExpanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = selectedBudgetName,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Budget") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = budgetExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                focusedLabelColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = budgetExpanded,
                                            onDismissRequest = { budgetExpanded = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("None") },
                                                onClick = {
                                                    viewModel.onEditBudgetChange(null)
                                                    budgetExpanded = false
                                                }
                                            )
                                            uiState.allBudgets.forEach { budget ->
                                                DropdownMenuItem(
                                                    text = { Text(budget.name) },
                                                    onClick = {
                                                        viewModel.onEditBudgetChange(budget.id)
                                                        budgetExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Make sure uiState.budgetName is populated in your ViewModel when loading the transaction!
                                    DetailRow(Icons.Default.AccountBalanceWallet, "Budget", uiState.budgetName ?: "None")
                                }
                            }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Transaction not found")
                }
            }
        }
    }
}

@Composable
fun IconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        IconBox(icon)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatDate(date: Date): String = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(date)