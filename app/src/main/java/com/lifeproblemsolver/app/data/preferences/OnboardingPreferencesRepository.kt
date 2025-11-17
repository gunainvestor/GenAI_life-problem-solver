package com.lifeproblemsolver.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_preferences")

@Singleton
class OnboardingPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val COMPLETED: Preferences.Key<Boolean> = booleanPreferencesKey("onboarding_completed")
        val DAILY_MINUTES: Preferences.Key<Int> = intPreferencesKey("decision_daily_minutes")
        val WEEKLY_MINUTES: Preferences.Key<Int> = intPreferencesKey("decision_weekly_minutes")
        val RECLAIM_FOCUS: Preferences.Key<String> = stringPreferencesKey("reclaim_focus")
    }

    val onboardingState: Flow<OnboardingState> = context.onboardingDataStore.data.map { prefs ->
        OnboardingState(
            hasCompleted = prefs[Keys.COMPLETED] ?: false,
            dailyDecisionMinutes = prefs[Keys.DAILY_MINUTES] ?: 0,
            weeklyDecisionMinutes = prefs[Keys.WEEKLY_MINUTES] ?: 0,
            reclaimFocusIntent = prefs[Keys.RECLAIM_FOCUS].orEmpty()
        )
    }

    suspend fun saveOnboardingResult(result: OnboardingResult) {
        context.onboardingDataStore.edit { prefs ->
            prefs[Keys.COMPLETED] = true
            prefs[Keys.DAILY_MINUTES] = result.dailyDecisionMinutes
            prefs[Keys.WEEKLY_MINUTES] = result.weeklyDecisionMinutes
            prefs[Keys.RECLAIM_FOCUS] = result.reclaimFocusIntent
        }
    }
}

