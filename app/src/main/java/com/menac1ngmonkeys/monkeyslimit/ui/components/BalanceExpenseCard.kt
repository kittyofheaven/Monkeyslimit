package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.ui.theme.lighten
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat
import java.util.Locale

@Composable
fun BalanceExpenseCard(
    modifier: Modifier = Modifier,
    // Removed 'totalBalance' as it is no longer used for the calculation
    totalIncome: Double,
    totalExpense: Double
) {
    val indonesianLocale = Locale.Builder()
        .setLanguage("in")
        .setRegion("ID")
        .build()

    // Logic updated: Calculate ratio based on Income instead of Balance
    var balanceExpenseRatio = 0f
    if (totalIncome != 0.0) {
        balanceExpenseRatio = (totalExpense / totalIncome).toFloat()
    }

    val balanceExpensePercentage = String.format(indonesianLocale, "%.2f%%", balanceExpenseRatio * 100f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // Total Income & Expenses Section
        Row(
            modifier = Modifier
                .height(55.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BalExpSingular(
                BalExpData.Income(amount = totalIncome)
            )
//            VerticalDivider(
//                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
//                thickness = 1.5f.dp,
//            )
            BalExpSingular(
                BalExpData.Expense(amount = totalExpense)
            )
        }
//        Spacer(modifier = Modifier.size(20.dp))
//        // Progress Bar
//        Box(
//            modifier = Modifier
//                .fillMaxWidth(0.9f)
//                .height(25.dp)
//                .clip(
//                    shape = RoundedCornerShape(50)
//                )
//                .background(Color(0xFF444444))
//        ) {
//            BalanceExpenseBar(
//                balanceExpenseRatio = balanceExpenseRatio,
//                balanceExpensePercentage = balanceExpensePercentage,
//                // We pass Income here so the bar shows "Expense / Income"
//                totalBalance = totalIncome.toRupiahFormat(),
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//        Spacer(
//            modifier = Modifier
//                .size(10.dp)
//        )
//        // Footer Text
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                painter = painterResource(
//                    id = R.drawable.select_check_box_24px
//                ),
//                contentDescription = "Check Box",
//                tint = Color.Unspecified
//            )
//            Spacer(Modifier.size(5.dp))
//            // Text updated to reflect the logic change
//            Text(
//                text = "$balanceExpensePercentage Of Total Income, Looks Good.",
//                fontSize = TextUnit(
//                    value = 12f,
//                    type = TextUnitType.Sp
//                )
//            )
//        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceExpenseCardPreview() {
    BalanceExpenseCard(
        totalExpense = 987654.321,
        totalIncome = 5000000.0,
    )
}