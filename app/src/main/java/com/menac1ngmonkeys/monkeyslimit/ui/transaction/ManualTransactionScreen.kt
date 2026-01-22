package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import AppViewModelProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeysDatePicker
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeysTimePicker
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ManualTransactionViewModel
import java.util.Date

@Composable
fun ManualTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManualTransactionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    ManualTransactionContent(
        budgets = uiState.budgets,
        categories = uiState.categories,
        onNavigateBack = onNavigateBack,
        onSave = { date, amount, name, categoryId, budgetId, type ->
            viewModel.saveTransaction(date, amount, name, categoryId, budgetId, type, onNavigateBack)
        }
    )
}

@Composable
fun ManualTransactionContent(
    budgets: List<Budgets>,
    categories: List<Categories>,
    onNavigateBack: () -> Unit,
    onSave: (Date, Double, String, Int, Int?, TransactionType) -> Unit
) {
    // --- Form State ---
    var amount by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var transactionDate by remember { mutableStateOf(Date()) }

    // Dialogs
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Filter Categories based on Type
    val filteredCategories = remember(categories, transactionType) {
        categories.filter { it.type == transactionType }
    }

    // Selected Items (Default to first available)
    var selectedCategory by remember(filteredCategories) { mutableStateOf(filteredCategories.firstOrNull()) }
    var selectedBudget by remember(budgets) { mutableStateOf(budgets.firstOrNull()) }

    // Error State
    var isAmountError by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }

    // --- Date/Time Pickers ---
    MonkeysDatePicker(
        show = showDatePicker,
        initialDate = transactionDate,
        onDismiss = { showDatePicker = false },
        onDateSelected = { selectedMillis ->
            showDatePicker = false
            if (selectedMillis != null) {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = selectedMillis }
                val currentCal = java.util.Calendar.getInstance().apply { time = transactionDate }
                currentCal.set(java.util.Calendar.YEAR, cal.get(java.util.Calendar.YEAR))
                currentCal.set(java.util.Calendar.MONTH, cal.get(java.util.Calendar.MONTH))
                currentCal.set(java.util.Calendar.DAY_OF_MONTH, cal.get(java.util.Calendar.DAY_OF_MONTH))
                transactionDate = currentCal.time
            }
        }
    )

    MonkeysTimePicker(
        show = showTimePicker,
        initialDate = transactionDate,
        onDismiss = { showTimePicker = false },
        onTimeSelected = { hour, minute ->
            showTimePicker = false
            val cal = java.util.Calendar.getInstance().apply { time = transactionDate }
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
            cal.set(java.util.Calendar.MINUTE, minute)
            transactionDate = cal.time
        }
    )

    // --- UI Layout ---
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // 2. Date & Time (Clickable Row)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = formatDateOnly(transactionDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showDatePicker = true }
                )
                Text(", ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = formatTimeOnly(transactionDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showTimePicker = true }
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // 3. Type Selector
            TransactionTypeSelector(
                selectedType = transactionType,
                onTypeSelected = { newType ->
                    transactionType = newType
                    // Reset category to first valid option for new type
                    selectedCategory = categories.firstOrNull { it.type == newType }
                }
            )

            Spacer(Modifier.height(24.dp))

            // 4. Amount Input (Custom Transparent Style)
            TransparentTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    isAmountError = false
                },
                label = "Amount",
                prefix = "Rp.",
                keyboardType = KeyboardType.Number,
                isError = isAmountError
            )
            if (isAmountError) {
                Text("Amount must be greater than 0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            // 5. Category Select
            Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            if (filteredCategories.isNotEmpty()) {
                ManualDropdown(
                    label = selectedCategory?.name ?: "Select Category",
                    items = filteredCategories,
                    onItemSelected = { selectedCategory = it },
                    itemLabel = { it.name }
                )
            } else {
                Text("No categories available for $transactionType", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            // 6. Name Input
            TransparentTextField(
                value = name,
                onValueChange = {
                    name = it
                    isNameError = false
                },
                label = "Name",
                placeholder = "",
                isError = isNameError
            )
            if (isNameError) {
                Text("Name cannot be empty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            // 7. Budget Select (Only for Expense)
            if (transactionType == TransactionType.EXPENSE) {
                Text("Budget", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Spacer(Modifier.height(8.dp))
                if (budgets.isNotEmpty()) {
                    ManualDropdown(
                        label = selectedBudget?.name ?: "Select Budget",
                        items = budgets,
                        onItemSelected = { selectedBudget = it },
                        itemLabel = { it.name }
                    )
                } else {
                    Text("No budgets found", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            // 8. Save Button
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    val nameValid = name.isNotBlank()
                    val amountValid = amountVal > 0.0

                    if (nameValid && amountValid && selectedCategory != null) {
                        onSave(
                            transactionDate,
                            amountVal,
                            name,
                            selectedCategory!!.id,
                            selectedBudget?.id,
                            transactionType
                        )
                    } else {
                        isNameError = !nameValid
                        isAmountError = !amountValid
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(150.dp)
                    .height(45.dp)
                    .shadow(4.dp, RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

// --- Reusable Components ---

@Composable
fun TransparentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    prefix: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (prefix.isNotEmpty()) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = if (isError) MaterialTheme.colorScheme.error else Color.Gray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun <T> ManualDropdown(
    label: String,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(25.dp))
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.secondary) // Matches the Yellow in your theme
            .clickable { expanded = true }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center // Center content like in the screenshot
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondary
            )
            // Optional: You can remove the icon if the screenshot doesn't have it,
            // but usually dropdowns implies an arrow.
             Spacer(Modifier.width(8.dp))
             Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSecondary)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = itemLabel(item), color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- Preview ---
@Preview(showBackground = true, heightDp = 900)
@Composable
fun ManualTransactionScreenPreview() {
    val dummyCategories = listOf(
        Categories(1, "Food", null, "Food desc", TransactionType.EXPENSE),
        Categories(2, "Salary", null, "Work", TransactionType.INCOME)
    )
    val dummyBudgets = listOf(
        Budgets(1, "Weekly Food", 0.0, 1000.0, Date(), null, null)
    )

    MonkeyslimitTheme {
        ManualTransactionContent(
            budgets = dummyBudgets,
            categories = dummyCategories,
            onNavigateBack = {},
            onSave = { _, _, _, _, _, _ -> }
        )
    }
}