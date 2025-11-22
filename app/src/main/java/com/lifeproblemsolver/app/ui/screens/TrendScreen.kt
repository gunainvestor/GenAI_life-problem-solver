package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.ui.viewmodel.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrendViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Problem Trends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Time Period Dropdown
                    Box {
                        TextButton(
                            onClick = { expanded = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = uiState.selectedPeriod.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            TimePeriod.values().forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period.displayName) },
                                    onClick = {
                                        viewModel.loadTrends(period)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                TrendContent(
                    uiState = uiState,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun TrendContent(
    uiState: TrendUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Summary Card
        SummaryCard(
            totalProblems = uiState.totalProblems,
            resolved = uiState.statusTrend.resolved,
            unresolved = uiState.statusTrend.unresolved
        )

        // Daily Trend Chart
        if (uiState.dailyTrend.isNotEmpty()) {
            DailyTrendChart(
                title = "Problems Over Time",
                data = uiState.dailyTrend
            )
        }

        // Category Trend Chart
        if (uiState.categoryTrend.isNotEmpty()) {
            CategoryTrendChart(
                title = "Problems by Category",
                data = uiState.categoryTrend
            )
        }

        // Priority Trend Chart
        if (uiState.priorityTrend.isNotEmpty()) {
            PriorityTrendChart(
                title = "Problems by Priority",
                data = uiState.priorityTrend
            )
        }

        // Status Pie Chart (as bar chart since Vico doesn't have pie)
        StatusTrendChart(
            title = "Resolved vs Unresolved",
            resolved = uiState.statusTrend.resolved,
            unresolved = uiState.statusTrend.unresolved
        )
    }
}

@Composable
private fun SummaryCard(
    totalProblems: Int,
    resolved: Int,
    unresolved: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Total", totalProblems.toString())
                StatItem("Resolved", resolved.toString(), Color(0xFF4CAF50))
                StatItem("Unresolved", unresolved.toString(), Color(0xFFFF9800))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color ?: MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DailyTrendChart(
    title: String,
    data: List<DailyTrendData>
) {
    ChartCard(title = title) {
        val chartEntryModel = entryModelOf(
            *data.map { it.count.toFloat() }.toTypedArray()
        )

        Chart(
            model = chartEntryModel,
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                ),
                spacing = 24.dp
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                valueFormatter = AxisValueFormatter { value, _ ->
                    if (value.toInt() in data.indices) {
                        data[value.toInt()].date
                    } else {
                        ""
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}

@Composable
private fun CategoryTrendChart(
    title: String,
    data: List<CategoryTrendData>
) {
    ChartCard(title = title) {
        val chartEntryModel = entryModelOf(
            *data.map { it.count.toFloat() }.toTypedArray()
        )

        Chart(
            model = chartEntryModel,
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineColor = MaterialTheme.colorScheme.secondary
                    )
                ),
                spacing = 32.dp
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                valueFormatter = AxisValueFormatter { value, _ ->
                    if (value.toInt() in data.indices) {
                        data[value.toInt()].category
                    } else {
                        ""
                    }
                },
                labelRotationDegrees = -45f
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}

@Composable
private fun PriorityTrendChart(
    title: String,
    data: List<PriorityTrendData>
) {
    ChartCard(title = title) {
        val chartEntryModel = entryModelOf(
            *data.map { it.count.toFloat() }.toTypedArray()
        )

        Chart(
            model = chartEntryModel,
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineColor = MaterialTheme.colorScheme.tertiary
                    )
                ),
                spacing = 32.dp
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                valueFormatter = AxisValueFormatter { value, _ ->
                    if (value.toInt() in data.indices) {
                        data[value.toInt()].priority.name
                    } else {
                        ""
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}

@Composable
private fun StatusTrendChart(
    title: String,
    resolved: Int,
    unresolved: Int
) {
    ChartCard(title = title) {
        val chartEntryModel = entryModelOf(
            resolved.toFloat(), unresolved.toFloat()
        )

        Chart(
            model = chartEntryModel,
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                ),
                spacing = 48.dp
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                valueFormatter = AxisValueFormatter { value, _ ->
                    when (value.toInt()) {
                        0 -> "Resolved"
                        1 -> "Unresolved"
                        else -> ""
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Add labels below chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF4CAF50))
                )
                Text("Resolved: $resolved", style = MaterialTheme.typography.bodySmall)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF9800))
                )
                Text("Unresolved: $unresolved", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

