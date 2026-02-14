package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

import AppViewModelProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.menac1ngmonkeys.monkeyslimit.ui.components.BalanceExpenseCard
import com.menac1ngmonkeys.monkeyslimit.ui.components.MainContentContainer
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardFilter
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardNotification
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardUiState
import com.menac1ngmonkeys.monkeyslimit.ui.theme.lighten
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    dashboardViewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
    appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val dashboardUiState by dashboardViewModel.dashboardUiState.collectAsState()
    val appUiState by appViewModel.appUiState.collectAsState()

    DashboardScreenContent(
        modifier = modifier,
        appUiState = appUiState,
        dashboardUiState = dashboardUiState,
        onFilterSelected = dashboardViewModel::updateFilter,
        onTransactionClick = { transactionId ->
            navController.navigate("transaction_detail/$transactionId")
        }
    )
}

@Composable
fun DashboardScreenContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState,
    dashboardUiState: DashboardUiState,
    onFilterSelected: (DashboardFilter) -> Unit = {},
    onTransactionClick: (Int) -> Unit = {}
) {
    val totalExpense = appUiState.totalExpense
    val totalIncome = appUiState.totalIncome

    // 1. Lock the dismissal state for the entire app session
    var isNotificationDismissed by rememberSaveable { mutableStateOf(false) }

    // 2. Cache the FIRST valid notification so it doesn't change when filters are clicked
    var cachedNotification by remember { mutableStateOf<DashboardNotification?>(null) }

    LaunchedEffect(dashboardUiState.notification) {
        // Only save it if we haven't saved one yet AND it's a real notification
        if (cachedNotification == null && dashboardUiState.notification !is DashboardNotification.None) {
            cachedNotification = dashboardUiState.notification
        }
    }

    // 3. Extract details using the CACHED notification (not the actively changing state)
    val notif = cachedNotification ?: DashboardNotification.None

    val (bgColor, iconColor, icon, title, message) = when (notif) {
        is DashboardNotification.Alert -> NotificationStyle(
            bgColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.tertiary,
            icon = Icons.Default.Warning,
            title = notif.title,
            message = notif.message
        )
        is DashboardNotification.Warning -> NotificationStyle(
            bgColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.tertiary,
            icon = Icons.Default.Warning,
            title = notif.title,
            message = notif.message
        )
        is DashboardNotification.Achievement -> NotificationStyle(
            bgColor = MaterialTheme.colorScheme.primaryContainer.lighten(0.2f),
            iconColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.EmojiEvents,
            title = notif.title,
            message = notif.message
        )
        else -> NotificationStyle(Color.White, Color.Gray, Icons.Default.Done, "", "")
    }

    // 4. It's visible ONLY if it's not "None" AND the user hasn't dismissed it
    val isNotificationVisible = notif !is DashboardNotification.None && !isNotificationDismissed

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        BalanceExpenseCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            totalIncome = totalIncome,
            totalExpense = totalExpense,
        )
        Spacer(Modifier.size(20.dp))

        MainContentContainer(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- DYNAMIC NOTIFICATION CARD ---
                AnimatedVisibility(
                    visible = isNotificationVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        NotificationCard(
                            backgroundColor = bgColor,
                            iconColor = iconColor,
                            icon = icon,
                            title = title,
                            message = message,
                            onClose = {
                                // Permanently dismiss for this session
                                isNotificationDismissed = true
                            }
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }
                // -----------------------------

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dashboardUiState.currentMonth,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    DashboardFilterRow(
                        currentFilter = dashboardUiState.currentFilter,
                        onFilterSelected = onFilterSelected
                    )
                }

                Spacer(Modifier.size(8.dp))

                if (dashboardUiState.recentTransactions.isEmpty()) {
                    // Determine the specific message based on the active filter
                    val emptyMessage = when (dashboardUiState.currentFilter) {
                        DashboardFilter.INCOME -> "No income yet."
                        DashboardFilter.EXPENSE -> "No expenses yet."
                        else -> "No transactions yet."
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Take up remaining space
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(dashboardUiState.recentTransactions) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                onClick = onTransactionClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper data class for style extraction
data class NotificationStyle(
    val bgColor: Color,
    val iconColor: Color,
    val icon: ImageVector,
    val title: String,
    val message: String
)

@Composable
fun NotificationCard(
    backgroundColor: Color,
    iconColor: Color,
    icon: ImageVector,
    title: String,
    message: String,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onClose() },
                        tint = Color.Gray
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun DashboardFilterRow(
    modifier: Modifier = Modifier,
    currentFilter: DashboardFilter,
    onFilterSelected: (DashboardFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        FilterChipItem(
            text = "All",
            selected = currentFilter == DashboardFilter.ALL,
            onClick = { onFilterSelected(DashboardFilter.ALL) }
        )
        FilterChipItem(
            text = "Income",
            selected = currentFilter == DashboardFilter.INCOME,
            onClick = { onFilterSelected(DashboardFilter.INCOME) }
        )
        FilterChipItem(
            text = "Expense",
            selected = currentFilter == DashboardFilter.EXPENSE,
            onClick = { onFilterSelected(DashboardFilter.EXPENSE) }
        )
    }
}

@Composable
fun FilterChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}