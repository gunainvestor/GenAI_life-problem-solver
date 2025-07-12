package com.lifeproblemsolver.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.ui.components.*
import com.lifeproblemsolver.app.ui.theme.*
import com.lifeproblemsolver.app.ui.viewmodel.AddProblemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProblemScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProblem: (Long) -> Unit,
    viewModel: AddProblemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRateLimitAlert by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.createdProblemId > 0) {
            onNavigateToProblem(uiState.createdProblemId)
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            // Show error snackbar or handle error
        }
    }

    Scaffold(
        topBar = {
            PremiumTopBar(
                title = "Add Problem",
                onBackClick = onNavigateBack,
                actions = {
                    VoiceToTextComponent(
                        onTextReceived = { spokenText ->
                            // Add the spoken text to the description field
                            viewModel.updateDescription(uiState.description + (if (uiState.description.isNotBlank()) " " else "") + spokenText)
                        },
                        modifier = Modifier.size(48.dp)
                    )
                    IconButton(onClick = { /* Share or other action */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Problem Title
            PremiumTextFieldWithVoice(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = "Problem Title",
                placeholder = "Enter a clear, concise title for your problem",
                leadingIcon = Icons.Default.Title,
                modifier = Modifier.fillMaxWidth()
            )

            // Problem Description
            PremiumTextFieldWithVoice(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = "Problem Description",
                placeholder = "Describe your problem in detail...",
                leadingIcon = Icons.Default.Description,
                modifier = Modifier.fillMaxWidth()
            )

            // Category Selection
            PremiumCard(
                modifier = Modifier.fillMaxWidth(),
                gradient = SecondaryGradient
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    PremiumCategorySelector(
                        selectedCategory = uiState.category,
                        onCategorySelected = { viewModel.updateCategory(it) }
                    )
                }
            }

            // Priority Selection
            PremiumCard(
                modifier = Modifier.fillMaxWidth(),
                gradient = AccentGradient
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Priority Level",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    PremiumPrioritySelector(
                        selectedPriority = uiState.priority,
                        onPrioritySelected = { viewModel.updatePriority(it) }
                    )
                }
            }

            // AI Solution Generation
            if (uiState.title.isNotBlank() || uiState.description.isNotBlank()) {
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = PremiumGradient
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

                        if (uiState.aiSuggestion.isBlank()) {
                            PremiumButton(
                                onClick = { 
                                    if (uiState.hasReachedRateLimit && !uiState.hasUserApiKey) {
                                        showRateLimitAlert = true
                                    } else {
                                        viewModel.generateAiSolution()
                                    }
                                },
                                text = "Generate AI Solution",
                                icon = Icons.Default.AutoAwesome,
                                enabled = !uiState.isGeneratingAi
                            )
                        } else {
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
                                        text = uiState.aiSuggestion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        if (uiState.isGeneratingAi) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
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

            // Submit Button
            PremiumButton(
                text = "Submit Problem",
                onClick = { viewModel.saveProblem() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.title.isNotBlank() && uiState.description.isNotBlank()
            )
            
            if (uiState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Saving problem...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Rate limit alert dialog
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
private fun PremiumCategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "Personal", "Professional", "Health", "Financial", 
        "Relationships", "Education", "Technology", "Other"
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(120.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            val animatedElevation by animateFloatAsState(
                targetValue = if (isSelected) 6f else 2f,
                animationSpec = tween(200),
                label = "category_elevation"
            )
            
            Card(
                modifier = Modifier
                    .shadow(
                        elevation = animatedElevation.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = if (isSelected) PrimaryBlue.copy(alpha = 0.3f) else Color.Transparent
                    )
                    .background(
                        brush = if (isSelected) PrimaryGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumPrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Priority.values().forEach { priority ->
            val isSelected = selectedPriority == priority
            val animatedElevation by animateFloatAsState(
                targetValue = if (isSelected) 6f else 2f,
                animationSpec = tween(200),
                label = "priority_elevation"
            )
            
            val priorityGradient = when (priority) {
                Priority.HIGH -> Brush.linearGradient(listOf(ErrorRed, ErrorRedLight))
                Priority.MEDIUM -> Brush.linearGradient(listOf(AccentGold, AccentGoldLight))
                Priority.LOW -> Brush.linearGradient(listOf(SuccessGreen, SuccessGreenLight))
                Priority.URGENT -> Brush.linearGradient(listOf(ErrorRed, ErrorRedLight))
            }
            
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = animatedElevation.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = if (isSelected) ErrorRed.copy(alpha = 0.3f) else Color.Transparent
                    )
                    .background(
                        brush = if (isSelected) priorityGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPrioritySelected(priority) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = priority.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
} 