package com.menac1ngmonkeys.monkeyslimit.ui.budget

import AppViewModelProvider
import BudgetDetailViewModelFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.menac1ngmonkeys.monkeyslimit.ui.components.BalanceExpenseCard
import com.menac1ngmonkeys.monkeyslimit.ui.components.MainContentContainer
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetItemUiState
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetDetailViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetViewModel

/**
 * The "smart" stateful container for the Budget screen.
 * It manages the animation between the main budget list and the detail view.
 */
@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory),
    budgetViewModel: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val appUiState by appViewModel.appUiState.collectAsState()
    val budgetListState by budgetViewModel.uiState.collectAsState()

    // Animation state for the detail view
    var screenState by remember { mutableStateOf<BudgetScreenState>(BudgetScreenState.List) }

    // We use a Box to allow composables to be layered on top of each other with zIndex
    Box(
        modifier = modifier
            .fillMaxSize()
            // ✅ FIX 1: Force the background to match the TopBar color
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- This is the static background content ---
        BudgetListScreenContent(
            appUiState = appUiState,
            budgetItems = budgetListState.budgetItems,
            onBudgetClick = { budgetId ->
                screenState = BudgetScreenState.Detail(budgetId)
            },
            onAddBudgetClick = {
                navController.navigate("add_budget")
            }
        )

        // Animated Detail View
        AnimatedContent(
            targetState = screenState,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (targetState is BudgetScreenState.Detail) {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "BudgetListDetailTransition"
        ) { state ->
            when (state) {
                is BudgetScreenState.List -> {
                    Box(Modifier.fillMaxSize()) { /* Empty placeholder */ }
                }
                is BudgetScreenState.Detail -> {
                    BudgetDetailWrapper(
                        budgetId = state.budgetId,
                        onNavigateBack = {
                            screenState = BudgetScreenState.List
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetDetailWrapper(
    budgetId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetDetailViewModel = viewModel(
        key = budgetId.toString(),
        factory = BudgetDetailViewModelFactory(budgetId)
    )
) {
    BudgetDetailWithHeader(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun BudgetListScreenContent(
    appUiState: AppUiState,
    budgetItems: List<BudgetItemUiState>,
    onBudgetClick: (Int) -> Unit,
    onAddBudgetClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        MainContentContainer(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AddBudgetCard(onClick = onAddBudgetClick)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(budgetItems) { budgetItem ->
                        BudgetRow(
                            budgetItem = budgetItem,
                            onClick = { onBudgetClick(budgetItem.id) }
                        )
                    }

                    item { Spacer(Modifier.size(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BudgetDetailWithHeader(
    viewModel: BudgetDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    MainContentContainer(
        modifier = modifier
            .fillMaxSize()
            // ✅ FIX 2: Ensure the detail view also matches the TopBar color
            .background(MaterialTheme.colorScheme.background)
            // ✅ FIX 3: Capture clicks so they don't fall through to the list
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Do nothing */ }
    ) {
        Column {
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
                Text(
                    text = uiState.budget?.name ?: "Budget Detail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            BudgetDetailScreenContent(uiState = uiState)
        }
    }
}

@Composable
private fun AddBudgetCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Budget",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Add a new budget",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    MonkeyslimitTheme {
        val previewAppUiState = AppUiState(totalBalance = 25000000.0, totalExpense = 12500000.0)
        val previewBudgets = listOf(
            BudgetItemUiState(1, "Cigarettes", 150000.0, 150000.0, 1.0f),
            BudgetItemUiState(2, "Investing", 900000.0, 900000.0, 1.0f)
        )
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)) {
            BalanceExpenseCard(
                totalBalance = previewAppUiState.totalBalance,
                totalExpense = previewAppUiState.totalExpense
            )
            Spacer(Modifier.size(15.dp))
            MainContentContainer(modifier = Modifier.weight(1f)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(previewBudgets) { BudgetRow(budgetItem = it, onClick = {}) }
                }
            }
        }
    }
}

private sealed class BudgetScreenState {
    data object List : BudgetScreenState()
    data class Detail(val budgetId: Int) : BudgetScreenState()
}