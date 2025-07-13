plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.lifeproblemsolver.app"
    compileSdk = 34

    // JVM toolchain for both Java and Kotlin
    kotlin {
        jvmToolchain(17)
    }

    // Explicit Java compile options to match Kotlin toolchain
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Kotlin compiler options
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    // Enable BuildConfig for API key
    buildFeatures {
        buildConfig = true
    }
    
    // Disable JDK image transformation
    androidResources {
        noCompress += listOf("")
    }

    // Signing configuration for Google Play Store
    signingConfigs {
        create("release") {
            storeFile = file("release-keystore.jks")
            storePassword = "lifeproblemsolver2024"
            keyAlias = "release-key"
            keyPassword = "lifeproblemsolver2024"
        }
    }

    defaultConfig {
        applicationId = "com.lifeproblemsolver.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // BuildConfig fields for API keys
        buildConfigField("String", "OPENAI_API_KEY", "\"${project.findProperty("OPENAI_API_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // Temporarily disable minification
            isShrinkResources = false  // Temporarily disable resource shrinking
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    
    // Excel Export (Apache POI)
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.xmlbeans:xmlbeans:5.1.1")
    
    // Firebase Analytics
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.5.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.google.android.material:material:1.11.0")
}

ksp {
    arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
}

// Task to generate SHA fingerprints
tasks.register("generateFingerprints") {
    group = "verification"
    description = "Generate SHA-1 and SHA-256 fingerprints for Firebase"
    
    doLast {
        println("=== SHA Certificate Fingerprints ===")
        println("Debug keystore fingerprints:")
        
        // Debug keystore
        val debugKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
        if (debugKeystore.exists()) {
            exec {
                commandLine("keytool", "-list", "-v", "-keystore", debugKeystore.absolutePath, 
                           "-alias", "androiddebugkey", "-storepass", "android", "-keypass", "android")
            }
        } else {
            println("Debug keystore not found at: ${debugKeystore.absolutePath}")
        }
        
        println("\n=== Instructions ===")
        println("1. Copy the SHA-1 and SHA-256 fingerprints above")
        println("2. Go to Firebase Console: https://console.firebase.google.com/")
        println("3. Select your project > Project Settings > Your apps")
        println("4. Add your Android app with package: com.lifeproblemsolver.app")
        println("5. Add the SHA fingerprints to your Firebase project")
        println("6. Download google-services.json and place it in the app/ directory")
    }
} 