package com.menac1ngmonkeys.monkeyslimit.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.menac1ngmonkeys.monkeyslimit.ui.components.MainContentContainer
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionRow
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetDetailUiState
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetDetailViewModel

@Composable
fun BudgetDetailWithHeader(
    viewModel: BudgetDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- Edit Dialog ---
    if (showEditDialog && uiState.budget != null) {
        EditBudgetDialog(
            currentName = uiState.budget!!.name,
            currentLimit = uiState.budget!!.limitAmount,
            otherBudgetNames = uiState.otherBudgetNames, // Pass existing names
            onDismiss = { showEditDialog = false },
            onConfirm = { name, limit ->
                viewModel.updateBudget(name, limit)
                showEditDialog = false
            }
        )
    }

    // --- Delete Dialog ---
    if (showDeleteDialog) {
        DeleteBudgetDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteBudget(onSuccess = onNavigateBack)
                showDeleteDialog = false
            }
        )
    }

    MainContentContainer(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Do nothing */ }
    ) {
        Column {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(Modifier.width(8.dp))
                val displayName = uiState.budget?.name?.length?.let {
                    if (it > 15) {
                        "${uiState.budget?.name?.take(12)}..."
                    } else {
                        uiState.budget?.name
                    }
                }

                Text(
                    text = displayName ?: "Budget Detail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.weight(1f)) // Push icons to the right

                // Edit Button
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Delete Button
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Budget",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            BudgetDetailScreenContent(
                uiState = uiState,
                onTransactionClick = { transactionId ->
                    navController.navigate("transaction_detail/$transactionId")
                }
            )
        }
    }
}

@Composable
fun BudgetDetailScreenContent(
    uiState: BudgetDetailUiState,
    modifier: Modifier = Modifier,
    onTransactionClick: (Int) -> Unit = {},
) {
    if (uiState.budget == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Budget Info
            item {
                BudgetRow(
                    budgetItem = uiState.budget,
                    onClick = {  }
                )
            }

            // Sub-header
            if (uiState.relatedTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Related Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Transaction List
            items(uiState.relatedTransactions) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) }
                )
            }
        }
    }
}

@Composable
fun EditBudgetDialog(
    currentName: String,
    currentLimit: Double,
    otherBudgetNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var limitStr by remember { mutableStateOf(currentLimit.toLong().toString()) }

    var isNameError by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf("") }
    var isLimitError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Budget",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isNameError = false
                    },
                    label = { Text("Budget Name") },
                    singleLine = true,
                    isError = isNameError,
                    supportingText = if (isNameError) {
                        { Text(nameErrorMessage) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Limit Field
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            limitStr = it
                            isLimitError = false
                        }
                    },
                    label = { Text("Limit Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isLimitError,
                    supportingText = if (isLimitError) {
                        { Text("Must be > 0", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val finalLimit = limitStr.toDoubleOrNull() ?: 0.0

                    // VALIDATION LOGIC
                    val limitValid = finalLimit > 0.0
                    val nameNotEmpty = cleanName.isNotBlank()

                    // Check for duplicates (case insensitive), excluding self (handled by VM filtering)
                    val nameIsDuplicate = otherBudgetNames.any { it.equals(cleanName, ignoreCase = true) }

                    if (nameNotEmpty && !nameIsDuplicate && limitValid) {
                        onConfirm(cleanName, finalLimit)
                    } else {
                        // Set Errors
                        isLimitError = !limitValid

                        if (!nameNotEmpty) {
                            isNameError = true
                            nameErrorMessage = "Name cannot be empty"
                        } else if (nameIsDuplicate) {
                            isNameError = true
                            nameErrorMessage = "Budget already exists"
                        }
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
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun DeleteBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Budget?") },
        text = { Text("Are you sure you want to delete this budget? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}