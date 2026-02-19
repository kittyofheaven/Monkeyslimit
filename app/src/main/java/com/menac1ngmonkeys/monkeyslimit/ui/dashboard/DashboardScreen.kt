package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

import AppViewModelProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.components.BalanceExpenseCard
import com.menac1ngmonkeys.monkeyslimit.ui.components.MainContentContainer
import com.menac1ngmonkeys.monkeyslimit.ui.components.MonkeysDatePicker
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardFilter
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardNotification
import com.menac1ngmonkeys.monkeyslimit.ui.state.DashboardUiState
import com.menac1ngmonkeys.monkeyslimit.ui.theme.lighten
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        },
        onDateSelected = { dateMillis ->
            // TODO: If you update your DashboardViewModel to filter by date, call it here!
             dashboardViewModel.updateDate(dateMillis)
        }
    )
}

@Composable
fun DashboardScreenContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState,
    dashboardUiState: DashboardUiState,
    onFilterSelected: (DashboardFilter) -> Unit = {},
    onTransactionClick: (Int) -> Unit = {},
    onDateSelected: (Long) -> Unit = {}
) {
    val totalExpense = appUiState.totalExpense
    val totalIncome = appUiState.totalIncome

    // 1. Lock the dismissal state for the entire app session
    var isNotificationDismissed by rememberSaveable { mutableStateOf(false) }

    // 2. Cache the FIRST valid notification so it doesn't change when filters are clicked
    var cachedNotification by remember { mutableStateOf<DashboardNotification?>(null) }

    // --- DATE PICKER & SWITCHER STATES ---
    var selectedDateMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isToday = remember(selectedDateMillis) {
        val calSelected = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val calToday = Calendar.getInstance()
        calSelected.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                calSelected.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
    }

    val dateLabel = remember(selectedDateMillis, isToday) {
        if (isToday) SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(selectedDateMillis)) + " - Today"
        else SimpleDateFormat("EEEE - dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    LaunchedEffect(dashboardUiState.notification) {
        if (cachedNotification == null && dashboardUiState.notification !is DashboardNotification.None) {
            cachedNotification = dashboardUiState.notification
        }
    }

    val notif = cachedNotification ?: DashboardNotification.None

    val (bgColor, iconColor, iconId, title, message) = when (notif) {
        is DashboardNotification.Alert -> NotificationStyle(
            bgColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.tertiary,
            iconId = R.drawable.negative_2,
            title = notif.title,
            message = notif.message
        )
        is DashboardNotification.Warning -> NotificationStyle(
            bgColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.tertiary,
            iconId = R.drawable.negative_1,
            title = notif.title,
            message = notif.message
        )
        is DashboardNotification.Achievement -> NotificationStyle(
            bgColor = MaterialTheme.colorScheme.primaryContainer.lighten(0.2f),
            iconColor = MaterialTheme.colorScheme.primary,
            iconId = R.drawable.positive_4,
            title = notif.title,
            message = notif.message
        )
        else -> NotificationStyle(Color.White, Color.Gray, 0, "", "")
    }

    val isNotificationVisible = notif !is DashboardNotification.None && !isNotificationDismissed

    // Use MonkeysDatePicker
    MonkeysDatePicker(
        show = showDatePicker,
        initialDate = Date(selectedDateMillis),
        onDismiss = { showDatePicker = false },
        onDateSelected = { dateMillis ->
            showDatePicker = false
            if (dateMillis != null) {
                selectedDateMillis = dateMillis
                onDateSelected(dateMillis)
            }
        },
        disableFutureDates = true,
    )

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
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = isNotificationVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        NotificationCard(
                            backgroundColor = bgColor,
                            iconColor = iconColor,
                            imageId = iconId,
                            title = title,
                            message = message,
                            onClose = { isNotificationDismissed = true }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                // --- NEW VERTICAL HEADER LAYOUT ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Date Switcher Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                            selectedDateMillis = cal.timeInMillis
                            onDateSelected(selectedDateMillis)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Wrap the Text in a Box that takes up all the remaining middle space
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showDatePicker = true }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (!isToday) {
                                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                                    cal.add(Calendar.DAY_OF_YEAR, 1)
                                    selectedDateMillis = cal.timeInMillis
                                    onDateSelected(selectedDateMillis)
                                }
                            },
                            enabled = !isToday
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Date",
                                // Hides the arrow seamlessly when it's today
                                tint = if (isToday) Color.Transparent else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Filters Row
                    DashboardFilterRow(
                        currentFilter = dashboardUiState.currentFilter,
                        onFilterSelected = onFilterSelected,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    )
                }

                Spacer(Modifier.size(8.dp))

                if (dashboardUiState.recentTransactions.isEmpty()) {
                    val emptyMessage = when (dashboardUiState.currentFilter) {
                        DashboardFilter.INCOME -> "No income yet."
                        DashboardFilter.EXPENSE -> "No expenses yet."
                        else -> "No transactions yet."
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
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
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(dashboardUiState.recentTransactions) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                onClick = onTransactionClick,
                                subtitle = transaction.subtitle.substringBefore(" ")
                            )
                        }
                    }
                }
            }
        }
    }
}

data class NotificationStyle(
    val bgColor: Color,
    val iconColor: Color,
    val iconId: Int,
    val title: String,
    val message: String
)

@Composable
fun NotificationCard(
    backgroundColor: Color,
    iconColor: Color,
    imageId: Int,
    title: String,
    message: String,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .border(1.dp, color = backgroundColor, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = null,
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

// Added horizontalArrangement parameter to allow centering the chips
@Composable
fun DashboardFilterRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    currentFilter: DashboardFilter,
    onFilterSelected: (DashboardFilter) -> Unit
) {
    Row(
        horizontalArrangement = horizontalArrangement,
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