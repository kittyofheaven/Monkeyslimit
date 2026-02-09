package com.menac1ngmonkeys.monkeyslimit.ui.analytics

import AppViewModelProvider
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.ui.components.IncomeExpenseLegend
import com.menac1ngmonkeys.monkeyslimit.ui.components.IncomeExpenseLineChart
import com.menac1ngmonkeys.monkeyslimit.ui.state.AnalyticsUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.AppUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.CategoryExpense
import com.menac1ngmonkeys.monkeyslimit.ui.state.Timeframe
import com.menac1ngmonkeys.monkeyslimit.utils.compactNumber
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AnalyticsViewModel
import com.menac1ngmonkeys.monkeyslimit.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.roundToInt

enum class AnalyticsDetailType {
    INCOME, EXPENSE, SAVINGS
}

@Composable
fun AnalyticsScreenContent(
    modifier: Modifier = Modifier,
    appUiState: AppUiState,
    analyticsUiState: AnalyticsUiState,
    onTimeframeSelected: (Timeframe) -> Unit,
    onDatePrev: () -> Unit,
    onDateNext: () -> Unit,
    onDateRangeApplied: (LocalDate?, LocalDate?) -> Unit,
    onDetailClick: (AnalyticsDetailType) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 2. Timeframe Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeframeSelector(
                        selectedTimeframe = analyticsUiState.selectedTimeframe,
                        onTimeframeSelected = onTimeframeSelected,
                        modifier = Modifier.weight(0.9f)
                    )

                    IconButton(
                        onClick = { /* TODO: Export */ },
                        modifier = Modifier.weight(0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 3. Date Range Selector
            item {
                DateRangeSelector(
                    selectedTimeframe = analyticsUiState.selectedTimeframe,
                    currentDate = analyticsUiState.currentDate,
                    onPrevClick = onDatePrev,
                    onNextClick = onDateNext
                )
            }

            // 4. Income & Expense Cards
            item {
                ClickableIncomeExpenseSummary(
                    income = analyticsUiState.totalIncome,
                    expense = analyticsUiState.totalExpense,
                    onIncomeClick = { onDetailClick(AnalyticsDetailType.INCOME) },
                    onExpenseClick = { onDetailClick(AnalyticsDetailType.EXPENSE) }
                )
            }

            // 5. Savings Card
            item {
                ClickableSavingsCard(
                    income = analyticsUiState.totalIncome,
                    expense = analyticsUiState.totalExpense,
                    accumulatedSavings = analyticsUiState.totalAccumulatedSavings, // Pass the new value
                    onClick = { onDetailClick(AnalyticsDetailType.SAVINGS) }
                )
            }

            // 6. Chart Card
            item {
                AnalyticsBox {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val incomeColor = Color(0xFF7CB342)
                        val expenseColor = Color(0xFFE57373)

                        IncomeExpenseLineChart(
                            incomeValues = analyticsUiState.incomeValues,
                            expenseValues = analyticsUiState.expenseValues,
                            dateLabels = analyticsUiState.dateLabels,
                            incomeColor = incomeColor,
                            expenseColor = expenseColor,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        )

                        Spacer(Modifier.height(16.dp))

                        IncomeExpenseLegend(
                            incomeColor = incomeColor,
                            expenseColor = expenseColor,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }

            // 7. Expense by Category
            if (analyticsUiState.categoryExpenses.isNotEmpty()) {
                item {
                    ExpenseByCategoryCard(
                        categories = analyticsUiState.categoryExpenses
                    )
                }
            }

            // 8. Top Expenses List
            if (analyticsUiState.topExpenses.isNotEmpty()) {
                item {
                    TopExpensesSection(
                        transactions = analyticsUiState.topExpenses,
                        categoryMap = analyticsUiState.categoryMap
                    )
                }
            }
        }
    }
}

// --- DETAIL SCREEN OVERLAY (SWIPEABLE + SMOOTH ANIMATION + LIST) ---

@Composable
fun DetailScreen(
    type: AnalyticsDetailType,
    transactions: List<Transactions>, // Receive List
    categoryMap: Map<Int, Categories>, // Receive Map
    onBack: () -> Unit
) {
    // Animation State for Drag
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        val currentOffset = offsetY.value

                        if (currentOffset > 600f) {
                            onBack()
                        } else {
                            scope.launch { offsetY.animateTo(0f) }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (offsetY.value + dragAmount).coerceAtLeast(0f)
                        scope.launch { offsetY.snapTo(newOffset) }
                    }
                )
            },
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${type.name.lowercase().replaceFirstChar { it.uppercase() }} Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Transaction List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(transactions) { transaction ->
                        val category = categoryMap[transaction.categoryId]
                        TransactionDetailItem(
                            transaction = transaction,
                            categoryName = category?.name ?: "Unknown",
                            iconId = category?.icon
                        )
                    }
                }
            }
        }
    }
}

// New Item Composable for Detail List
@Composable
fun TransactionDetailItem(
    transaction: Transactions,
    categoryName: String,
    iconId: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconId ?: R.drawable.expense),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note ?: categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = transaction.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = transaction.totalAmount.toRupiahFormat(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

// --- EXISTING COMPONENTS (Chart, Donut, List) ---

@Composable
fun ExpenseByCategoryCard(
    categories: List<CategoryExpense>,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<CategoryExpense?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Expense by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    DonutChart(
                        categories = categories,
                        size = 140.dp,
                        selectedCategory = selectedCategory,
                        onCategoryClick = { selectedCategory = if (selectedCategory == it) null else it }
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selectedCategory != null) {
                            Text(
                                text = "${selectedCategory?.percentage?.toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedCategory?.categoryName ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .width(100.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "100%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 120.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        val opacity = if (selectedCategory == null || isSelected) 1f else 0.3f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures {
                                    selectedCategory = if (isSelected) null else category
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(category.color.copy(alpha = opacity), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = opacity),
                                modifier = Modifier.width(100.dp)
                            )
                            Text(
                                text = "${category.percentage.toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = opacity)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    categories: List<CategoryExpense>,
    size: Dp,
    selectedCategory: CategoryExpense?,
    onCategoryClick: (CategoryExpense) -> Unit
) {
    Canvas(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = Offset(size.toPx() / 2, size.toPx() / 2)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (angle < 0) angle += 360f
                    val touchAngleNormalized = (angle + 90) % 360

                    var currentStartAngle = 0f
                    for (category in categories) {
                        val sweepAngle = (category.percentage / 100f) * 360f
                        if (touchAngleNormalized >= currentStartAngle && touchAngleNormalized < currentStartAngle + sweepAngle) {
                            onCategoryClick(category)
                            return@detectTapGestures
                        }
                        currentStartAngle += sweepAngle
                    }
                }
            }
    ) {
        var startAngle = -90f
        val baseStrokeWidth = 30f
        val selectedStrokeWidth = 45f

        categories.forEach { category ->
            val isSelected = selectedCategory == category
            val sweepAngle = (category.percentage / 100f) * 360f
            val strokeWidth = if (isSelected) selectedStrokeWidth else baseStrokeWidth
            val color = if (selectedCategory == null || isSelected) category.color else category.color.copy(alpha = 0.3f)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun TopExpensesSection(
    transactions: List<Transactions>,
    categoryMap: Map<Int, Categories>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Top Expenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "This period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            transactions.forEachIndexed { index, transaction ->
                val category = categoryMap[transaction.categoryId]
                Log.d("TopExpensesSection", "Category: ${category?.name}")
                TopExpenseItem(
                    rank = index + 1,
                    transaction = transaction,
                    categoryName = category?.name ?: "Category ${transaction.categoryId}",
                    iconId = category?.icon
                )
            }
        }
    }
}

@Composable
fun TopExpenseItem(
    rank: Int,
    transaction: Transactions,
    categoryName: String,
    iconId: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0E68C).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF967C37)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = iconId ?: R.drawable.expense),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$categoryName • " + transaction.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("d MMM")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = compactNumber(transaction.totalAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory),
    analyticsViewModel: AnalyticsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val appUiState by appViewModel.appUiState.collectAsState()
    val analyticsUiState by analyticsViewModel.analyticsUiState.collectAsState()

    var selectedDetail by remember { mutableStateOf<AnalyticsDetailType?>(null) }
    var contentToDisplay by remember { mutableStateOf<AnalyticsDetailType?>(null) }

    if (selectedDetail != null) {
        contentToDisplay = selectedDetail
    }

    BackHandler(enabled = selectedDetail != null) {
        selectedDetail = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnalyticsScreenContent(
            modifier = modifier,
            appUiState = appUiState,
            analyticsUiState = analyticsUiState,
            onTimeframeSelected = { timeframe ->
                analyticsViewModel.selectTimeframe(timeframe)
            },
            onDatePrev = { analyticsViewModel.decrementDate() },
            onDateNext = { analyticsViewModel.incrementDate() },
            onDateRangeApplied = { startDate, endDate ->
                analyticsViewModel.setCustomRange(startDate, endDate)
            },
            onDetailClick = { type ->
                selectedDetail = type
            }
        )

        AnimatedVisibility(
            visible = selectedDetail != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            contentToDisplay?.let { type ->
                // Select correct transaction list
                val transactionsToShow = when(type) {
                    AnalyticsDetailType.INCOME -> analyticsUiState.incomeTransactions
                    AnalyticsDetailType.EXPENSE -> analyticsUiState.expenseTransactions
                    AnalyticsDetailType.SAVINGS -> analyticsUiState.incomeTransactions + analyticsUiState.expenseTransactions
                }

                DetailScreen(
                    type = type,
                    transactions = transactionsToShow,
                    categoryMap = analyticsUiState.categoryMap,
                    onBack = { selectedDetail = null }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    AnalyticsScreen()
}