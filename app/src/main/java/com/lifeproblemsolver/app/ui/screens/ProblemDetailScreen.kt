package com.lifeproblemsolver.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.ui.components.*
import com.lifeproblemsolver.app.ui.theme.*
import com.lifeproblemsolver.app.ui.viewmodel.ProblemDetailUiState
import com.lifeproblemsolver.app.ui.viewmodel.ProblemDetailViewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import androidx.compose.ui.res.painterResource
import com.lifeproblemsolver.app.R
import android.util.Log
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailScreen(
    problemId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ProblemDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRateLimitAlert by remember { mutableStateOf(false) }
    
    // Handle navigation back when problem is deleted
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            Log.d("ProblemDetailScreen", "Navigating back after delete")
            onNavigateBack()
            viewModel.onNavigateBackHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        // Premium Top Bar
        PremiumTopBar(
            title = "Problem Details",
            onBackClick = {
                Log.d("ProblemDetailScreen", "Back button pressed")
                onNavigateBack()
            },
            actions = {
                uiState.problem?.let { problem ->
                    if (!problem.isResolved) {
                        IconButton(
                            onClick = { viewModel.markAsResolved() },
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    spotColor = SuccessGreen.copy(alpha = 0.3f)
                                )
                                .background(
                                    SuccessGradient,
                                    RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Mark as Resolved",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.deleteProblem() },
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(10.dp),
                                spotColor = ErrorRed.copy(alpha = 0.3f)
                            )
                            .background(
                                Brush.linearGradient(listOf(ErrorRed, ErrorRedLight)),
                                RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Loading problem...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PremiumButton(
                                text = "Retry",
                                onClick = { /* Reload problem */ }
                            )
                            PremiumButton(
                                text = "Clear",
                                onClick = { viewModel.clearError() }
                            )
                        }
                    }
                }
            }
            uiState.problem != null -> {
                PremiumProblemDetailContent(
                    problem = uiState.problem!!,
                    isGeneratingAi = uiState.isGeneratingAi,
                    onGenerateAiSolution = { 
                        if (uiState.hasReachedRateLimit && !uiState.hasUserApiKey) {
                            showRateLimitAlert = true
                        } else {
                            viewModel.generateAiSolution()
                        }
                    },
                    hasReachedRateLimit = uiState.hasReachedRateLimit,
                    currentRequestCount = uiState.currentRequestCount,
                    hasUserApiKey = uiState.hasUserApiKey
                )
            }
        }
    }
    
    // Rate Limit Alert Dialog
    if (showRateLimitAlert) {
        AlertDialog(
            onDismissRequest = { showRateLimitAlert = false },
            title = {
                Text(
                    text = "Rate Limit Reached",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "You've reached the free tier limit. Add your own API key in settings for unlimited AI solutions.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                PremiumButton(
                    onClick = { showRateLimitAlert = false },
                    text = "OK"
                )
            }
        )
    }
}

@Composable
private fun PremiumProblemDetailContent(
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Problem header
        PremiumProblemHeader(problem = problem)
        
        // Problem details
        PremiumProblemDetails(problem = problem)
        
        // AI Solution section
        PremiumAiSolutionSection(
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
private fun PremiumProblemHeader(problem: com.lifeproblemsolver.app.data.model.Problem) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = PrimaryGradient
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = problem.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    text = problem.priority.name,
                    status = when (problem.priority) {
                        Priority.HIGH -> StatusType.ERROR
                        Priority.MEDIUM -> StatusType.WARNING
                        Priority.LOW -> StatusType.SUCCESS
                        Priority.URGENT -> StatusType.ERROR
                    }
                )
                
                if (problem.isResolved) {
                    StatusChip(
                        text = "RESOLVED",
                        status = StatusType.SUCCESS
                    )
                }
                
                Text(
                    text = problem.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun PremiumProblemDetails(problem: com.lifeproblemsolver.app.data.model.Problem) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = SecondaryGradient
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Problem Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            if (problem.description.isNotBlank()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = problem.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Created",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = problem.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                
                if (problem.isResolved) {
                    Column {
                        Text(
                            text = "Resolved",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = problem.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumAiSolutionSection(
    problem: com.lifeproblemsolver.app.data.model.Problem,
    isGeneratingAi: Boolean,
    onGenerateAiSolution: () -> Unit,
    hasReachedRateLimit: Boolean = false,
    currentRequestCount: Int = 0,
    hasUserApiKey: Boolean = false
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = AccentGradient
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "AI Solution",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            // Rate limit alert
            if (hasReachedRateLimit && !hasUserApiKey) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Rate Limit Reached",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "You've used $currentRequestCount/5 free requests. Add your own API key in settings for unlimited usage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            if (problem.aiSolution?.isNotBlank() == true) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "AI Suggestion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = problem.aiSolution ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        PremiumButton(
                            onClick = onGenerateAiSolution,
                            text = "Regenerate Solution",
                            icon = Icons.Default.Refresh,
                            enabled = !isGeneratingAi,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            } else {
                PremiumButton(
                    onClick = onGenerateAiSolution,
                    text = "Generate AI Solution",
                    icon = Icons.Default.AutoAwesome,
                    enabled = !isGeneratingAi
                )
            }
            
            if (isGeneratingAi) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Generating AI solution...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
} 