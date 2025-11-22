package com.lifeproblemsolver.app.data.preferences

data class OnboardingState(
    val hasCompleted: Boolean = false,
    val dailyDecisionMinutes: Int = 0,
    val weeklyDecisionMinutes: Int = 0,
    val reclaimFocusIntent: String = ""
)

data class OnboardingResult(
    val dailyDecisionMinutes: Int,
    val weeklyDecisionMinutes: Int,
    val reclaimFocusIntent: String
)



