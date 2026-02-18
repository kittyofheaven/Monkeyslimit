package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.components.MarqueeText
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.ui.theme.lighten
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat

@Composable
fun TransactionRow(
    transaction: TransactionItemData,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick(transaction.id) }
                .padding(vertical = 12.dp, horizontal = 8.dp), // Added slight padding for breathing room
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Icon with circular background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape) // Changed to perfect circle like the image
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.08f // Slightly lighter alpha for cleaner look
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = transaction.iconResId),
                    contentDescription = transaction.title,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.width(12.dp))

            // 2. Middle Section: Title and Subtitle (Combined category & time)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                MarqueeText(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = TextUnit(15f, TextUnitType.Sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(2.dp))

                // Combined Category and Subtitle (e.g., "Subscription, 06:20 PM")
                Text(
                    text = "${transaction.category}, ${transaction.subtitle.substringBefore(" ")}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextUnit(12f, TextUnitType.Sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            // 3. Right Section: Amount and Type Label
            val amountColor = if (transaction.isExpense) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.primary
            }

            // Target image uses explicit "+" for income
            val amountPrefix = if (transaction.isExpense) "-" else "+"

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$amountPrefix${transaction.amount.toRupiahFormat()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = amountColor,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )

                Spacer(Modifier.height(2.dp))

                // Bottom Right Label (e.g., "Expense" or "Income")
                Text(
                    text = if (transaction.isExpense) "Expense" else "Income",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = TextUnit(12f, TextUnitType.Sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(0.2f),
            thickness = 1.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionRowPreview() {
    MonkeyslimitTheme {
        Column {
            TransactionRow(
                transaction = TransactionItemData(
                    id = 0,
                    iconResId = R.drawable.food,
                    title = "Google One Subscription",
                    subtitle = "06:20 PM",
                    category = "Subscription",
                    amount = 48700.0,
                    isExpense = true,
                )
            )
            TransactionRow(
                transaction = TransactionItemData(
                    id = 1,
                    iconResId = R.drawable.food, // replace with a briefcase icon
                    title = "Freelance UI Kit",
                    subtitle = "04:08 PM",
                    category = "Work",
                    amount = 1930000.0,
                    isExpense = false,
                )
            )
        }
    }
}