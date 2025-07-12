package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeproblemsolver.app.ui.components.VoiceToTextComponent
import com.lifeproblemsolver.app.ui.viewmodel.ProblemListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToAddProblem: () -> Unit,
    onProblemDetailNav: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWeekendCalendar: () -> Unit,
    viewModel: ProblemListViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf(
        TabItem(
            title = "Problems",
            icon = Icons.Default.List,
            route = "problems"
        ),
        TabItem(
            title = "Calendar",
            icon = Icons.Default.CalendarMonth,
            route = "calendar"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Life Problem Solver") },
                actions = {
                    VoiceToTextComponent(
                        onTextReceived = { spokenText ->
                            // For now, just navigate to add problem
                            // In the future, this could pre-fill the form
                            onNavigateToAddProblem()
                        },
                        modifier = Modifier.size(48.dp)
                    )
                    IconButton(onClick = onNavigateToAddProblem) {
                        Icon(Icons.Default.Add, contentDescription = "Add Problem")
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        onNavigateToWeekendCalendar()
                                    },
                                    onTap = {
                                        onNavigateToSettings()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
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
        ) {
            // Tab row
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = tab.title) }
                    )
                }
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> {
                    ProblemListScreen(
                        onNavigateToAddProblem = onNavigateToAddProblem,
                        onProblemDetailNav = onProblemDetailNav,
                        onNavigateToSettings = onNavigateToSettings,
                        viewModel = viewModel,
                        showTopBar = false // Hide top bar since it's in MainScreen
                    )
                }
                1 -> {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    CalendarScreen(
                        problems = uiState.problems,
                        onNavigateToProblem = onProblemDetailNav
                    )
                }
            }
        }
    }
}

private data class TabItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) 