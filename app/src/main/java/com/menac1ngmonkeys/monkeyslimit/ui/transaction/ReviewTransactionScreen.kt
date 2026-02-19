package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeyLoadingScreen
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeysDatePicker
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeysTimePicker
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.utils.CurrencyVisualTransformation
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ReviewItemUi
import com.menac1ngmonkeys.monkeyslimit.viewmodel.ReviewTransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReviewTransactionScreen(
    viewModel: ReviewTransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ReviewTransactionScreenContent(
        budgets = state.budgets,
        categories = state.categories,
        imageUri = state.imageUri,
        // PERBAIKAN 1: Oper data hasil scan ke konten UI
        detectedDate = state.detectedDate,
        detectedItems = state.detectedItems,
        isLoading = state.isLoading,
        onSave = { date, items, type ->
            viewModel.saveTransaction(
                context = context,
                date = date,
                reviewItems = items,
                type = type,
                onSuccess = onNavigateBack
            )
        },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun ReviewTransactionScreenContent(
    budgets: List<Budgets>,
    categories: List<Categories>,
    imageUri: String?,
    // PERBAIKAN 2: Tambahkan parameter penerima
    detectedDate: Date? = null,
    detectedItems: List<ReviewItemUi> = emptyList(),
    isLoading: Boolean,
    onSave: (Date, List<ReviewItemUi>, TransactionType) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    // Inisialisasi awal. Jika detectedDate sudah ada (sangat cepat), pakai itu. Jika tidak, pakai Date()
    var transactionDate by remember { mutableStateOf(detectedDate ?: Date()) }

    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val filteredCategories = remember(categories, transactionType) {
        categories.filter { it.type == transactionType }
    }

    var itemToEdit by remember { mutableStateOf<ReviewItemUi?>(null) }
    var itemToDelete by remember { mutableStateOf<ReviewItemUi?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isImageExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var globalSelectedBudget by remember(budgets) { mutableStateOf(budgets.firstOrNull()) }

    // This state keeps the overlay alive long enough to finish its fade-out animation
    var showLoadingScreen by remember { mutableStateOf(isLoading) }

    LaunchedEffect(isLoading) {
        if (isLoading) showLoadingScreen = true
    }

    // PERBAIKAN 3: Inisialisasi items dengan logika cerdas
    var items by remember(categories, budgets) {
        mutableStateOf(
            if (categories.isNotEmpty() && budgets.isNotEmpty()) {
                val defaultCatId = categories.first().id
                val defaultBudgetId = budgets.first().id

                if (detectedItems.isEmpty()) {
//                    listOf(
//                        ReviewItemUi(1, "Fish n Chips", defaultCatId, defaultBudgetId, 1, 55000.0),
//                        ReviewItemUi(2, "Ice Tea", defaultCatId, defaultBudgetId, 1, 15000.0)
//                    )
                    emptyList<ReviewItemUi>()
                } else {
                    // FIX: Preserve the AI Category ID if it found one (> 0), otherwise use Default
                    detectedItems.map {
                        it.copy(
                            categoryId = if (it.categoryId != 0) it.categoryId else defaultCatId,
                            budgetId = defaultBudgetId
                        )
                    }
                }
            } else emptyList()
        )
    }

    // PERBAIKAN 4: LaunchedEffect untuk Auto-Update TANGGAL
    // Ini kuncinya! Ketika OCR selesai memproses di background, detectedDate berubah.
    // Kode ini akan memaksa UI mengupdate transactionDate.
    LaunchedEffect(detectedDate) {
        if (detectedDate != null) {
            transactionDate = detectedDate
        }
    }

    // PERBAIKAN 5: LaunchedEffect untuk Auto-Update ITEMS (Total Harga)
    LaunchedEffect(detectedItems) {
        if (detectedItems.isNotEmpty() && categories.isNotEmpty() && budgets.isNotEmpty()) {
            val defaultCatId = categories.first().id
            val defaultBudgetId = budgets.first().id
            items = detectedItems.map {
                // FIX: Preserve the AI Category ID if it found one (> 0), otherwise use Default
                it.copy(
                    categoryId = if (it.categoryId != 0) it.categoryId else defaultCatId,
                    budgetId = defaultBudgetId
                )
            }
        }
    }

    val subtotal = items.sumOf { it.pricePerUnit * it.quantity }
    val totalAmount = subtotal

    // --- DIALOGS ---
    if (isImageExpanded && imageUri != null) {
        FullScreenImageDialog(imageUri) { isImageExpanded = false }
    }

    if (itemToEdit != null) {
        TransactionItemDialog(
            title = "Edit Item",
            initialName = itemToEdit!!.name,
            initialPrice = itemToEdit!!.pricePerUnit,
            initialQty = itemToEdit!!.quantity,
            onDismiss = { itemToEdit = null },
            onConfirm = { name, price, qty ->
                items = items.map {
                    if (it.id == itemToEdit!!.id) it.copy(name = name, pricePerUnit = price, quantity = qty) else it
                }
                itemToEdit = null
            }
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Delete Item",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${itemToDelete?.name}'? This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Remove the item from the list
                        items = items.filter { it.id != itemToDelete?.id }
                        itemToDelete = null // Close the dialog
                    },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showAddDialog) {
        TransactionItemDialog(
            title = "Add Item",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, qty ->
                val defaultCatId = filteredCategories.firstOrNull()?.id ?: 0
                val defaultBudgetId = globalSelectedBudget?.id ?: budgets.firstOrNull()?.id ?: 0
                val newId = (items.maxOfOrNull { it.id } ?: 0) + 1
                items = items + ReviewItemUi(newId, name, defaultCatId, defaultBudgetId, qty, price)
                showAddDialog = false
            }
        )
    }

    MonkeysDatePicker(
        show = showDatePicker,
        initialDate = transactionDate,
        onDismiss = { showDatePicker = false },
        onDateSelected = { selectedMillis ->
            showDatePicker = false
            if (selectedMillis != null) {
                val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                val currentCal = Calendar.getInstance().apply { time = transactionDate }
                currentCal.set(Calendar.YEAR, cal.get(Calendar.YEAR))
                currentCal.set(Calendar.MONTH, cal.get(Calendar.MONTH))
                currentCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
                transactionDate = currentCal.time
            }
        },
        proceedText = "Next"
    )

    MonkeysTimePicker(
        show = showTimePicker,
        initialDate = transactionDate,
        onDismiss = { showTimePicker = false },
        onTimeSelected = { hour, minute ->
            showTimePicker = false
            val cal = Calendar.getInstance().apply { time = transactionDate }
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            transactionDate = cal.time
        }
    )

    // --- MAIN CONTENT ---
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // Root Box allows the Loading Screen to float on top of the UI
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // 1. IMAGE PREVIEW
                if (imageUri != null) {
                    ReceiptImagePreview(imageUri) { isImageExpanded = true }
                    Spacer(Modifier.height(16.dp))
                }

                Text(
                    text = "Make sure to check all items were read correctly",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 2. TYPE SWITCHER
                TransactionTypeSelector(
                    selectedType = transactionType,
                    onTypeSelected = { newType ->
                        transactionType = newType
                        val defaultCategory = categories.firstOrNull { it.type == newType }
                        if (defaultCategory != null) {
                            items = items.map { it.copy(categoryId = defaultCategory.id) }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // 3. DATE & TIME
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
                }

                // 4. ITEMS LIST
                items.forEachIndexed { index, item ->
                    val currentCategory = categories.find { it.id == item.categoryId }
                    val currentBudget = budgets.find { it.id == item.budgetId }

                    ReviewItemRow(
                        item = item,
                        currentCategory = currentCategory,
                        currentBudget = currentBudget,
                        allCategories = filteredCategories,
                        allBudgets = budgets,
                        showBudget = transactionType == TransactionType.EXPENSE,
                        onEdit = { itemToEdit = item },
                        onDelete = { itemToDelete = item },
                        onCategorySelected = { newCat ->
                            items = items.toMutableList().apply { this[index] = item.copy(categoryId = newCat.id) }
                        },
                        onBudgetSelected = { newBudget ->
                            items = items.toMutableList().apply { this[index] = item.copy(budgetId = newBudget.id) }
                        }
                    )

                    if (index < items.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 5. ADD ITEM
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showAddDialog = true }.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.AddCircleOutline, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 16.dp))

                // 6. SUMMARY
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(formatCurrency(totalAmount), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }

                Spacer(Modifier.height(24.dp))

                // 8. SAVE BUTTON
                Button(
                    onClick = { if (items.isNotEmpty()) onSave(transactionDate, items, transactionType) },
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(150.dp).height(45.dp).shadow(4.dp, RoundedCornerShape(25.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(25.dp),
                ) {
                    Text("Save", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // THE LOADING OVERLAY (Drawn last = floats on top)
        if (showLoadingScreen) {
            MonkeyLoadingScreen(
                loadingText = "Analyzing...",
                monkeyImageRes = R.drawable.positive_3,
                isFinished = !isLoading, // Tells the bar to sprint to 100% and fade
                onDismiss = { showLoadingScreen = false } // Actually kills the view after fade
            )
        }
    }
}

// --- COMPONENTS ---

@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) // Lighter grey background
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TypeButton(
            text = "Expense",
            isSelected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f)
        )
        TypeButton(
            text = "Income",
            isSelected = selectedType == TransactionType.INCOME,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Matching styles: Selected = Primary (Green), Unselected = Transparent
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shadow = if (isSelected) 2.dp else 0.dp

    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(shadow, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun ReviewItemRow(
    item: ReviewItemUi,
    currentCategory: Categories?,
    currentBudget: Budgets?,
    allCategories: List<Categories>,
    allBudgets: List<Budgets>,
    showBudget: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCategorySelected: (Categories) -> Unit,
    onBudgetSelected: (Budgets) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryDropdown(currentCategory, allCategories, onCategorySelected)
                if (showBudget) {
                    MiniBudgetDropdown(currentBudget, allBudgets, onBudgetSelected)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("x${item.quantity}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatCurrency(item.pricePerUnit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun ReceiptImagePreview(imageUri: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Receipt",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun FullScreenImageDialog(imageUri: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Full Receipt",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun TransactionItemDialog(
    title: String,
    initialName: String = "",
    initialPrice: Double = 0.0,
    initialQty: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, qty: Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var priceStr by remember { mutableStateOf(if (initialPrice > 0) initialPrice.toString() else "") }
    var qtyStr by remember { mutableStateOf(if (initialQty > 0) initialQty.toString() else "") }

    var isNameError by remember { mutableStateOf(false) }
    var isPriceError by remember { mutableStateOf(false) }
    var isQtyError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isNameError = false },
                    label = { Text("Item Name") },
                    singleLine = true,
                    isError = isNameError,
                    supportingText = if (isNameError) { { Text("Name cannot be empty") } } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { newValue ->
                        val cleanText = newValue.replace(Regex("[^0-9.,]"), "").replace('.', ',')
                        val parts = cleanText.split(',')
                        priceStr = if (parts.size > 1) "${parts[0]},${parts[1]}" else parts[0]

                        isPriceError = false
                    },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isPriceError,
                    visualTransformation = CurrencyVisualTransformation(),
                    supportingText = if (isPriceError) { { Text("Must be > 0", color = MaterialTheme.colorScheme.error) } } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it; isQtyError = false },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isQtyError,
                    supportingText = if (isQtyError) { { Text("Must be > 0", color = MaterialTheme.colorScheme.error) } } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPrice = priceStr.toDoubleOrNull() ?: 0.0
                    val finalQty = qtyStr.toIntOrNull() ?: 0
                    val nameValid = name.isNotBlank()
                    val priceValid = finalPrice > 0.0
                    val qtyValid = finalQty > 0

                    if (nameValid && priceValid && qtyValid) {
                        onConfirm(name, finalPrice, finalQty)
                    } else {
                        isNameError = !nameValid
                        isPriceError = !priceValid
                        isQtyError = !qtyValid
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surface // Standard dialog background
    )
}

// --- DROPDOWNS & PILLS ---

@Composable
fun MiniBudgetDropdown(selectedBudget: Budgets?, budgets: List<Budgets>, onBudgetSelected: (Budgets) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PillButton(
            text = selectedBudget?.name ?: "Budget",
            color = MaterialTheme.colorScheme.primaryContainer, // Use Yellow (Primary Container)
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            height = 28.dp, textSize = 12.sp, onClick = { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            budgets.forEach { budget ->
                DropdownMenuItem(text = { Text(budget.name) }, onClick = { onBudgetSelected(budget); expanded = false })
            }
        }
    }
}

@Composable
fun CategoryDropdown(selectedCategory: Categories?, categories: List<Categories>, onCategorySelected: (Categories) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PillButton(
            text = selectedCategory?.name ?: "Select",
            color = MaterialTheme.colorScheme.secondary, // Use Yellow (Secondary)
            textColor = MaterialTheme.colorScheme.onSecondary,
            height = 28.dp, textSize = 12.sp, onClick = { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(text = { Text(category.name) }, onClick = { onCategorySelected(category); expanded = false })
            }
        }
    }
}

@Composable
fun BudgetDropdown(selectedBudget: Budgets?, budgets: List<Budgets>, onBudgetSelected: (Budgets) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(25.dp))
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.secondary) // Use Yellow (Secondary)
            .clickable { expanded = true }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = selectedBudget?.name ?: "Select Budget", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
            }
            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSecondary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            budgets.forEach { budget ->
                DropdownMenuItem(text = { Text(budget.name) }, onClick = { onBudgetSelected(budget); expanded = false })
            }
        }
    }
}

@Composable
fun PillButton(text: String, color: Color, textColor: Color, modifier: Modifier = Modifier, height: Dp = 32.dp, textSize: TextUnit = 14.sp, onClick: () -> Unit) {
    Box(modifier = modifier.height(height).shadow(2.dp, CircleShape).clip(CircleShape).background(color).clickable { onClick() }.padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = text, color = textColor, fontSize = textSize, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, null, tint = textColor, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun PreviewReviewScreen() {
    MonkeyslimitTheme {
        val dummyCategories = listOf(Categories(1, "Food", null, null))
        val dummyBudgets = listOf(Budgets(1, "Weekly", 0.0, 1000.0, Date(), null, null))
        ReviewTransactionScreenContent(
            budgets = dummyBudgets,
            categories = dummyCategories,
            imageUri = null,
            detectedDate = null, // <--- Add null
            detectedItems = emptyList(), // <--- Add emptyList
            isLoading = false,
            onSave = { _, _, _ -> }
        )
    }
}