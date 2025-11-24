package com.menac1ngmonkeys.monkeyslimit.ui.components

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.menac1ngmonkeys.monkeyslimit.utils.compactNumber
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
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

/**
 * Renders an animated dual-line analytics chart (Income vs. Expense) using the
 * Vico Compose charting library.
 *
 * This composable is responsible for translating chart data (values + labels)
 * into a polished visual line chart with:
 *
 * - Smooth cubic line interpolation
 * - Animated line drawing and point reveal
 * - Tap-to-highlight marker with custom styled text
 * - Colored dots on each data point
 * - Dynamic y-axis formatting using `compactNumber()`
 * - X-axis date formatting provided externally via `dateLabels`
 *
 * ---
 *
 * ## Data Model
 *
 * The chart displays two series:
 *
 * 1. **Income**
 * 2. **Expense**
 *
 * Each series is provided as a list of numeric values (`Number`), where:
 *
 * - The **index** of each value represents the x-axis position.
 * - The corresponding **`dateLabels[index]`** entry is used as that point's
 *   human-readable x-axis label.
 *
 * The ViewModel must guarantee that:
 * ```
 * incomeValues.size == expenseValues.size == dateLabels.size
 * ```
 *
 * If the lists are empty, the chart displays a friendly placeholder instead
 * of crashing (Vico does not allow empty line series).
 *
 * ---
 *
 * ## Appearance & Styling
 *
 * ### Lines
 * Each series is rendered as a smooth cubic Bézier curve using:
 *
 * `LineCartesianLayer.PointConnector.cubic(curvature = 0.4f)`
 *
 * The line color is controlled via:
 *
 * - [incomeColor] — typically themed yellow
 * - [expenseColor] — typically themed red
 *
 * ---
 *
 * ### Data Points (Dots)
 * Each line has circular data points defined using `CorneredShape.Rounded`.
 * Dot sizes can be adjusted via the `sizeDp` parameter.
 *
 * ---
 *
 * ### Marker (Tap-to-highlight)
 *
 * Tapping or long-pressing the chart surface displays a floating marker bubble
 * showing:
 *
 * - The x-axis label (date)
 * - The compact income value (colored `incomeColor`)
 * - The compact expense value (colored `expenseColor`)
 *
 * Marker text is rendered using a `SpannableString` to support per-value color
 * spans. The marker background uses the current Material surface color for
 * readability in both light and dark themes.
 *
 * Example marker text:
 * ```
 * 18 Nov — 1.3M, 820K
 * ```
 *
 * ---
 *
 * ### Y-Axis (Start Axis)
 *
 * Y-axis values are formatted using `compactNumber()` without modifying
 * the underlying numeric domain. For example:
 *
 * ```
 * 0 → "0"
 * 1800000 → "1.8M"
 * 5200000000 → "5.2B"
 * ```
 *
 * The axis still uses the real numeric values to compute its range and ticks.
 *
 * ---
 *
 * ### Animation
 *
 * The chart animates in when it first appears using:
 *
 * ```
 * animationSpec = tween(
 *     durationMillis = 700,
 *     easing = FastOutSlowInEasing,
 * )
 * ```
 *
 * and supports animated transitions when data changes via Vico’s model
 * diffing system.
 *
 * ---
 *
 * ## Parameters
 *
 * @param incomeValues The numeric values for the income line series.
 * @param expenseValues The numeric values for the expense line series.
 * @param dateLabels X-axis text labels corresponding to each data point.
 * @param modifier Optional Compose modifier for layout behavior.
 * @param incomeColor The color used for the income line and its data points.
 * @param expenseColor The color used for the expense line and its data points.
 *
 * ---
 *
 * ## Errors & Edge Cases
 *
 * - If income or expense lists are empty, Vico would normally throw
 *   `"Series can't be empty."` — this composable prevents that by displaying
 *   a placeholder instead of rendering the chart.
 * - If `dateLabels` is shorter than the series length, missing labels are
 *   gracefully defaulted to the last label.
 *
 * ---
 *
 * ## Used By
 *
 * This composable is intended to be used inside analytics screens such as
 * `AnalyticsScreenContent`, where the ViewModel provides time-bucketed values
 * (DAILY, WEEKLY, MONTHLY, YEARLY) and corresponding `dateLabels`.
 */

@Composable
fun IncomeExpenseLineChart(
    incomeValues: List<Number>,
    expenseValues: List<Number>,
    dateLabels: List<String>,
    modifier: Modifier = Modifier,
    incomeColor: Color = MaterialTheme.colorScheme.primary,
    expenseColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    // 🔒 If there's no data, show a simple placeholder and bail out
    if (incomeValues.isEmpty() || expenseValues.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data for this timeframe",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp),
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    // feed data to chart (this triggers diff animation when values change)
    LaunchedEffect(incomeValues, expenseValues) {
        if (incomeValues.isEmpty() || expenseValues.isEmpty()) {
            // nothing to plot, don't update the model
            return@LaunchedEffect
        }

        modelProducer.runTransaction {
            lineSeries {
                series(incomeValues)  // series 1: income
                series(expenseValues) // series 2: expense
            }
        }
    }

    // Rounded shape for dots / marker background
    val circleShape = CorneredShape(
        Corner.Rounded,
        Corner.Rounded,
        Corner.Rounded,
        Corner.Rounded
    )

    // --- Points (dots) style for each series ---
    val incomePoint = LineCartesianLayer.Point(
        component = rememberShapeComponent(
            shape = circleShape,
            fill = fill(incomeColor),
        ),
        sizeDp = 6f, // dot size
    )

    val expensePoint = LineCartesianLayer.Point(
        component = rememberShapeComponent(
            shape = circleShape,
            fill = fill(expenseColor),
        ),
        sizeDp = 6f,
    )

    // --- Custom Y range: always round max up to a “nice” power-of-ten step ---
    val rangeProvider = remember {
        object : CartesianLayerRangeProvider {
            override fun getMinX(
                minX: Double,
                maxX: Double,
                extraStore: ExtraStore
            ): Double = minX

            override fun getMaxX(
                minX: Double,
                maxX: Double,
                extraStore: ExtraStore
            ): Double = maxX

            override fun getMinY(
                minY: Double,
                maxY: Double,
                extraStore: ExtraStore
            ): Double {
                // Always start at 0 on the Y axis
                return 0.0
            }

            override fun getMaxY(
                minY: Double,
                maxY: Double,
                extraStore: ExtraStore
            ): Double {
                if (maxY <= 0.0) return maxY

                // Example:
                // maxY = 3_200_000 → exponent = 6 → unit = 10^6 = 1_000_000
                // -> ceil(3_200_000 / 1_000_000) = 4 → 4 * 1_000_000 = 4_000_000
                var exponent = floor(log10(maxY))
                // cap the exponent to 6
                exponent = if (exponent > 6.00) 6.00 else exponent
                val unit = 10.0.pow(exponent)      // 1, 10, 100, 1000, 1_000_000, ...
                return ceil(maxY / unit) * unit    // next "round" max
            }
        }
    }


    // --- Smooth colored lines with dots ---
    val lineLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
            // income line
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(fill(incomeColor)),
                pointConnector = LineCartesianLayer.PointConnector.cubic(curvature = 0.5f),
                pointProvider = LineCartesianLayer.PointProvider.single(incomePoint),
            ),
            // expense line
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(fill(expenseColor)),
                pointConnector = LineCartesianLayer.PointConnector.cubic(curvature = 0.5f),
                pointProvider = LineCartesianLayer.PointProvider.single(expensePoint),
            ),
        ),
        rangeProvider = rangeProvider,
    )

    // Formatter for the marker label text
    val markerValueFormatter = remember(dateLabels) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            // We use LineCartesianLayerMarkerTarget because we only have line layers
            val lineTarget = targets.filterIsInstance<LineCartesianLayerMarkerTarget>()
                .firstOrNull() ?: return@ValueFormatter ""

            if (lineTarget.points.isEmpty()) return@ValueFormatter ""

            // We assume:
            // points[0] = income (first series)
            // points[1] = expense (second series)
            val incomePoint = lineTarget.points.getOrNull(0)
            val expensePoint = lineTarget.points.getOrNull(1)

            // x index comes from the entry.x (0, 1, 2, ...)
            val xIndex = incomePoint?.entry?.x?.toInt() ?: 0
            val dateLabel = dateLabels.getOrNull(xIndex) ?: ""

            val incomeShort = incomePoint?.entry?.y?.let { compactNumber(it) } ?: "-"
            val expenseShort = expensePoint?.entry?.y?.let { compactNumber(it) } ?: "-"

            // Plain text first
            val text = "$dateLabel — $incomeShort, $expenseShort"
            val spannable = SpannableString(text)

            // Calculate ranges for income and expense substrings
            val dashAndSpace = " — "
            val commaAndSpace = ", "

            val incomeStart = dateLabel.length + dashAndSpace.length
            val incomeEnd = incomeStart + incomeShort.length

            val expenseStart = incomeEnd + commaAndSpace.length
            val expenseEnd = expenseStart + expenseShort.length

            // Apply colors (income = yellow, expense = red)
            spannable.setSpan(
                ForegroundColorSpan(incomeColor.toArgb()),
                incomeStart,
                incomeEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                ForegroundColorSpan(expenseColor.toArgb()),
                expenseStart,
                expenseEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable
        }
    }


    // --- Tap-to-highlight marker (appears on press) ---
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
        valueFormatter = { _, y, _ ->
            // y is the raw double (0.0, 1_200_000.0, etc.)
            compactNumber(y)
        }
    )

    val bottomAxis = HorizontalAxis.rememberBottom(
        valueFormatter = { _, x, _ ->
            val index = x.toInt()
            if (dateLabels.isNotEmpty()) {
                // Try to map to a date label, otherwise fall back to last label
                dateLabels.getOrNull(index)
                    ?: dateLabels.last()
            } else {
                // No labels? Fall back to the numeric index.
                index.toString()
            }
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            lineLayer,
            startAxis = startAxis,
            bottomAxis = bottomAxis,
            marker = marker, // <- enables tap-to-highlight
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        // --- Animated line + point reveal ---
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing,
        ),
        animateIn = true, // initial "draw in" animation when chart first appears
    )
}









