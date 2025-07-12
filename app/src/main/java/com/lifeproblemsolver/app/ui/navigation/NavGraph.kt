package com.lifeproblemsolver.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeproblemsolver.app.ui.screens.AddProblemScreen
import com.lifeproblemsolver.app.ui.screens.ApiKeySettingsScreen
import com.lifeproblemsolver.app.ui.screens.ProblemDetailScreen
import com.lifeproblemsolver.app.ui.screens.ProblemListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ProblemList.route,
        modifier = modifier
    ) {
        composable(Screen.ProblemList.route) {
            ProblemListScreen(
                onNavigateToAddProblem = { navController.navigate(Screen.AddProblem.route) },
                onProblemDetailNav = { problemId -> navController.navigate(Screen.ProblemDetail.createRoute(problemId)) },
                onNavigateToSettings = { navController.navigate(Screen.ApiKeySettings.route) },
                viewModel = hiltViewModel()
            )
        }
        
        composable(Screen.AddProblem.route) {
            AddProblemScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProblemDetailNav = { problemId -> navController.navigate(Screen.ProblemDetail.createRoute(problemId)) }
            )
        }
        
        composable(
            route = Screen.ProblemDetail.route,
            arguments = listOf(
                navArgument("problemId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getLong("problemId") ?: 0L
            ProblemDetailScreen(
                problemId = problemId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ApiKeySettings.route) {
            ApiKeySettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 