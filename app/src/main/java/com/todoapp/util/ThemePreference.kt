package com.todoapp.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the user's theme preference (Light / Dark) and persists it
 * in SharedPreferences.  Exposes a [StateFlow] so composables can
 * reactively recompose when the theme changes.
 */
@Singleton
class ThemePreference @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_IS_DARK = "is_dark_theme"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean(KEY_IS_DARK, true))

    /** Observable dark-theme flag */
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    /** Toggle between light and dark, persisting immediately */
    fun toggle() {
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue
        prefs.edit().putBoolean(KEY_IS_DARK, newValue).apply()
    }
}
