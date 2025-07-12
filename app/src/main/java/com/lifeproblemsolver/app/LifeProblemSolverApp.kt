package com.lifeproblemsolver.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LifeProblemSolverApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Enable debug mode for development
        if (BuildConfig.DEBUG) {
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
        }
    }
} 