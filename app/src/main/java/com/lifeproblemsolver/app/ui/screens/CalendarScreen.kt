package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeproblemsolver.app.data.model.Problem
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen(
    problems: List<Problem>,
    onNavigateToProblem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Month navigation header
        MonthHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )

        // Calendar grid
        CalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            problems = problems,
            onDateSelected = { selectedDate = it },
            onNavigateToProblem = onNavigateToProblem
        )

        // Selected date problems
        if (problems.any { it.createdAt.toLocalDate() == selectedDate }) {
            SelectedDateProblems(
                date = selectedDate,
                problems = problems.filter { it.createdAt.toLocalDate() == selectedDate },
                onNavigateToProblem = onNavigateToProblem
            )
        }
    }
}

@Composable
private fun MonthHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }

        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    problems: List<Problem>,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateToProblem: (Long) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val daysInMonth = lastDayOfMonth.dayOfMonth

    // Calculate the first day to display (including previous month's days)
    val firstDisplayDay = firstDayOfMonth.minusDays((firstDayOfWeek - 1).toLong())
    val totalDaysToDisplay = 42 // 6 weeks * 7 days

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Day of week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(totalDaysToDisplay) { index ->
                val date = firstDisplayDay.plusDays(index.toLong())
                val isCurrentMonth = date.month == currentMonth.month
                val isSelected = date == selectedDate
                val hasProblems = problems.any { it.createdAt.toLocalDate() == date }
                val dayProblems = problems.filter { it.createdAt.toLocalDate() == date }

                CalendarDay(
                    date = date,
                    isCurrentMonth = isCurrentMonth,
                    isSelected = isSelected,
                    hasProblems = hasProblems,
                    problemCount = dayProblems.size,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    hasProblems: Boolean,
    problemCount: Int,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasProblems) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    if (problemCount > 1) {
                        Text(
                            text = if (problemCount > 9) "9+" else problemCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDateProblems(
    date: LocalDate,
    problems: List<Problem>,
    onNavigateToProblem: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Problems on ${date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(problems) { problem ->
                    ProblemCard(
                        problem = problem,
                        onClick = { onNavigateToProblem(problem.id) },
                        showActions = false
                    )
                }
            }
        }
    }
}

@Composable
private fun ProblemCard(
    problem: Problem,
    onClick: () -> Unit,
    showActions: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = problem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category icon
                    Icon(
                        imageVector = when (problem.category.lowercase(Locale.getDefault())) {
                            "work" -> Icons.Default.Work
                            "health" -> Icons.Default.Favorite
                            "relationship" -> Icons.Default.People
                            "finance" -> Icons.Default.AttachMoney
                            "education" -> Icons.Default.School
                            else -> Icons.Default.Category
                        },
                        contentDescription = problem.category,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    // Priority indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (problem.priority) {
                                    com.lifeproblemsolver.app.data.model.Priority.URGENT -> Color.Red
                                    com.lifeproblemsolver.app.data.model.Priority.HIGH -> Color(0xFFFF6B35)
                                    com.lifeproblemsolver.app.data.model.Priority.MEDIUM -> Color(0xFFFFD93D)
                                    com.lifeproblemsolver.app.data.model.Priority.LOW -> Color(0xFF6BCF7F)
                                }
                            )
                    )
                }
            }

            if (problem.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = problem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
} 