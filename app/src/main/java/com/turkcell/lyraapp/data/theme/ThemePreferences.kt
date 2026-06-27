package com.turkcell.lyraapp.data.theme

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    // Null means follow system default
    private val _isDarkModeFlow = MutableStateFlow<Boolean?>(
        if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    )
    val isDarkModeFlow: StateFlow<Boolean?> = _isDarkModeFlow.asStateFlow()

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
        _isDarkModeFlow.value = isDark
    }

    fun clearOverride() {
        prefs.edit().remove(KEY_DARK_MODE).apply()
        _isDarkModeFlow.value = null
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
