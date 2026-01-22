package com.menac1ngmonkeys.monkeyslimit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@Composable
fun BalanceExpenseBar(
    balanceExpenseRatio: Float,
    balanceExpensePercentage: String,
    totalBalance: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF444444),
    progressColor: Color = MaterialTheme.colorScheme.secondary,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    height: Dp = 25.dp,
    cornerRadius: Dp = 50.dp
) {
    // PILL ABU
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            // LEFT pill (progress part)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(balanceExpenseRatio.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(progressColor)
                    .padding(start = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = balanceExpensePercentage,
                    color = textColor,
                    fontSize = TextUnit(12f, TextUnitType.Sp),
                )
            }

            // RIGHT text
            Text(
                text = totalBalance,
                color = Color.White,
                fontSize = TextUnit(12f, TextUnitType.Sp),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(end = 10.dp),
            )
        }
    }
}
