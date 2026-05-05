package com.todoapp.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary         = Primary,
    onPrimary       = OnPrimary,
    primaryContainer = PrimaryDark,
    secondary       = Secondary,
    onSecondary     = OnPrimary,
    secondaryContainer = SecondaryDark,
    background      = Background,
    onBackground    = OnBackground,
    surface         = Surface,
    onSurface       = OnSurface,
    surfaceVariant  = SurfaceVariant,
    error           = Error,
    onError         = OnError
)

private val LightColorScheme = lightColorScheme(
    primary         = PrimaryLight2,
    onPrimary       = OnPrimary,
    primaryContainer = PrimaryLight,
    secondary       = Secondary,
    onSecondary     = OnPrimary,
    secondaryContainer = SecondaryDark,
    background      = BackgroundLight,
    onBackground    = OnBackgroundLight,
    surface         = SurfaceLight,
    onSurface       = OnSurfaceLight,
    surfaceVariant  = SurfaceVariantLight,
    error           = Error,
    onError         = OnError
)

@Composable
fun TodoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Note: window.statusBarColor is deprecated on API 35+ (Android 15).
            // enableEdgeToEdge() in MainActivity handles the status bar background.
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
