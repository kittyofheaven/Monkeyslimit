package com.menac1ngmonkeys.monkeyslimit.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionRow
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetItemUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetDetailUiState
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetDetailViewModel

/**
 * Smart composable that handles ViewModel creation for the budget detail screen.
 */
@Composable
fun BudgetDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    BudgetDetailScreenContent(
        uiState = uiState,
        modifier = modifier
    )
}

/**
 * Stateless composable that displays the UI for the budget detail screen.
 */
@Composable
fun BudgetDetailScreenContent(
    uiState: BudgetDetailUiState,
    modifier: Modifier = Modifier
) {
    // Show a loading indicator while data is being fetched for the first time
    if (uiState.budget == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header item: The BudgetRow for the main budget
            item {
                // ✅ FIX: Provide the empty onClick lambda
                BudgetRow(
                    budgetItem = uiState.budget,
                    onClick = {} // This row doesn't navigate anywhere, so the action is empty
                )
            }

            // Sub-header for the transaction list
            if (uiState.relatedTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Related Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // List of associated transactions
            items(uiState.relatedTransactions) { transaction ->
                TransactionRow(transaction = transaction)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BudgetDetailScreenPreview() {
    val previewState = BudgetDetailUiState(
        budget = BudgetItemUiState(1, "Needs - Foods and...", 1028700.0, 1200000.0, 0.85f),
        relatedTransactions = listOf(
            // You can add a couple of fake TransactionItemData objects here for a better preview
        )
    )
    MonkeyslimitTheme {
        BudgetDetailScreenContent(uiState = previewState)
    }
}
