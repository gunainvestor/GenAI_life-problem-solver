package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridCells
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
import com.lifeproblemsolver.app.ui.components.VoiceToTextComponent
import com.lifeproblemsolver.app.ui.viewmodel.AddProblemUiState
import com.lifeproblemsolver.app.ui.viewmodel.AddProblemViewModel
import androidx.compose.ui.res.painterResource
import com.lifeproblemsolver.app.R
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProblemScreen(
    onNavigateBack: () -> Unit,
    onProblemDetailNav: (Long) -> Unit,
    viewModel: AddProblemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRateLimitAlert by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.createdProblemId > 0) {
            Log.d("AddProblemScreen", "Navigating to problem detail with ID: ${uiState.createdProblemId}")
            onProblemDetailNav(uiState.createdProblemId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Problem") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("AddProblemScreen", "Back button pressed")
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title field
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Problem Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error?.contains("Title") == true
                )
                
                // Description field with voice input
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    isError = uiState.error?.contains("Description") == true,
                    trailingIcon = {
                        VoiceToTextComponent(
                            onTextReceived = { spokenText ->
                                viewModel.appendToDescription(spokenText)
                            }
                        )
                    }
                )
                
                // Notes field
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                
                // Category selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                CategorySelector(
                    selectedCategory = uiState.category,
                    onCategorySelected = { viewModel.updateCategory(it) }
                )
                
                // Priority selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                PrioritySelector(
                    selectedPriority = uiState.priority,
                    onPrioritySelected = { viewModel.updatePriority(it) }
                )
                
                // AI Solution Generation
                if (uiState.title.isNotBlank() || uiState.description.isNotBlank()) {
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
                            if (uiState.hasReachedRateLimit && !uiState.hasUserApiKey) {
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
                                                text = "You've used ${uiState.currentRequestCount}/5 free requests. Add your own API key in settings for unlimited usage.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                            
                            if (uiState.aiSuggestion.isBlank()) {
                                Button(
                                    onClick = { 
                                        if (uiState.hasReachedRateLimit && !uiState.hasUserApiKey) {
                                            showRateLimitAlert = true
                                        } else {
                                            viewModel.generateAiSolution()
                                        }
                                    },
                                    enabled = !uiState.isGeneratingAi,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (uiState.isGeneratingAi) {
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
                            } else {
                                Text(
                                    text = uiState.aiSuggestion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Button(
                                    onClick = { 
                                        if (uiState.hasReachedRateLimit && !uiState.hasUserApiKey) {
                                            showRateLimitAlert = true
                                        } else {
                                            viewModel.generateAiSolution()
                                        }
                                    },
                                    enabled = !uiState.isGeneratingAi,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (uiState.isGeneratingAi) {
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
                
                // Error message
                if (uiState.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                // Save button
                Button(
                    onClick = { viewModel.saveProblem() },
                    enabled = !uiState.isLoading && uiState.title.isNotBlank() && uiState.description.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Problem")
                    }
                }
            }
        }
    }
    
    // Rate Limit Alert Dialog
    if (showRateLimitAlert) {
        AlertDialog(
            onDismissRequest = { showRateLimitAlert = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Rate Limit Reached")
            },
            text = {
                Text(
                    "You've used ${uiState.currentRequestCount}/5 free AI requests. " +
                    "To continue using AI features, please add your own OpenAI API key in the settings."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showRateLimitAlert = false }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRateLimitAlert = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("Work", "Health", "Relationship", "Finance", "Education", "Other")
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(120.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                selected = selectedCategory == category,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = getCategoryIconRes(category)),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Priority.values().forEach { priority ->
            FilterChip(
                onClick = { onPrioritySelected(priority) },
                label = { Text(priority.name.lowercase().capitalize()) },
                selected = selectedPriority == priority,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = getPriorityIconRes(priority)),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
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

private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
} 