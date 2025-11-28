# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK.

#===============================================================================
# Room Database
#===============================================================================
-keep class com.snappet.persistence.entities.** { *; }
-keep class com.snappet.persistence.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

#===============================================================================
# Kotlin Coroutines
#===============================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

#===============================================================================
# Jetpack Compose
#===============================================================================
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class androidx.compose.** {
    *;
}
-dontwarn androidx.compose.**

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }

# Keep CompositionLocal providers
-keepclassmembers class * {
    *** get*();
}

#===============================================================================
# Lottie Animations
#===============================================================================
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

#===============================================================================
# Kotlin Reflection
#===============================================================================
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

#===============================================================================
# AndroidX and Lifecycle
#===============================================================================
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.navigation.** { *; }
-dontwarn androidx.lifecycle.**

#===============================================================================
# Data Classes and Serialization
#===============================================================================
-keepclassmembers class com.snappet.core.models.** {
    <fields>;
    <init>(...);
}
-keep class com.snappet.core.models.** { *; }

#===============================================================================
# Application Classes
#===============================================================================
-keep class com.snappet.** { *; }
-keepclassmembers class com.snappet.** {
    *;
}

#===============================================================================
# Enum Classes
#===============================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#===============================================================================
# TarsosDSP (Disabled - dependency removed, using custom DSP)
#===============================================================================
# -keep class be.tarsos.dsp.** { *; }

#===============================================================================
# General Android
#===============================================================================
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#===============================================================================
# Optimization
#===============================================================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
