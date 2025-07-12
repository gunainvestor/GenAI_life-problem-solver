package com.lifeproblemsolver.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lifeproblemsolver.app.ui.screens.AddProblemScreen
import com.lifeproblemsolver.app.ui.screens.ProblemDetailScreen
import com.lifeproblemsolver.app.ui.screens.ProblemListScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.ProblemList.route
    ) {
        composable(Screen.ProblemList.route) {
            ProblemListScreen(
                onNavigateToAddProblem = {
                    navController.navigate(Screen.AddProblem.route)
                },
                onNavigateToProblemDetail = { problemId ->
                    navController.navigate(Screen.ProblemDetail.createRoute(problemId))
                }
            )
        }
        
        composable(Screen.AddProblem.route) {
            AddProblemScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProblemDetail = { problemId ->
                    navController.navigate(Screen.ProblemDetail.createRoute(problemId)) {
                        popUpTo(Screen.ProblemList.route)
                    }
                }
            )
        }
        
        composable(
            route = Screen.ProblemDetail.route,
            arguments = listOf(
                navArgument("problemId") {
                    type = NavType.LongType
                }
            )
        ) {
            ProblemDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object ProblemList : Screen("problem_list")
    object AddProblem : Screen("add_problem")
    object ProblemDetail : Screen("problem_detail/{problemId}") {
        fun createRoute(problemId: Long) = "problem_detail/$problemId"
    }
} 