package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.ui.viewmodel.ProblemDetailUiState
import com.lifeproblemsolver.app.ui.viewmodel.ProblemDetailViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import androidx.compose.ui.res.painterResource
import com.lifeproblemsolver.app.R
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailScreen(
    problemId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ProblemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle navigation back when problem is deleted
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            Log.d("ProblemDetailScreen", "Navigating back after delete")
            onNavigateBack()
            viewModel.onNavigateBackHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Problem Details") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Log.d("ProblemDetailScreen", "Back button pressed")
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.problem?.let { problem ->
                        if (!problem.isResolved) {
                            IconButton(onClick = { viewModel.markAsResolved() }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Mark as Resolved")
                            }
                        }
                        IconButton(onClick = { viewModel.deleteProblem() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
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
                    onRetry = { /* Reload problem */ },
                    onClearError = { viewModel.clearError() }
                )
            }
            uiState.problem != null -> {
                ProblemDetailContent(
                    problem = uiState.problem!!,
                    isGeneratingAi = uiState.isGeneratingAi,
                    onGenerateAiSolution = { viewModel.generateAiSolution() },
                    hasReachedRateLimit = uiState.hasReachedRateLimit,
                    currentRequestCount = uiState.currentRequestCount,
                    hasUserApiKey = uiState.hasUserApiKey
                )
            }
        }
    }
}

@Composable
private fun ProblemDetailContent(
    problem: com.lifeproblemsolver.app.data.model.Problem,
    isGeneratingAi: Boolean,
    onGenerateAiSolution: () -> Unit,
    hasReachedRateLimit: Boolean = false,
    currentRequestCount: Int = 0,
    hasUserApiKey: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Problem header
        ProblemHeader(problem = problem)
        
        // Problem details
        ProblemDetails(problem = problem)
        
        // AI Solution section
        AiSolutionSection(
            problem = problem,
            isGeneratingAi = isGeneratingAi,
            onGenerateAiSolution = onGenerateAiSolution,
            hasReachedRateLimit = hasReachedRateLimit,
            currentRequestCount = currentRequestCount,
            hasUserApiKey = hasUserApiKey
        )
    }
}

@Composable
private fun ProblemHeader(problem: com.lifeproblemsolver.app.data.model.Problem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = problem.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityChip(priority = problem.priority)
                
                if (problem.category.isNotBlank()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(problem.category) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = getCategoryIconRes(problem.category)),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                if (problem.isResolved) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Resolved") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProblemDetails(problem: com.lifeproblemsolver.app.data.model.Problem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description
            DetailSection(
                title = "Description",
                icon = Icons.Default.Description,
                content = problem.description
            )
            
            // Notes section removed - notes field not in current Problem model
            // if (problem.notes.isNotBlank()) {
            //     DetailSection(
            //         title = "Notes",
            //         icon = Icons.Default.Note,
            //         content = problem.notes
            //     )
            // }
            
            // Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailSection(
                    title = "Created",
                    icon = Icons.Default.Schedule,
                    content = formatDate(problem.createdAt),
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                DetailSection(
                    title = "Updated",
                    icon = Icons.Default.Update,
                    content = formatDate(problem.updatedAt),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AiSolutionSection(
    problem: com.lifeproblemsolver.app.data.model.Problem,
    isGeneratingAi: Boolean,
    onGenerateAiSolution: () -> Unit,
    hasReachedRateLimit: Boolean = false,
    currentRequestCount: Int = 0,
    hasUserApiKey: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "AI Solution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Rate limit alert
            if (hasReachedRateLimit && !hasUserApiKey) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column {
                            Text(
                                text = "Rate Limit Reached",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "You've used $currentRequestCount/5 free requests. Add your own API key in settings for unlimited usage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            if (problem.aiSolution.isNullOrBlank()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No AI solution generated yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = onGenerateAiSolution,
                        enabled = !isGeneratingAi && !hasReachedRateLimit
                    ) {
                        if (isGeneratingAi) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Solution")
                        }
                    }
                }
            } else {
                Text(
                    text = problem.aiSolution ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Button(
                    onClick = onGenerateAiSolution,
                    enabled = !isGeneratingAi && !hasReachedRateLimit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGeneratingAi) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regenerating...")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regenerate Solution")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getCategoryIconRes(category: String): Int = when (category.trim().lowercase()) {
    "work" -> R.drawable.ic_category_work
    "health" -> R.drawable.ic_category_health
    "relationship" -> R.drawable.ic_category_relationship
    "finance" -> R.drawable.ic_category_finance
    "education" -> R.drawable.ic_category_education
    else -> R.drawable.ic_category_other
}

private fun getPriorityIconRes(priority: Priority): Int = when (priority) {
    Priority.LOW -> R.drawable.ic_priority_low
    Priority.MEDIUM -> R.drawable.ic_priority_medium
    Priority.HIGH -> R.drawable.ic_priority_high
    Priority.URGENT -> R.drawable.ic_priority_urgent
}

@Composable
private fun PriorityChip(priority: Priority) {
    val (color, text) = when (priority) {
        Priority.LOW -> Pair(MaterialTheme.colorScheme.tertiary, "Low")
        Priority.MEDIUM -> Pair(MaterialTheme.colorScheme.primary, "Medium")
        Priority.HIGH -> Pair(MaterialTheme.colorScheme.error, "High")
        Priority.URGENT -> Pair(MaterialTheme.colorScheme.error, "Urgent")
    }
    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = getPriorityIconRes(priority)),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        }
    )
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

private fun formatDate(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    return localDateTime.format(formatter)
} 