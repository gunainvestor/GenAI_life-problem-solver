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
import com.lifeproblemsolver.app.ui.viewmodel.AddProblemUiState
import com.lifeproblemsolver.app.ui.viewmodel.AddProblemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProblemScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProblemDetail: (Long) -> Unit,
    viewModel: AddProblemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.createdProblemId > 0) {
            onNavigateToProblemDetail(uiState.createdProblemId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Problem") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                
                // Description field
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    isError = uiState.error?.contains("Description") == true
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
                
                // Category field
                OutlinedTextField(
                    value = uiState.category,
                    onValueChange = { viewModel.updateCategory(it) },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null)
                    }
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
                            
                            if (uiState.aiSuggestion.isBlank()) {
                                Button(
                                    onClick = { viewModel.generateAiSolution() },
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
                                    onClick = { viewModel.generateAiSolution() },
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
                        imageVector = when (priority) {
                            Priority.LOW -> Icons.Default.LowPriority
                            Priority.MEDIUM -> Icons.Default.PriorityHigh
                            Priority.HIGH -> Icons.Default.PriorityHigh
                            Priority.URGENT -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
} 