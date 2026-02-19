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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.menac1ngmonkeys.monkeyslimit.utils.compactNumber
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

    // Use .0 to ensure these are evaluated as Doubles to safely handle up to Trillions
    val amountValue = balExpData.amount
    val isHundredMillionPlus = amountValue >= 100_000_000.0
    val isTenMillionPlus = amountValue >= 10_000_000.0

    Column(
        modifier = Modifier.width(150.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (amountValue > 1_000_000_000_000_000) {
                ">1000T"
            } else if (isHundredMillionPlus) {
                // E.g. -Rp100M, -Rp10B, -Rp10T
                "${prefix}Rp${compactNumber(balExpData.amount)}"
            } else {
                // E.g. -Rp99.999.999
                prefix + balExpData.amount.toRupiahFormat()
            }

            ,
            fontSize = if (isTenMillionPlus && !isHundredMillionPlus) {
                // Only shrink font for uncompacted long numbers (10M - 99M)
                18.sp
            } else {
                // Compacted numbers (10T) are short strings, so they fit perfectly at 20.sp!
                20.sp
            },
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