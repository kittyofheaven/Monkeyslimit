package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat
import java.util.Locale

@Composable
fun BalanceExpenseCard(
    modifier: Modifier = Modifier,
    totalBalance: Double,
    totalExpense: Double
) {
    val indonesianLocale = Locale.Builder()
        .setLanguage("in")  // "in" is the code for Indonesian
        .setRegion("ID")    // "ID" is the code for Indonesia
        .build()
    var balanceExpenseRatio = (totalExpense / totalBalance).toFloat()
    var balanceExpensePercentage = String.format(indonesianLocale,"%.2f%%", balanceExpenseRatio * 100f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Total Balance & Expenses Section
        Row(
            modifier = Modifier
                .height(55.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BalExpSingular(
                BalExpData.Balance(amount = totalBalance)
            )
            VerticalDivider(
                color = Color(0xFFDFF7E2), // LightGreen100
                thickness = 1.5f.dp,
            )
            BalExpSingular(
                BalExpData.Expense(amount = totalExpense)
            )
        }
        Spacer(
            modifier = Modifier
                .size(20.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(25.dp)
                .clip(
                    shape = RoundedCornerShape(50)
                )
                .background(Color(0xFF444444))
        ) {
            BalanceExpenseBar(
                balanceExpenseRatio = balanceExpenseRatio,
                balanceExpensePercentage = balanceExpensePercentage,
                totalBalance = totalBalance.toRupiahFormat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(
            modifier = Modifier
                .size(10.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.select_check_box_24px
                ),
                contentDescription = "Check Box",
                tint = Color.Unspecified
            )
            Spacer(Modifier.size(5.dp))
            Text(
                text = "$balanceExpensePercentage Of Total Balance, Looks Good.",
                fontSize = TextUnit(
                    value = 12f,
                    type = TextUnitType.Sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceExpenseCardPreview() {
    BalanceExpenseCard(
        totalBalance = 123456.789,
        totalExpense = 987654.321,
    )
}