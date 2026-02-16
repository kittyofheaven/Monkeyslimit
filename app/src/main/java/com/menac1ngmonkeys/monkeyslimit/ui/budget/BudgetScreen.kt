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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.menac1ngmonkeys.monkeyslimit.ui.components.MainContentContainer
import com.menac1ngmonkeys.monkeyslimit.ui.navigation.NavItem
import com.menac1ngmonkeys.monkeyslimit.ui.state.BudgetUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.SortDirection
import com.menac1ngmonkeys.monkeyslimit.ui.state.SortType
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetDetailViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory),
    budgetViewModel: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val budgetListState by budgetViewModel.uiState.collectAsState()

    // Animation state for the detail view
    var screenState by remember { mutableStateOf<BudgetScreenState>(BudgetScreenState.List) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BudgetListScreenContent(
            uiState = budgetListState,
            onMonthSelected = { budgetViewModel.updateMonth(it) },
            onYearChanged = { budgetViewModel.updateYear(it) },
            onSortChange = { type, direction -> budgetViewModel.updateSort(type, direction) }, // Pass sort event
            onBudgetClick = { budgetId ->
                screenState = BudgetScreenState.Detail(budgetId)
            },
            onAddBudgetClick = {
                navController.navigate("add_budget")
            },
            onAiRecommendationClick = {
                navController.navigate(NavItem.BudgetRecommendation.route)
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
                        // Pass current filter context so Detail matches the Selection
                        selectedMonth = budgetListState.selectedMonth,
                        selectedYear = budgetListState.selectedYear,
                        onNavigateBack = {
                            screenState = BudgetScreenState.List
                        },
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetDetailWrapper(
    budgetId: Int,
    selectedMonth: Int,
    selectedYear: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: BudgetDetailViewModel = viewModel(
        key = budgetId.toString(),
        factory = BudgetDetailViewModelFactory(budgetId)
    )
) {
    // Sync the detail view with the selected month/year
    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.setDateFilter(selectedMonth, selectedYear)
    }

    BudgetDetailWithHeader(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        navController = navController,
    )
}

@Composable
fun BudgetListScreenContent(
    modifier: Modifier = Modifier,
    uiState: BudgetUiState,
    onMonthSelected: (Int) -> Unit,
    onYearChanged: (Int) -> Unit,
    onSortChange: (SortType, SortDirection) -> Unit,
    onBudgetClick: (Int) -> Unit,
    onAddBudgetClick: () -> Unit = {},
    onAiRecommendationClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- FIXED HEADER SECTION ---

        // 1. Year Selector (Aligned Start)
        YearSelector(
            year = uiState.selectedYear,
            onYearChange = onYearChanged
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Month Selector (Optimized)
        MonthSlider(
            selectedMonthIndex = uiState.selectedMonth,
            onMonthSelected = onMonthSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Action Buttons
        ActionButtons(onAddBudgetClick, onAiRecommendationClick)

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Total Summary
        TotalBudgetSummary(uiState)

        Spacer(modifier = Modifier.height(12.dp))

        // 5. List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budgets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            // SORT BUTTON
            SortButton(
                currentSortType = uiState.sortType,
                currentSortDirection = uiState.sortDirection,
                onSortChange = onSortChange
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // --- SCROLLING LIST ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp)
        ) {
            items(uiState.budgetItems) { budgetItem ->
                BudgetRow(
                    budgetItem = budgetItem,
                    onClick = { onBudgetClick(budgetItem.id) }
                )
            }
        }
    }
}

@Composable
fun SortButton(
    currentSortType: SortType,
    currentSortDirection: SortDirection,
    onSortChange: (SortType, SortDirection) -> Unit
) {
    // 1. Determine the Label text based on current state
    val label = when (currentSortType) {
        SortType.NAME -> if (currentSortDirection == SortDirection.ASCENDING) "Name (A-Z)" else "Name (Z-A)"
        SortType.AMOUNT_LIMIT -> if (currentSortDirection == SortDirection.DESCENDING) "Amount (High-Low)" else "Amount (Low-High)"
        SortType.AMOUNT_USED -> if (currentSortDirection == SortDirection.DESCENDING) "Used (High-Low)" else "Used (Low-High)"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                // 2. Cycle Logic: Name(A) -> Name(Z) -> Amount(Desc) -> Amount(Asc) -> Used(Desc) -> Used(Asc) -> Name(A)
                val (nextType, nextDir) = when (currentSortType) {
                    SortType.NAME -> {
                        if (currentSortDirection == SortDirection.ASCENDING) SortType.NAME to SortDirection.DESCENDING
                        else SortType.AMOUNT_LIMIT to SortDirection.DESCENDING // Default to High-Low for Amount
                    }
                    SortType.AMOUNT_LIMIT -> {
                        if (currentSortDirection == SortDirection.DESCENDING) SortType.AMOUNT_LIMIT to SortDirection.ASCENDING
                        else SortType.AMOUNT_USED to SortDirection.DESCENDING // Default to High-Low for Used
                    }
                    SortType.AMOUNT_USED -> {
                        if (currentSortDirection == SortDirection.DESCENDING) SortType.AMOUNT_USED to SortDirection.ASCENDING
                        else SortType.NAME to SortDirection.ASCENDING // Reset loop
                    }
                }
                onSortChange(nextType, nextDir)
            }
            .padding(4.dp)
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
            contentDescription = "Sort",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun YearSelector(
    year: Int,
    onYearChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start, // Fixed: Aligned to Left/Start
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onYearChange(-1) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Year",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = year.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { onYearChange(1) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Year",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthSlider(
    selectedMonthIndex: Int, // 0 - 11
    onMonthSelected: (Int) -> Unit
) {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    // We start at a large index to simulate infinite scrolling.
    val baseIndex = 12000
    // Center the initial selected month in the viewport (offset by -2 items).
    val initialCenterIndex = baseIndex + selectedMonthIndex
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialCenterIndex - 2)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    // --- INSTANT VISUAL UPDATE LOGIC ---
    // derivedStateOf ensures this calculation runs efficiently on every frame of the scroll.
    // It finds the item index closest to the center of the viewport.
    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf null

            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            // Find item closest to center
            visibleItems.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }?.index
        }
    }

    // --- SYNC TO VIEWMODEL ---
    // Whenever the visual center changes, update the ViewModel.
    // This happens independently of the visual rendering, preventing lag.
    LaunchedEffect(centeredIndex) {
        centeredIndex?.let { index ->
            val actualMonthIndex = (index % 12).let { if (it < 0) it + 12 else it }
            if (actualMonthIndex != selectedMonthIndex) {
                onMonthSelected(actualMonthIndex)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val width = maxWidth
        val itemWidth = width / 5

        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                count = Int.MAX_VALUE,
                key = { it }
            ) { index ->
                val monthIndex = index % 12
                val adjustedMonthIndex = if (monthIndex < 0) monthIndex + 12 else monthIndex
                val monthName = months[adjustedMonthIndex]

                // Determine selection based on LOCAL calculation for instant feedback
                val isSelected = (index == centeredIndex)

                Box(
                    modifier = Modifier.width(itemWidth),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monthName,
                        style = if (isSelected) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        // Color updates instantly as centeredIndex changes
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable {
                                // Scroll this specific item index to the center (Slot 3)
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index - 2)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(
    onAddBudgetClick: () -> Unit,
    onAiRecommendationClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // AI Recommendation Button
        Button(
            onClick = onAiRecommendationClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD54F), // Gold Color
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "AI Recommendation",
                fontWeight = FontWeight.SemiBold
            )
        }

        // Add Budget Button
        OutlinedButton(
            onClick = onAddBudgetClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            border = BorderStroke(1.dp, Color.Black),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Black
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Add A New Budget",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TotalBudgetSummary(uiState: BudgetUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = uiState.totalLeft.toRupiahFormat(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "left",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${uiState.totalSpent.toRupiahFormat()} spent",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "${uiState.totalLimit.toRupiahFormat()} budgeted",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { uiState.overallPercentage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.LightGray.copy(alpha = 0.5f),
            strokeCap = StrokeCap.Round,
        )
    }
}

fun Double.toSimpleCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    return format.format(this)
}


private sealed class BudgetScreenState {
    data object List : BudgetScreenState()
    data class Detail(val budgetId: Int) : BudgetScreenState()
}