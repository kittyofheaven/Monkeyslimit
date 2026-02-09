package com.menac1ngmonkeys.monkeyslimit.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.ui.theme.MonkeyslimitTheme

/**
 * A container card for displaying analytics charts.
 * Aligns with the white card style in the design.
 */
@Composable
fun AnalyticsBox(
    modifier: Modifier = Modifier,
    onRefreshClick: () -> Unit = {},
    onDateRangeClick: () -> Unit = {},
    filterLabel: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Income & Expense Trend", // Or "Monthly Expense Trend"
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Optional: You can put specific chart actions here if needed
                // For now keeping it simple as per the clean design image
            }

            // Chart Content Area
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsBoxPreview() {
    MonkeyslimitTheme(darkTheme = true) {
        AnalyticsBox {
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