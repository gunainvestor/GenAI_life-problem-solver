package com.lifeproblemsolver.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.Priority
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.res.painterResource
import com.lifeproblemsolver.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemCard(
    problem: Problem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = problem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
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
                    // Priority chip
                    PriorityChip(priority = problem.priority)
                    
                    // Category chip
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
                    
                    // Resolved status
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
                        modifier = Modifier.size(16.dp),
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

private fun formatDate(localDateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    return localDateTime.format(formatter)
} 