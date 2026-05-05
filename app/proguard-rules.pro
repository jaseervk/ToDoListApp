# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class com.todoapp.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Hilt / Dagger ────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# ── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── Jetpack Compose ──────────────────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# ── Kotlin Metadata (needed for reflection-based libraries) ──────────────────
-keepattributes RuntimeVisibleAnnotations
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }

# ── Google Fonts Provider ────────────────────────────────────────────────────
-keep class androidx.compose.ui.text.googlefonts.** { *; }

# ── Prevent R8 from stripping enum values (used by Room, Hilt, etc.) ────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Keep the Application class ───────────────────────────────────────────────
-keep class com.todoapp.TodoApplication { *; }
