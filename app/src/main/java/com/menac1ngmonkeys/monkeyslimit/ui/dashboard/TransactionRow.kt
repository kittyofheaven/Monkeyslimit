package com.menac1ngmonkeys.monkeyslimit.ui.dashboard

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.components.MarqueeText
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat

// Add this new composable to your DashboardScreen.kt file

@Composable
fun TransactionRow(transaction: TransactionItemData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Icon with background
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(30)) // Creates the rounded square look
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.1f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(id = transaction.iconResId),
                contentDescription = transaction.title,
                // In dark mode, primary is Orange, in light mode, it's Green.
                // Use secondary in light mode if you always want yellow/orange.
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.width(16.dp))

        // 2. Title and Subtitle
        Column(
            modifier = Modifier
                .weight(1f)
            ,

        ) {
            MarqueeText(
                text = transaction.title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = TextUnit(15f, TextUnitType.Sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = transaction.subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextUnit(10f, TextUnitType.Sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        VerticalDivider(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 4.dp)
            ,
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        // 3. Category
        MarqueeText(
            text = transaction.category,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(0.7f),
//            textAlign = TextAlign.Center
        )

        VerticalDivider(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 4.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        // 4. Amount
        val amountColor = if (transaction.isExpense) {
            MaterialTheme.colorScheme.tertiary // This is your Red color
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val amountPrefix = if (transaction.isExpense) "-" else ""

        Text(
            text = "$amountPrefix${transaction.amount.toRupiahFormat()}",
            style = MaterialTheme.typography.labelLarge.copy(),
            color = amountColor,
            textAlign = TextAlign.End,
            modifier = Modifier.width(100.dp) // Give it a fixed width to align amounts
        )
    }
}

@Preview(
    showBackground = true,
//    uiMode = androidx.work.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun TransactionRowPreview() {
    MonkeyslimitTheme {
        TransactionRow(
            transaction = TransactionItemData(
                iconResId = R.drawable.food,
                title = "Groceries",
                subtitle = "17:00 - April 24",
                category = "Pantry",
                amount = 100000.0,
                isExpense = true
            )
        )
    }
}