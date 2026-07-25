package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiagnosisResult
import com.example.data.model.PlantDiaryEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class DailyHealthPoint(
    val dayLabel: String,
    val fullDateStr: String,
    val timestamp: Long,
    val healthScore: Int, // 0 - 100 %
    val heightCm: Float?, // height in cm if available
    val plantName: String,
    val note: String?,
    val hasLog: Boolean
)

enum class GraphMetric {
    HEALTH_SCORE,
    HEIGHT_GROWTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyHealthGraphCard(
    diaryEntries: List<PlantDiaryEntry>,
    historyList: List<DiagnosisResult>,
    modifier: Modifier = Modifier,
    onAddLogClick: (() -> Unit)? = null
) {
    var selectedMetric by remember { mutableStateOf(GraphMetric.HEALTH_SCORE) }
    var selectedPlantName by remember { mutableStateOf("All Plants") }

    // Collect list of available plant names
    val plantNames = remember(diaryEntries, historyList) {
        val names = mutableSetOf<String>()
        diaryEntries.forEach { names.add(it.plantName) }
        historyList.forEach { names.add(it.plantName) }
        listOf("All Plants") + names.sorted()
    }

    // Process 7-day data points
    val weeklyPoints = remember(diaryEntries, historyList, selectedPlantName) {
        computeWeeklyDataPoints(diaryEntries, historyList, selectedPlantName)
    }

    var selectedPointIndex by remember(weeklyPoints) { mutableIntStateOf(weeklyPoints.lastIndex) }
    val selectedPoint = weeklyPoints.getOrNull(selectedPointIndex)

    // Compute stats
    val avgHealth = remember(weeklyPoints) {
        val validScores = weeklyPoints.map { it.healthScore }
        if (validScores.isNotEmpty()) validScores.average().roundToInt() else 85
    }

    val healthDelta = remember(weeklyPoints) {
        if (weeklyPoints.size >= 2) {
            val first = weeklyPoints.first().healthScore
            val last = weeklyPoints.last().healthScore
            last - first
        } else 0
    }

    val maxHeights = remember(weeklyPoints) {
        weeklyPoints.mapNotNull { it.heightCm }
    }
    val currentHeight = maxHeights.lastOrNull() ?: 18.0f
    val heightDelta = if (maxHeights.size >= 2) (maxHeights.last() - maxHeights.first()) else 3.0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_health_graph_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShowChart,
                            contentDescription = "Health Trends",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Weekly Health & Growth",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "7-Day Plant Vitality Index",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onAddLogClick != null) {
                    IconButton(
                        onClick = onAddLogClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .size(36.dp)
                            .testTag("add_log_from_graph_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Observation",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Plant Selector Filter Chips
            if (plantNames.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(plantNames) { plant ->
                        FilterChip(
                            selected = (plant == selectedPlantName),
                            onClick = { selectedPlantName = plant },
                            label = { Text(plant, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (plant == selectedPlantName) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Summary Metric Banner Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Avg Health or Height
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedMetric == GraphMetric.HEALTH_SCORE) Icons.Filled.Favorite else Icons.Filled.Height,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (selectedMetric == GraphMetric.HEALTH_SCORE) "Avg Health" else "Height",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (selectedMetric == GraphMetric.HEALTH_SCORE) "$avgHealth%" else "%.1f cm".format(currentHeight),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Metric 2: Trend
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if ((if (selectedMetric == GraphMetric.HEALTH_SCORE) healthDelta else heightDelta.toInt()) >= 0) {
                        Color(0xFFE8F5E9)
                    } else Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if ((if (selectedMetric == GraphMetric.HEALTH_SCORE) healthDelta else heightDelta.toInt()) >= 0)
                                        Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if ((if (selectedMetric == GraphMetric.HEALTH_SCORE) healthDelta else heightDelta.toInt()) >= 0)
                                    Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "7-Day Trend",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (selectedMetric == GraphMetric.HEALTH_SCORE) {
                                    if (healthDelta >= 0) "+$healthDelta%" else "$healthDelta%"
                                } else {
                                    if (heightDelta >= 0) "+%.1f cm".format(heightDelta) else "%.1f cm".format(heightDelta)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if ((if (selectedMetric == GraphMetric.HEALTH_SCORE) healthDelta else heightDelta.toInt()) >= 0)
                                    Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Toggle Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedMetric == GraphMetric.HEALTH_SCORE) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                        .clickable { selectedMetric = GraphMetric.HEALTH_SCORE }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Health Score (%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedMetric == GraphMetric.HEALTH_SCORE) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedMetric == GraphMetric.HEALTH_SCORE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedMetric == GraphMetric.HEIGHT_GROWTH) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                        .clickable { selectedMetric = GraphMetric.HEIGHT_GROWTH }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Height Growth (cm)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedMetric == GraphMetric.HEIGHT_GROWTH) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedMetric == GraphMetric.HEIGHT_GROWTH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // The Canvas Graph
            val primaryColor = MaterialTheme.colorScheme.primary
            val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                CanvasGraphView(
                    points = weeklyPoints,
                    metric = selectedMetric,
                    selectedIndex = selectedPointIndex,
                    onSelectIndex = { selectedPointIndex = it },
                    primaryColor = primaryColor,
                    gridColor = gridColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tooltip / Selected Node Detail Card
            if (selectedPoint != null) {
                AnimatedVisibility(visible = true) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("graph_point_detail_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedPoint.healthScore >= 80) Color(0xFFE8F5E9)
                                        else if (selectedPoint.healthScore >= 50) Color(0xFFFFF8E1)
                                        else Color(0xFFFFEBEE)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${selectedPoint.healthScore}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selectedPoint.healthScore >= 80) Color(0xFF2E7D32)
                                    else if (selectedPoint.healthScore >= 50) Color(0xFFF57F17)
                                    else Color(0xFFD32F2F)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedPoint.dayLabel} • ${selectedPoint.fullDateStr}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (selectedPoint.heightCm != null) {
                                        Text(
                                            text = "Height: %.1f cm".format(selectedPoint.heightCm),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = selectedPoint.plantName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (!selectedPoint.note.isNull_or_blank()) {
                                    Text(
                                        text = "“${selectedPoint.note}”",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.trim().isEmpty()
}

@Composable
private fun CanvasGraphView(
    points: List<DailyHealthPoint>,
    metric: GraphMetric,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    primaryColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points, metric) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    val values = remember(points, metric) {
        points.map { pt ->
            if (metric == GraphMetric.HEALTH_SCORE) pt.healthScore.toFloat()
            else pt.heightCm ?: 15f
        }
    }

    val minY = remember(values, metric) {
        if (metric == GraphMetric.HEALTH_SCORE) 0f
        else (values.minOrNull() ?: 10f) - 3f
    }
    val maxY = remember(values, metric) {
        if (metric == GraphMetric.HEALTH_SCORE) 100f
        else (values.maxOrNull() ?: 25f) + 3f
    }

    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val axisTextStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Canvas(
        modifier = modifier
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val width = size.width
                    val paddingLeft = 32.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val graphWidth = width - paddingLeft - paddingRight
                    val stepX = if (points.size > 1) graphWidth / (points.size - 1) else graphWidth

                    var closestIndex = 0
                    var minDistance = Float.MAX_VALUE

                    for (i in points.indices) {
                        val nodeX = paddingLeft + i * stepX
                        val dist = kotlin.math.abs(offset.x - nodeX)
                        if (dist < minDistance) {
                            minDistance = dist
                            closestIndex = i
                        }
                    }
                    onSelectIndex(closestIndex)
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 36.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 28.dp.toPx()

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        // Draw horizontal grid lines (4 lines)
        val gridLinesCount = 4
        for (i in 0 until gridLinesCount) {
            val fraction = i.toFloat() / (gridLinesCount - 1)
            val y = paddingTop + graphHeight * (1f - fraction)
            val gridVal = minY + (maxY - minY) * fraction

            // Gridline
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Y Axis Label
            val labelStr = if (metric == GraphMetric.HEALTH_SCORE) "${gridVal.toInt()}%" else "%.0f".format(gridVal)
            val textLayoutResult = textMeasurer.measure(labelStr, axisTextStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(paddingLeft - textLayoutResult.size.width - 6.dp.toPx(), y - textLayoutResult.size.height / 2f)
            )
        }

        if (points.isEmpty()) return@Canvas

        val stepX = if (points.size > 1) graphWidth / (points.size - 1) else graphWidth

        // Compute screen coordinates for each point
        val pointCoords = points.indices.map { i ->
            val x = paddingLeft + i * stepX
            val rawVal = values[i]
            val valFraction = ((rawVal - minY) / (maxY - minY)).coerceIn(0f, 1f)
            val y = paddingTop + graphHeight * (1f - valFraction * animProgress.value)
            Offset(x, y)
        }

        // Draw Area Path
        val areaPath = Path().apply {
            moveTo(pointCoords.first().x, paddingTop + graphHeight)
            lineTo(pointCoords.first().x, pointCoords.first().y)

            for (i in 0 until pointCoords.size - 1) {
                val p0 = pointCoords[i]
                val p1 = pointCoords[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y
                cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }

            lineTo(pointCoords.last().x, paddingTop + graphHeight)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.02f)
                ),
                startY = paddingTop,
                endY = paddingTop + graphHeight
            )
        )

        // Draw Smooth Line Path
        val linePath = Path().apply {
            moveTo(pointCoords.first().x, pointCoords.first().y)
            for (i in 0 until pointCoords.size - 1) {
                val p0 = pointCoords[i]
                val p1 = pointCoords[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlY1 = p0.y
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                val controlY2 = p1.y
                cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            }
        }

        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw Nodes and X-Axis Labels
        pointCoords.forEachIndexed { i, coord ->
            val pt = points[i]
            val isSelected = (i == selectedIndex)

            // X-Axis Day Label
            val dayLayout = textMeasurer.measure(pt.dayLabel, axisTextStyle.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) primaryColor else axisTextStyle.color
            ))
            drawText(
                textLayoutResult = dayLayout,
                topLeft = Offset(coord.x - dayLayout.size.width / 2f, height - paddingBottom + 6.dp.toPx())
            )

            if (isSelected) {
                // Vertical selection guide line
                drawLine(
                    color = primaryColor.copy(alpha = 0.4f),
                    start = Offset(coord.x, paddingTop),
                    end = Offset(coord.x, paddingTop + graphHeight),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Halo glow ring
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = 12.dp.toPx(),
                    center = coord
                )

                // Outer circle
                drawCircle(
                    color = primaryColor,
                    radius = 7.dp.toPx(),
                    center = coord
                )

                // Inner white dot
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = coord
                )
            } else {
                // Regular node circle
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = coord
                )
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = coord,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

// Helper calculation to group diary and scan records into a 7-day timeline
private fun computeWeeklyDataPoints(
    diaryEntries: List<PlantDiaryEntry>,
    historyList: List<DiagnosisResult>,
    selectedPlantName: String
): List<DailyHealthPoint> {
    val cal = Calendar.getInstance()
    val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
    val sdfDate = SimpleDateFormat("MMM d", Locale.getDefault())

    val points = mutableListOf<DailyHealthPoint>()

    // We want 7 days leading up to today (Day -6 to Day 0)
    for (dayOffset in 6 downTo 0) {
        val targetCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -dayOffset)
        }
        val dayLabel = if (dayOffset == 0) "Today" else sdfDay.format(targetCal.time)
        val fullDateStr = sdfDate.format(targetCal.time)

        // Filter entries for this day
        val dayStart = targetCal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dayEnd = targetCal.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // Matching diary entries
        val matchingDiaries = diaryEntries.filter { entry ->
            val matchesPlant = (selectedPlantName == "All Plants" || entry.plantName.equals(selectedPlantName, ignoreCase = true))
            val matchesTime = (entry.timestamp in dayStart..dayEnd) || (dayOffset == 0 && entry.dateFormatted == "Today")
            matchesPlant && matchesTime
        }

        // Matching diagnosis scans
        val matchingScans = historyList.filter { scan ->
            val matchesPlant = (selectedPlantName == "All Plants" || scan.plantName.equals(selectedPlantName, ignoreCase = true))
            val matchesTime = (scan.timestamp in dayStart..dayEnd)
            matchesPlant && matchesTime
        }

        // Calculate health score & height for this day
        var computedHealth: Int
        var computedHeight: Float? = null
        var note: String? = null
        var plantName = if (selectedPlantName != "All Plants") selectedPlantName else "Garden Collection"
        var hasLog = false

        if (matchingDiaries.isNotEmpty() || matchingScans.isNotEmpty()) {
            hasLog = true
            val healthScores = mutableListOf<Int>()

            matchingDiaries.forEach { d ->
                // Rating 1-5 maps to 20-100%
                val score = (d.healthRating * 20).coerceIn(20, 100)
                healthScores.add(score)
                if (d.heightCm != null) computedHeight = d.heightCm
                if (!d.notes.isNull_or_blank()) note = d.notes
                plantName = d.plantName
            }

            matchingScans.forEach { s ->
                healthScores.add(s.healthScore)
                if (note == null) note = "${s.diseaseName} (${s.diseaseSeverity} risk)"
                if (selectedPlantName == "All Plants") plantName = s.plantName
            }

            computedHealth = healthScores.average().roundToInt().coerceIn(0, 100)
        } else {
            // Default baseline projection curve if day has no explicit entries
            val baselineCurve = listOf(72, 75, 78, 82, 86, 92, 96)
            computedHealth = baselineCurve.getOrElse(6 - dayOffset) { 85 }
            computedHeight = 14.5f + (6 - dayOffset) * 0.5f
            note = if (dayOffset == 0) "Routine foliage inspection" else "Stable vitality score"
        }

        points.add(
            DailyHealthPoint(
                dayLabel = dayLabel,
                fullDateStr = fullDateStr,
                timestamp = dayStart,
                healthScore = computedHealth,
                heightCm = computedHeight,
                plantName = plantName,
                note = note,
                hasLog = hasLog
            )
        )
    }

    return points
}
