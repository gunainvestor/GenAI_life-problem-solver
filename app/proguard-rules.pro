# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Apache POI classes
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.apache.logging.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.logging.**

# Handle missing java.awt classes (required by Apache POI)
-dontwarn java.awt.**
-dontwarn java.awt.geom.**
-dontwarn java.awt.font.**
-dontwarn java.awt.image.**
-dontwarn java.awt.color.**
-dontwarn java.awt.print.**

# Handle specific missing classes
-dontwarn java.awt.Shape
-dontwarn com.graphbuilder.curve.ShapeMultiPath

# Keep POI specific classes
-keep class org.apache.poi.ss.usermodel.** { *; }
-keep class org.apache.poi.xssf.usermodel.** { *; }
-keep class org.apache.poi.hssf.usermodel.** { *; }

# Keep POI internal classes
-keep class org.apache.poi.util.** { *; }
-keep class org.apache.poi.common.** { *; }

# Keep XMLBeans classes
-keep class org.apache.xmlbeans.impl.** { *; }
-keep class org.apache.xmlbeans.xmlstream.** { *; }

# Keep logging classes
-keep class org.apache.logging.log4j.** { *; }
-keep class org.apache.logging.log4j.core.** { *; }

# Keep Compose-related classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt/Dagger classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# Keep Retrofit classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep OkHttp classes
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep your app's main classes
-keep class com.lifeproblemsolver.app.** { *; }
-keepclassmembers class com.lifeproblemsolver.app.** { *; }

# Keep data models
-keep class com.lifeproblemsolver.app.data.model.** { *; }
-keepclassmembers class com.lifeproblemsolver.app.data.model.** { *; }

# Keep DAO interfaces
-keep interface com.lifeproblemsolver.app.data.dao.** { *; }

# Keep ViewModels
-keep class com.lifeproblemsolver.app.ui.viewmodel.** { *; }
-keepclassmembers class com.lifeproblemsolver.app.ui.viewmodel.** { *; }

# Keep services
-keep class com.lifeproblemsolver.app.service.** { *; }
-keepclassmembers class com.lifeproblemsolver.app.service.** { *; }

# Keep backup service
-keep class com.lifeproblemsolver.app.data.backup.** { *; }
-keepclassmembers class com.lifeproblemsolver.app.data.backup.** { *; }

# Keep export service
-keep class com.lifeproblemsolver.app.data.export.** { *; }
-keepclassmembers class com.lifeproblemsolver.app.data.export.** { *; }

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Kotlin top-level functions (like NavGraph)
-keepclassmembers class * {
    @kotlin.jvm.JvmStatic *;
}

# Keep all functions in navigation package
-keepclassmembers class com.lifeproblemsolver.app.ui.navigation.** {
    *;
}

# Keep Android lifecycle components
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# Keep navigation components
-keepnames class androidx.navigation.fragment.NavHostFragment
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# Keep Compose navigation
-keep class androidx.navigation.compose.** { *; }

# Keep specific navigation classes
-keep class com.lifeproblemsolver.app.ui.navigation.** { *; }
-keepclassmembers class com.lifeproblemsolver.app.ui.navigation.** { *; }

# Keep NavGraph and related classes
-keep class com.lifeproblemsolver.app.ui.navigation.NavGraphKt { *; }
-keep class com.lifeproblemsolver.app.ui.navigation.Screen { *; }

# Keep all UI classes
-keep class com.lifeproblemsolver.app.ui.** { *; }
-keepclassmembers class com.lifeproblemsolver.app.ui.** { *; }

# Keep Material Design components
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }

# Keep date/time libraries
-keep class org.jetbrains.kotlinx.datetime.** { *; }

# General Android optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Additional rules for Kotlin-generated classes
-keep class **$* { *; }
-keepclassmembers class **$* {
    *;
}

# Keep all Kotlin-generated classes in navigation package
-keep class com.lifeproblemsolver.app.ui.navigation.**$* { *; }
-keepclassmembers class com.lifeproblemsolver.app.ui.navigation.**$* { *; }

# Keep NavGraph function specifically
-keepclassmembers class com.lifeproblemsolver.app.ui.navigation.NavGraphKt {
    public static void NavGraph(androidx.navigation.NavHostController, androidx.compose.ui.Modifier);
    public static void NavGraph(androidx.navigation.NavHostController);
}

# Keep all Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
} 