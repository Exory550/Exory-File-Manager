package com.exory550.exoryfilemanager.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun setLanguage(language: String) {
        val prefs = context.getSharedPreferences("exory_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("language", language).apply()
    }

    fun applyLocale(context: Context) {}

    fun setLocale(context: Context): Context = context
}
