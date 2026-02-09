package com.menac1ngmonkeys.monkeyslimit.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.utils.compactNumber
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat

/**
 * Displays the colorful Income and Expense cards side-by-side.
 */
@Composable
fun ClickableIncomeExpenseSummary(
    income: Double,
    expense: Double,
    onIncomeClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Income Card (Greenish)
        SummaryCard(
            title = "Income",
            amount = income,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            backgroundColor = MaterialTheme.colorScheme.primary, // Material Green 600 approx
            contentColor = Color.White,
            modifier = Modifier
                .weight(1f)
                .clickable { onIncomeClick() }
        )

        // Expense Card (Reddish)
        SummaryCard(
            title = "Expense",
            amount = expense,
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f), // Material Red 300 approx
            contentColor = Color.White,
            modifier = Modifier
                .weight(1f)
                .clickable { onExpenseClick() }
        )
    }
}

@Composable
fun ClickableSavingsCard(
    income: Double,
    expense: Double,
    accumulatedSavings: Double,
    onClick: () -> Unit
) {
    val savings = income - expense
    // Simple calculation for percentage of income saved
    val percentage = if (income != 0.0) (savings / income) * 100 else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp), // Rounded corners to match SummaryCard
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Added shadow for "pop up" effect
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header Icon + Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Savings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // Amount
                        Text(
                            text = savings.toRupiahFormat(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Percentage Text
                        Text(
                            text = "${"%.1f".format(percentage)}% of income",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (savings >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    }

                    // Right Side: Accumulated Total (Bottom Right)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL SAVINGS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF829D79), // Muted Green/Grey label
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = accumulatedSavings.toRupiahFormat(), // Using compact for space
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF757575) // Grey Value
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Icon and Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            Text(
                text = amount.toRupiahFormat(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomeExpenseSummaryPreview() {
    MonkeyslimitTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            ClickableIncomeExpenseSummary(
                income = 25000000.0, expense = 9250000.0,
                onIncomeClick = {},
                onExpenseClick = {}
            )
            Spacer(Modifier.height(16.dp))
            ClickableSavingsCard(
                income = 25000000.0,
                expense = 9250000.0,
                onClick = {},
                accumulatedSavings = 9000.0,
            )
        }
    }
}