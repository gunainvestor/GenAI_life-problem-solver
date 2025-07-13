package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeproblemsolver.app.data.model.WeekendCalendar
import com.lifeproblemsolver.app.ui.components.PremiumCard
import com.lifeproblemsolver.app.ui.components.PremiumTopBar
import com.lifeproblemsolver.app.ui.theme.*
import com.lifeproblemsolver.app.ui.viewmodel.WeekendCalendarViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekendCalendarScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeekendCalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showNoteDialog by remember { mutableStateOf(false) }
    var selectedWeekendForNote by remember { mutableStateOf<WeekendCalendar?>(null) }
    var noteText by remember { mutableStateOf("") }
    
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
        // Top Bar
        PremiumTopBar(
            title = "Weekend Calendar",
            onBackClick = onNavigateBack
        )
        
        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            // Group weekends by year and month
            val weekendsByMonth = uiState.weekends.groupBy { YearMonth.from(it.date) }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    PremiumCard(
                        modifier = Modifier.fillMaxWidth(),
                        gradient = PrimaryGradient
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Weekend,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Weekend Planner",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Plan your weekends for the next 5 years. Tap to select or deselect weekends. Long press to add notes.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                // Stats Card
                item {
                    val selectedCount = uiState.weekends.count { it.isSelected }
                    val totalCount = uiState.weekends.size
                    PremiumCard(
                        modifier = Modifier.fillMaxWidth(),
                        gradient = SecondaryGradient
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                title = "Selected",
                                value = selectedCount.toString(),
                                icon = Icons.Default.CheckCircle
                            )
                            StatItem(
                                title = "Total",
                                value = totalCount.toString(),
                                icon = Icons.Default.CalendarMonth
                            )
                        }
                    }
                }
                // Error Message
                if (uiState.error != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = uiState.error!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { viewModel.clearError() }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close"
                                    )
                                }
                            }
                        }
                    }
                }
                // Month-wise calendar
                weekendsByMonth.forEach { (yearMonth, weekends) ->
                    if (weekends.isNotEmpty()) {
                        item {
                            MonthHeader(
                                year = yearMonth.year,
                                month = yearMonth.monthValue,
                                weekendCount = weekends.size
                            )
                        }
                        item {
                            val rows = ceil(weekends.size / 7.0).toInt()
                            val gridHeight = (rows * 56 + (rows - 1) * 4).dp
                            
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(gridHeight)
                            ) {
                                items(weekends) { weekend ->
                                    WeekendDayCell(
                                        weekend = weekend,
                                        onClick = { viewModel.toggleWeekendSelection(context, weekend.date) },
                                        onLongClick = {
                                            selectedWeekendForNote = weekend
                                            noteText = weekend.note
                                            showNoteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Note Dialog
    if (showNoteDialog && selectedWeekendForNote != null) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    text = "Add Note for ${selectedWeekendForNote!!.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedWeekendForNote?.let { weekend ->
                            viewModel.updateWeekendNote(context, weekend.date, noteText)
                        }
                        showNoteDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNoteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun WeekendDayCell(
    weekend: WeekendCalendar,
    onClick: () -> Unit,
    onLongClick: (Offset) -> Unit
) {
    val isSelected = weekend.isSelected
    val isToday = weekend.date == LocalDate.now()
    
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable { onClick() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { offset -> onLongClick(offset) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = weekend.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            Text(
                text = weekend.date.month.name.take(3),
                style = MaterialTheme.typography.labelSmall,
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                }
            )
            
            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) { }
            }
            
            // Note indicator
            if (weekend.note.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) { }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    year: Int,
    month: Int,
    weekendCount: Int
) {
    val monthName = java.time.Month.of(month).getDisplayName(
        java.time.format.TextStyle.FULL,
        java.util.Locale.getDefault()
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Weekend,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$weekendCount weekends",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 