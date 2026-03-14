package com.exory550.exoryfilemanager.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "exory_prefs", Context.MODE_PRIVATE
    )

    fun getString(key: String, default: String = ""): String =
        prefs.getString(key, default) ?: default

    fun putString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun putBoolean(key: String, value: Boolean) =
        prefs.edit().putBoolean(key, value).apply()

    fun getInt(key: String, default: Int = 0): Int =
        prefs.getInt(key, default)

    fun putInt(key: String, value: Int) =
        prefs.edit().putInt(key, value).apply()

    fun getLong(key: String, default: Long = 0L): Long =
        prefs.getLong(key, default)

    fun putLong(key: String, value: Long) =
        prefs.edit().putLong(key, value).apply()

    fun remove(key: String) =
        prefs.edit().remove(key).apply()

    fun clear() =
        prefs.edit().clear().apply()

    var generateImageThumbnails: Boolean
        get() = getBoolean("generate_image_thumbnails", true)
        set(value) = putBoolean("generate_image_thumbnails", value)

    var generateDocumentThumbnails: Boolean
        get() = getBoolean("generate_document_thumbnails", true)
        set(value) = putBoolean("generate_document_thumbnails", value)

    var thumbnailCacheSize: Int
        get() = getInt("thumbnail_cache_size", 100)
        set(value) = putInt("thumbnail_cache_size", value)

    var thumbnailQuality: Int
        get() = getInt("thumbnail_quality", 80)
        set(value) = putInt("thumbnail_quality", value)

    var thumbnailSize: Int
        get() = getInt("thumbnail_size", 256)
        set(value) = putInt("thumbnail_size", value)

    var cacheThumbnails: Boolean
        get() = getBoolean("cache_thumbnails", true)
        set(value) = putBoolean("cache_thumbnails", value)

    var preventScreenshots: Boolean
        get() = getBoolean("prevent_screenshots", false)
        set(value) = putBoolean("prevent_screenshots", value)

    var isIntroCompleted: Boolean
        get() = getBoolean("intro_completed", false)
        set(value) = putBoolean("intro_completed", value)

    var isAppLockEnabled: Boolean
        get() = getBoolean("app_lock_enabled", false)
        set(value) = putBoolean("app_lock_enabled", value)

    var wasAppProtectionHandled: Boolean
        get() = getBoolean("app_protection_handled", false)
        set(value) = putBoolean("app_protection_handled", value)

    companion object {
        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context).also { instance = it }
            }
        }
    }
}
