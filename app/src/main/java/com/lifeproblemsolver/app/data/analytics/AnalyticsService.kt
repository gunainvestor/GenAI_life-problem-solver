package com.lifeproblemsolver.app.data.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor() {
    
    private val analytics: FirebaseAnalytics = Firebase.analytics
    
    fun logProblemAdded(category: String, priority: String) {
        analytics.logEvent("problem_added") {
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, "problem_added")
            param("problem_category", category)
            param("problem_priority", priority)
        }
    }
    
    fun logProblemSolved(problemId: Long, category: String) {
        analytics.logEvent("problem_solved") {
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, "problem_solved")
            param("problem_id", problemId.toString())
            param("problem_category", category)
        }
    }
    
    fun logAiSolutionRequested(category: String) {
        analytics.logEvent("ai_solution_requested") {
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, "ai_solution_requested")
            param("problem_category", category)
        }
    }
    
    fun logApiKeyAdded() {
        analytics.logEvent("api_key_added") {
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, "api_key_added")
        }
    }
    
    fun logRateLimitReached() {
        analytics.logEvent("rate_limit_reached") {
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, "rate_limit_reached")
        }
    }
    
    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "LifeProblemSolver")
        }
    }
    
    fun logError(errorType: String, errorMessage: String) {
        analytics.logEvent("error") {
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, "error")
            param("error_type", errorType)
            param("error_message", errorMessage)
        }
    }
    
    fun logUserProperty(key: String, value: String) {
        analytics.setUserProperty(key, value)
    }
} 