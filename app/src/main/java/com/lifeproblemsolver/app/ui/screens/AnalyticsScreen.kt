package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeproblemsolver.app.ui.components.*
import com.lifeproblemsolver.app.ui.theme.*
import com.lifeproblemsolver.app.ui.viewmodel.AnalyticsViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            PremiumTopBar(
                title = "Value Analytics",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                // Header Card
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = PrimaryGradient
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How This App Adds Value",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Track your progress and see the impact",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                // Average Rating Card
                if (uiState.ratedProblemsCount > 0) {
                    ValueMetricCard(
                        title = "Average Solution Rating",
                        value = String.format("%.1f", uiState.averageRating),
                        subtitle = "${uiState.ratedProblemsCount} solutions rated",
                        icon = Icons.Default.Star,
                        gradient = AccentGradient,
                        additionalInfo = when {
                            uiState.averageRating >= 4.5f -> "Excellent! Solutions are highly effective"
                            uiState.averageRating >= 4.0f -> "Great! Solutions are working well"
                            uiState.averageRating >= 3.5f -> "Good! Solutions are helpful"
                            else -> "Keep tracking to improve"
                        }
                    )
                }
                
                // Problems Solved Card
                ValueMetricCard(
                    title = "Problems with Solutions",
                    value = "${uiState.problemsWithSolutionCount}",
                    subtitle = "Out of ${uiState.totalProblemsCount} total problems",
                    icon = Icons.Default.Psychology,
                    gradient = SecondaryGradient,
                    additionalInfo = if (uiState.totalProblemsCount > 0) {
                        "${(uiState.problemsWithSolutionCount * 100 / uiState.totalProblemsCount)}% of problems have AI solutions"
                    } else {
                        "Start adding problems to see your progress"
                    }
                )
                
                // Resolved Problems Card
                ValueMetricCard(
                    title = "Resolved Problems",
                    value = "${uiState.resolvedProblemsCount}",
                    subtitle = "Out of ${uiState.totalProblemsCount} total problems",
                    icon = Icons.Default.CheckCircle,
                    gradient = SuccessGradient,
                    additionalInfo = if (uiState.totalProblemsCount > 0) {
                        "${(uiState.resolvedProblemsCount * 100 / uiState.totalProblemsCount)}% resolution rate"
                    } else {
                        "Mark problems as resolved when you solve them"
                    }
                )
                
                // High Rating Solutions Card
                if (uiState.highRatingCount > 0) {
                    ValueMetricCard(
                        title = "Highly Rated Solutions",
                        value = "${uiState.highRatingCount}",
                        subtitle = "Solutions rated 4+ stars",
                        icon = Icons.Default.ThumbUp,
                        gradient = AccentGradient,
                        additionalInfo = "${(uiState.highRatingCount * 100 / uiState.ratedProblemsCount)}% of rated solutions are highly effective"
                    )
                }
                
                // Summary Card
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = PrimaryGradient
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = buildString {
                                append("You've tracked ${uiState.totalProblemsCount} problems. ")
                                if (uiState.problemsWithSolutionCount > 0) {
                                    append("${uiState.problemsWithSolutionCount} have AI-generated solutions. ")
                                }
                                if (uiState.ratedProblemsCount > 0) {
                                    append("Your average solution rating is ${String.format("%.1f", uiState.averageRating)}/5.0. ")
                                }
                                if (uiState.resolvedProblemsCount > 0) {
                                    append("You've resolved ${uiState.resolvedProblemsCount} problems.")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValueMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    additionalInfo: String
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = gradient
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = additionalInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

