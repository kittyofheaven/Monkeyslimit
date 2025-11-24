package com.menac1ngmonkeys.monkeyslimit.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.ui.state.Timeframe
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme

/**
 * A row of buttons for selecting a time period (e.g., Daily, Weekly).
 *
 * @param selectedTimeframe The currently active timeframe.
 * @param onTimeframeSelected A callback function that is invoked when a timeframe button is clicked.
 * @param modifier The modifier to be applied to the container card.
 */
@Composable
fun TimeframeSelector(
    selectedTimeframe: Timeframe,
    onTimeframeSelected: (Timeframe) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(25),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Timeframe.entries.forEach { timeframe ->
                val isSelected = selectedTimeframe == timeframe
                Button(
                    modifier = Modifier,
                    contentPadding = PaddingValues(12.5f.dp),
                    onClick = { onTimeframeSelected(timeframe) },
                    shape = RoundedCornerShape(35),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                ) {
                    Text(
                        text = timeframe.name.lowercase().replaceFirstChar { it.titlecase() },
                        style = textStyle,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TimeframeSelectorPreview() {
    MonkeyslimitTheme(darkTheme = true) {
        TimeframeSelector(
            selectedTimeframe = Timeframe.MONTHLY,
            onTimeframeSelected = {}
        )
    }
}
