package com.menac1ngmonkeys.monkeyslimit.ui.analytics

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat

/**
 * Displays a summary row for total income and total expense.
 *
 * @param income The total income amount to display.
 * @param expense The total expense amount to display.
 * @param modifier The modifier to be applied to the container Row.
 */
@Composable
fun IncomeExpenseSummary(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryItem(
            label = "Income",
            amount = income,
            iconResId = R.drawable.income, // Make sure you have this drawable
            color = MaterialTheme.colorScheme.onSurface
        )
        SummaryItem(
            label = "Expense",
            amount = expense,
            iconResId = R.drawable.expense, // Make sure you have this drawable
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Double,
    @DrawableRes iconResId: Int,
    color: Color
) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(20)
            )
            .padding(16.dp)
            .defaultMinSize(minWidth = 100.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                tint = color,
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color,
            )
        }
        Text(
            text = amount.toRupiahFormat(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Preview
@Composable
private fun IncomeExpenseSummaryPreview() {
    MonkeyslimitTheme(darkTheme = true) {
        IncomeExpenseSummary(income = 47200.0, expense = 35510.20)
    }
}
