package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat

/**
 * A card that displays progress towards a savings goal, along with
 * other related financial summaries like weekly revenue and expenses.
 *
 * @param modifier The modifier to be applied to the card.
 * @param currentSavings The current amount saved towards the goal.
 * @param savingsGoal The target savings amount. Used to calculate progress.
 * @param revenueLastWeek The total revenue from the previous week to display.
 * @param foodExpenseLastWeek The total food expense from the previous week to display.
 */
@Composable
fun SavingsGoalCard(
    modifier: Modifier = Modifier,
    currentSavings: Double,
    savingsGoal: Double,
    revenueLastWeek: Double,
    foodExpenseLastWeek: Double
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp) // Giving a fixed height improves layout stability
            .clip(RoundedCornerShape(20))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(vertical = 5.dp, horizontal = 8.dp),
    ) {
        val progress = if (savingsGoal > 0) {
            (currentSavings / savingsGoal).toFloat()
        } else {
            0f
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Savings Goal Progress
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.35f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconWithProgressBorder(
                    size = 64.dp,
                    strokeWidth = 3.dp,
                    progressColor = MaterialTheme.colorScheme.onSecondary,
                    trackColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f),
                    iconTint = MaterialTheme.colorScheme.onSecondary,
                    iconResId = R.drawable.savings_bold_48px,
                    progress = progress
                )
                Text(
                    text = "Savings\nOn Goals",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = TextUnit(12f, TextUnitType.Sp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondary,
                    lineHeight = 15.sp
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 4.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onSecondary
            )

            // Right Side: Revenue and Expense Summaries
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.65f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoRow(
                    iconResId = R.drawable.salary,
                    label = "Revenue Last Week",
                    amount = revenueLastWeek
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.CenterHorizontally),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                InfoRow(
                    iconResId = R.drawable.food,
                    label = "Food Last Week",
                    amount = foodExpenseLastWeek,
                    isExpense = true
                )
            }
        }
    }
}

/**
 * A helper composable for displaying a row with an icon, label, and amount.
 * This avoids repeating the Row/Column structure inside the card.
 */
@Composable
private fun InfoRow(
    @DrawableRes iconResId: Int,
    label: String,
    amount: Double,
    isExpense: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.weight(0.2f),
            painter = painterResource(iconResId),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Column(
            modifier = Modifier.weight(0.8f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            val (amountColor, prefix) = when (isExpense) {
                true -> MaterialTheme.colorScheme.tertiary to "-"
                false -> MaterialTheme.colorScheme.onSecondary to ""
            }

            Text(
                text = "$prefix${amount.toRupiahFormat()}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SavingsGoalCardPreview() {
    MonkeyslimitTheme(darkTheme = true) {
        SavingsGoalCard(
            currentSavings = 12_500_000.0,
            savingsGoal = 50_000_000.0,
            revenueLastWeek = 2_150_000.0,
            foodExpenseLastWeek = 475_000.0
        )
    }
}
