package com.lifeproblemsolver.app.ui.navigation

sealed class Screen(val route: String) {
    object ProblemList : Screen("problem_list")
    object Calendar : Screen("calendar")
    object AddProblem : Screen("add_problem")
    object ProblemDetail : Screen("problem_detail/{problemId}") {
        fun createRoute(problemId: Long) = "problem_detail/$problemId"
    }
    object ApiKeySettings : Screen("api_key_settings")
    object WeekendCalendar : Screen("weekend_calendar")
} 