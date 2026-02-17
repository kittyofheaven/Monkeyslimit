package com.menac1ngmonkeys.monkeyslimit.ui.components

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.utils.compactNumber
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.CorneredShape.Corner
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun IncomeExpenseLineChart(
    incomeValues: List<Number>,
    expenseValues: List<Number>,
    dateLabels: List<String>,
    modifier: Modifier = Modifier,
    incomeColor: Color = MaterialTheme.colorScheme.primary,
    expenseColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    // --- 1. NO DATA STATE CHECK ---
    val hasData = remember(incomeValues, expenseValues) {
        val totalIncome = incomeValues.sumOf { it.toDouble() }
        val totalExpense = expenseValues.sumOf { it.toDouble() }
        totalIncome != 0.0 || totalExpense != 0.0
    }

    if (incomeValues.isEmpty() || expenseValues.isEmpty() || !hasData) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "No transactions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "There is no data for the selected period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // --- 2. CHART CONFIGURATION ---

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(incomeValues, expenseValues) {
        if (incomeValues.isNotEmpty() && expenseValues.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(incomeValues)
                    series(expenseValues)
                }
            }
        }
    }

    // Styles
    val circleShape = CorneredShape(Corner.Rounded)
    val incomePoint = LineCartesianLayer.Point(
        component = rememberShapeComponent(shape = circleShape, fill = fill(incomeColor)),
        sizeDp = 6f,
    )
    val expensePoint = LineCartesianLayer.Point(
        component = rememberShapeComponent(shape = circleShape, fill = fill(expenseColor)),
        sizeDp = 6f,
    )

    // Range Provider (Autoscale Y-Axis)
    // Range Provider (Autoscale Y-Axis to Even Thousands)
    val rangeProvider = remember {
        object : CartesianLayerRangeProvider {
            override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore) = minX
            override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore) = maxX

            // Start Y at 0
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = 0.0

            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
                val safeMax = if (maxY <= 0.0001) 1.0 else maxY

                // 5 labels means there are exactly 4 spaces (intervals) between them
                val rawInterval = safeMax / 4.0

                // Find the nearest thousand for the interval, rounded up
                val intervalInThousands = kotlin.math.ceil(rawInterval / 1000.0).toLong()

                // Force the interval to be an EVEN number in thousands (e.g., 2k, 4k, 6k)
                val evenInterval = if (intervalInThousands % 2L == 0L) {
                    if (intervalInThousands == 0L) 2L else intervalInThousands
                } else {
                    intervalInThousands + 1L
                }

                // The new max Y is exactly 4 perfectly even intervals!
                return (evenInterval * 1000.0) * 4
            }
        }
    }

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(fill(incomeColor)),
                pointConnector = LineCartesianLayer.PointConnector.cubic(curvature = 0.5f),
                pointProvider = LineCartesianLayer.PointProvider.single(incomePoint),
            ),
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(fill(expenseColor)),
                pointConnector = LineCartesianLayer.PointConnector.cubic(curvature = 0.5f),
                pointProvider = LineCartesianLayer.PointProvider.single(expensePoint),
            ),
        ),
        rangeProvider = rangeProvider,
    )

    // Marker
    val markerValueFormatter = remember(dateLabels) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val lineTarget = targets.filterIsInstance<LineCartesianLayerMarkerTarget>().firstOrNull()
                ?: return@ValueFormatter ""

            val incomePoint = lineTarget.points.getOrNull(0)
            val expensePoint = lineTarget.points.getOrNull(1)
            val xIndex = incomePoint?.entry?.x?.toInt() ?: 0
            val dateLabel = dateLabels.getOrNull(xIndex) ?: ""

            val incomeShort = incomePoint?.entry?.y?.let { compactNumber(it) } ?: "-"
            val expenseShort = expensePoint?.entry?.y?.let { compactNumber(it) } ?: "-"

            val text = "$dateLabel — $incomeShort, $expenseShort"
            val spannable = SpannableString(text)
            val dashAndSpace = " — "
            val commaAndSpace = ", "
            val incomeStart = dateLabel.length + dashAndSpace.length
            val incomeEnd = incomeStart + incomeShort.length
            val expenseStart = incomeEnd + commaAndSpace.length
            val expenseEnd = expenseStart + expenseShort.length

            spannable.setSpan(ForegroundColorSpan(incomeColor.toArgb()), incomeStart, incomeEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(expenseColor.toArgb()), expenseStart, expenseEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable
        }
    }

    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurface,
            background = rememberShapeComponent(
                shape = circleShape,
                fill = fill(MaterialTheme.colorScheme.surface),
            ),
            padding = insets(horizontal = 8.dp, vertical = 4.dp),
        ),
        valueFormatter = markerValueFormatter,
        labelPosition = DefaultCartesianMarker.LabelPosition.Top
    )

    val startAxis = VerticalAxis.rememberStart(
        // Label Text Color
        label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),

        // ADD THIS LINE: Limits the Y-axis to exactly 5 labels
        itemPlacer = remember { VerticalAxis.ItemPlacer.count({ 5 }) },

        valueFormatter = { _, y, _ ->
            val label = compactNumber(y)
            if (label.isBlank()) " " else label
        }
    )

    val bottomAxis = HorizontalAxis.rememberBottom(
        // 1. Label Text Color
        label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),
        valueFormatter = { _, x, _ ->
            val index = x.toInt()
            val label = if (dateLabels.isNotEmpty()) {
                dateLabels.getOrNull(index) ?: dateLabels.last()
            } else {
                index.toString()
            }
            if (label.isBlank()) " " else label
        }
    )

    // --- ZOOM & SCROLL LOCK ---
    // By disabling zoom, Vico automatically fits the X-axis content to the available width.
    val zoomState = rememberVicoZoomState(zoomEnabled = true)
    val scrollState = rememberVicoScrollState()

    CartesianChartHost(
        chart = rememberCartesianChart(
            lineLayer,
            startAxis = startAxis,
            bottomAxis = bottomAxis,
            marker = marker,
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        zoomState = zoomState,
        scrollState = scrollState,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
    )
}