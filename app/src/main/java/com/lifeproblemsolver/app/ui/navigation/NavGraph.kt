package com.lifeproblemsolver.app.ui.navigation

import android.annotation.SuppressLint
import android.util.Log
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
import com.lifeproblemsolver.app.ui.screens.AddProblemScreen
import com.lifeproblemsolver.app.ui.screens.ApiKeySettingsScreen
import com.lifeproblemsolver.app.ui.screens.CalendarScreen
import com.lifeproblemsolver.app.ui.screens.MainScreen
import com.lifeproblemsolver.app.ui.screens.OnboardingScreen
import com.lifeproblemsolver.app.ui.screens.ProblemDetailScreen
import com.lifeproblemsolver.app.ui.screens.WeekendCalendarScreen
import com.lifeproblemsolver.app.ui.screens.ExcelExportScreen
import com.lifeproblemsolver.app.ui.screens.TrendScreen
import com.lifeproblemsolver.app.ui.screens.SplashScreen

class NavGraph {
    @SuppressLint("NotConstructor")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NavGraph(
        navController: NavHostController,
        modifier: Modifier = Modifier
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = modifier
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.ProblemList.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Screen.ProblemList.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.ProblemList.route) {
                MainScreen(
                    onNavigateToAddProblem = { navController.navigate(Screen.AddProblem.route) },
                    onProblemDetailNav = { problemId -> 
                        Log.d("NavGraph", "Navigating to problem detail with ID: $problemId")
                        navController.navigate(Screen.ProblemDetail.createRoute(problemId)) 
                    },
                    onNavigateToSettings = { navController.navigate(Screen.ApiKeySettings.route) },
                    onNavigateToWeekendCalendar = { navController.navigate(Screen.WeekendCalendar.route) },
                    onNavigateToExcelExport = { navController.navigate(Screen.ExcelExport.route) },
                    onNavigateToTrends = { navController.navigate(Screen.Trends.route) }
                )
            }
            
            composable(Screen.AddProblem.route) {
                AddProblemScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToProblem = { problemId -> 
                        Log.d("NavGraph", "AddProblemScreen navigating to problem detail with ID: $problemId")
                        // Navigate to problem detail and clear the add problem screen from stack
                        navController.navigate(Screen.ProblemDetail.createRoute(problemId)) {
                            popUpTo(Screen.AddProblem.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.ProblemDetail.route,
                arguments = listOf(
                    navArgument("problemId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val problemId = backStackEntry.arguments?.getLong("problemId") ?: 0L
                Log.d("NavGraph", "ProblemDetailScreen received problemId: $problemId")
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
            
            composable(Screen.WeekendCalendar.route) {
                WeekendCalendarScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.ExcelExport.route) {
                ExcelExportScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Trends.route) {
                TrendScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
} 