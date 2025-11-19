package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.utils.toRupiahFormat

@Composable
fun BalExpSingular(
    balExpData: BalExpData
) {
    // --- Logic for styling ---
    val isExpense = balExpData is BalExpData.Expense

    // Set color to Red (Error) if it's an expense, otherwise use default (OnSurface)
    val amountColor = if (isExpense) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    // Add a "-" prefix only if it's an expense
    val prefix = if (isExpense) "-" else ""
    // --- End of logic ---

    Column(
        modifier = Modifier
            .width(150.dp)
//            .padding(5.dp)
        ,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(balExpData.iconId),
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = balExpData.title,
                fontSize = TextUnit(12f, TextUnitType.Sp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = prefix + balExpData.amount.toRupiahFormat(),
            fontSize = TextUnit(20f, TextUnitType.Sp),
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BalExpSingularPreview() {
    BalExpSingular(BalExpData.Expense(1982.33))
}