# ==============================================================================
# CreaVITy - Full R8 / ProGuard Maximum Obfuscation & Aggressive Optimization
# ==============================================================================

# Aggressive Optimization Passes
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-repackageclasses 'com.kd.creavity.opt'
-overloadaggressively

# Strip debug and information attributes for maximum bytecode shrinking & obfuscation
-renamesourcefileattribute ""
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable,!MethodParameters

# Strip all logging calls and prints
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# Android Entry Points
-keep public class com.kd.creavity.MainActivity extends androidx.activity.ComponentActivity {
    public <init>();
}

# Keep project data packet models for cross-device wire protocol stability
-keepclassmembers class com.kd.creavity.data.binary.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Google Play Services Nearby Connections
-keep class com.google.android.gms.nearby.** { *; }
-keep interface com.google.android.gms.nearby.** { *; }
-dontwarn com.google.android.gms.nearby.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Compose runtime requirements
-keepclassmembers class * {
    @androidx.compose.runtime.Stable <fields>;
    @androidx.compose.runtime.Immutable <fields>;
}

-dontwarn androidx.compose.**
-dontwarn androidx.navigation.**
-dontwarn org.json.**
-dontwarn java.awt.**
