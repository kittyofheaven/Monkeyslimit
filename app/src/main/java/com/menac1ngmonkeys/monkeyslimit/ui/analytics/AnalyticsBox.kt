package com.menac1ngmonkeys.monkeyslimit.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.ui.components.RefreshIconButton
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme

/**
 * A container card for displaying analytics charts and controls.
 *
 * It includes a title, action icons for search and date selection, and a placeholder
 * area where a chart can be drawn.
 *
 * @param modifier The modifier to be applied to the container.
 * @param onRefreshClick A callback function for when the search icon is clicked.
 * @param onDateRangeClick A callback function for when the date range icon is clicked.
 * @param content The composable content to be placed inside the main area, typically a chart.
 */
@Composable
fun AnalyticsBox(
    modifier: Modifier = Modifier,
    onRefreshClick: () -> Unit = {},
    onDateRangeClick: () -> Unit = {},
    filterLabel: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = "Income & Expenses",
                    style = MaterialTheme.typography.titleMedium,
    //                fontWeight = FontWeight.Bold
                )
                ChartFilter(
                    filterLabel = filterLabel,
                )
            }
            Row {
                RefreshIconButton(onClick = onRefreshClick)

                IconButton(onClick = onDateRangeClick) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date Range")
                }
            }
        }

        // Chart Content Area
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsBoxPreview() {
    MonkeyslimitTheme(darkTheme = true) {
        AnalyticsBox {
            // This is how you would place a chart inside
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Chart will go here", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ChartFilter(
    filterLabel: String?,
    modifier: Modifier = Modifier,
) {
    // Reserve a constant height so the overall component doesn't shrink
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        if (filterLabel != null) {
            Text(
                text = filterLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // if null -> nothing is drawn, but the Box still takes the same vertical space
    }
}
