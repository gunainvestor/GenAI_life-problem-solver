package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.ui.components.ProblemCard
import com.lifeproblemsolver.app.ui.viewmodel.FilterType
import com.lifeproblemsolver.app.ui.viewmodel.ProblemListUiState
import com.lifeproblemsolver.app.ui.viewmodel.ProblemListViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemListScreen(
    onNavigateToAddProblem: () -> Unit = {},
    onProblemDetailNav: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: ProblemListViewModel = hiltViewModel(),
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("Life Problem Solver") },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showTopBar) {
                FloatingActionButton(onClick = onNavigateToAddProblem) {
                    Icon(Icons.Default.Add, contentDescription = "Add Problem")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            FilterChips(
                filterType = filterType,
                onFilterSelected = { viewModel.loadProblemsByFilter(it) }
            )

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadProblems() },
                        onClearError = { viewModel.clearError() }
                    )
                }
                uiState.problems.isEmpty() -> {
                    EmptyState(onAddProblem = onNavigateToAddProblem)
                }
                else -> {
                    ProblemList(
                        problems = uiState.problems,
                        onProblemClick = onProblemDetailNav,
                        onDeleteProblem = { viewModel.deleteProblem(it) }
                    )
                }
            }
        }

        // Filter dialog
        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onFilterSelected = { filterType ->
                    viewModel.loadProblemsByFilter(filterType)
                    showFilterDialog = false
                }
            )
        }
    }
}

@Composable
private fun FilterChips(
    filterType: FilterType,
    onFilterSelected: (FilterType) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FilterType.values()) { filter ->
            FilterChip(
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) },
                selected = filterType == filter,
                leadingIcon = {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun ProblemList(
    problems: List<Problem>,
    onProblemClick: (Long) -> Unit,
    onDeleteProblem: (Problem) -> Unit
) {
    // Sort problems by priority: URGENT -> HIGH -> MEDIUM -> LOW
    val sortedProblems = problems.sortedBy { problem ->
        when (problem.priority) {
            Priority.URGENT -> 0
            Priority.HIGH -> 1
            Priority.MEDIUM -> 2
            Priority.LOW -> 3
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sortedProblems) { problem ->
            SortedProblemCard(
                problem = problem,
                onClick = { onProblemClick(problem.id) },
                onDelete = { onDeleteProblem(problem) }
            )
        }
    }
}

@Composable
private fun EmptyState(onAddProblem: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No problems yet",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Start by adding your first life problem",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddProblem) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Problem")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onRetry) {
                    Text("Retry")
                }
                OutlinedButton(onClick = onClearError) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (FilterType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Problems") },
        text = {
            Column {
                FilterType.values().forEach { filterType ->
                    TextButton(
                        onClick = { onFilterSelected(filterType) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = filterType.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(filterType.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SortedProblemCard(
    problem: Problem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Get priority color for indicators only
    val priorityColor = when (problem.priority) {
        Priority.URGENT -> MaterialTheme.colorScheme.error
        Priority.HIGH -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> MaterialTheme.colorScheme.tertiary
        Priority.LOW -> MaterialTheme.colorScheme.secondary
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = problem.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = problem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority chip with matching color
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = priorityColor.copy(alpha = 0.2f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = priorityColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = when (problem.priority) {
                                    Priority.URGENT -> Icons.Default.Warning
                                    Priority.HIGH -> Icons.Default.PriorityHigh
                                    Priority.MEDIUM -> Icons.Default.Info
                                    Priority.LOW -> Icons.Default.LowPriority
                                },
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = priorityColor
                            )
                            Text(
                                text = problem.priority.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor
                            )
                        }
                    }
                    
                    // Category chip
                    if (problem.category.isNotBlank()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(problem.category) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                    
                    // Resolved status
                    if (problem.isResolved) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Resolved") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
                
                // Date
                Text(
                    text = formatDate(problem.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // AI solution preview
            if (problem.aiSolution?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "AI Solution Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Problem") },
            text = { Text("Are you sure you want to delete this problem? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDate(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    return localDateTime.format(formatter)
}

// Extension properties for FilterType
val FilterType.displayName: String
    get() = when (this) {
        FilterType.ALL -> "All"
        FilterType.RESOLVED -> "Resolved"
        FilterType.UNRESOLVED -> "Unresolved"
        FilterType.HIGH_PRIORITY -> "High Priority"
        FilterType.URGENT -> "Urgent"
    }

val FilterType.icon
    get() = when (this) {
        FilterType.ALL -> Icons.Default.List
        FilterType.RESOLVED -> Icons.Default.CheckCircle
        FilterType.UNRESOLVED -> Icons.Default.Pending
        FilterType.HIGH_PRIORITY -> Icons.Default.PriorityHigh
        FilterType.URGENT -> Icons.Default.Warning
    } 