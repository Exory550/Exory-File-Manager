package com.exory550.exoryfilemanager.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun applyTheme(context: Context) {
        val prefs = context.getSharedPreferences("exory_prefs", Context.MODE_PRIVATE)
        val mode = prefs.getInt("theme_mode", THEME_SYSTEM)
        setThemeMode(mode)
    }

    fun setThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    companion object {
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2
    }
}
