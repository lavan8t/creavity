# ==============================================================================
# breVITy - Full R8 / ProGuard Optimization & Obfuscation Rules
# ==============================================================================

# Aggressive Optimization & Obfuscation
-repackageclasses 'com.kd.brevity.opt'
-allowaccessmodification

# Strip debug logging from release APK for speed and size reduction
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Keep project domain models & binary packet serialization
-keep class com.kd.brevity.domain.model.** { *; }
-keep class com.kd.brevity.data.binary.** { *; }
-keepclassmembers class com.kd.brevity.domain.model.** { *; }

# Compose Stable & Immutable Annotations
-keepclassmembers class * {
    @androidx.compose.runtime.Stable <fields>;
    @androidx.compose.runtime.Immutable <fields>;
}

# Google Play Services Nearby Connections
-keep class com.google.android.gms.nearby.** { *; }
-dontwarn com.google.android.gms.nearby.**

# Kotlin Coroutines and Dispatchers
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# AndroidX Navigation & Compose
-dontwarn androidx.compose.**
-dontwarn androidx.navigation.**
