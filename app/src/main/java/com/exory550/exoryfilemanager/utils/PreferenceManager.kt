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

    // App settings
    var preventScreenshots: Boolean
        get() = getBoolean("prevent_screenshots", false)
        set(value) = putBoolean("prevent_screenshots", value)

    var isIntroCompleted: Boolean
        get() = getBoolean("intro_completed", false)
        set(value) = putBoolean("intro_completed", value)

    var wasAppProtectionHandled: Boolean
        get() = getBoolean("app_protection_handled", false)
        set(value) = putBoolean("app_protection_handled", value)

    // Theme & Language
    var themeMode: Int
        get() = getInt("theme_mode", THEME_SYSTEM)
        set(value) = putInt("theme_mode", value)

    var language: String
        get() = getString("language", "en")
        set(value) = putString("language", value)

    // App Lock
    var isAppLockEnabled: Boolean
        get() = getBoolean("app_lock_enabled", false)
        set(value) = putBoolean("app_lock_enabled", value)

    var lockMethod: String
        get() = getString("lock_method", "password")
        set(value) = putString("lock_method", value)

    var isPasswordSet: Boolean
        get() = getBoolean("is_password_set", false)
        set(value) = putBoolean("is_password_set", value)

    var isPinSet: Boolean
        get() = getBoolean("is_pin_set", false)
        set(value) = putBoolean("is_pin_set", value)

    var isPatternSet: Boolean
        get() = getBoolean("is_pattern_set", false)
        set(value) = putBoolean("is_pattern_set", value)

    var isBiometricEnabled: Boolean
        get() = getBoolean("biometric_enabled", false)
        set(value) = putBoolean("biometric_enabled", value)

    var lockTimeout: Long
        get() = getLong("lock_timeout", 0L)
        set(value) = putLong("lock_timeout", value)

    var autoLock: Boolean
        get() = getBoolean("auto_lock", true)
        set(value) = putBoolean("auto_lock", value)

    var lockOnScreenOff: Boolean
        get() = getBoolean("lock_on_screen_off", true)
        set(value) = putBoolean("lock_on_screen_off", value)

    var showLockNotifications: Boolean
        get() = getBoolean("show_lock_notifications", true)
        set(value) = putBoolean("show_lock_notifications", value)

    var hideNotificationContent: Boolean
        get() = getBoolean("hide_notification_content", false)
        set(value) = putBoolean("hide_notification_content", value)

    // Encryption
    var isEncryptionEnabled: Boolean
        get() = getBoolean("encryption_enabled", false)
        set(value) = putBoolean("encryption_enabled", value)

    var isEncryptMediaEnabled: Boolean
        get() = getBoolean("encrypt_media", false)
        set(value) = putBoolean("encrypt_media", value)

    var isEncryptDocumentsEnabled: Boolean
        get() = getBoolean("encrypt_documents", false)
        set(value) = putBoolean("encrypt_documents", value)

    var isEncryptThumbnailsEnabled: Boolean
        get() = getBoolean("encrypt_thumbnails", false)
        set(value) = putBoolean("encrypt_thumbnails", value)

    var isSecureDeleteEnabled: Boolean
        get() = getBoolean("secure_delete", false)
        set(value) = putBoolean("secure_delete", value)

    var encryptionMethod: String
        get() = getString("encryption_method", "aes")
        set(value) = putString("encryption_method", value)

    var wipeAllEncryptedData: Boolean
        get() = getBoolean("wipe_encrypted", false)
        set(value) = putBoolean("wipe_encrypted", value)

    // Thumbnails
    var showThumbnails: Boolean
        get() = getBoolean("show_thumbnails", true)
        set(value) = putBoolean("show_thumbnails", value)

    var generateImageThumbnails: Boolean
        get() = getBoolean("generate_image_thumbnails", true)
        set(value) = putBoolean("generate_image_thumbnails", value)

    var generateVideoThumbnails: Boolean
        get() = getBoolean("generate_video_thumbnails", true)
        set(value) = putBoolean("generate_video_thumbnails", value)

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

    // Storage
    var defaultStoragePath: String
        get() = getString("default_storage_path", "")
        set(value) = putString("default_storage_path", value)

    var autoCleanEnabled: Boolean
        get() = getBoolean("auto_clean_enabled", false)
        set(value) = putBoolean("auto_clean_enabled", value)

    var deleteEmptyFolders: Boolean
        get() = getBoolean("delete_empty_folders", false)
        set(value) = putBoolean("delete_empty_folders", value)

    var deleteThumbnails: Boolean
        get() = getBoolean("delete_thumbnails", false)
        set(value) = putBoolean("delete_thumbnails", value)

    var deleteApkFiles: Boolean
        get() = getBoolean("delete_apk_files", false)
        set(value) = putBoolean("delete_apk_files", value)

    var deleteLogFiles: Boolean
        get() = getBoolean("delete_log_files", false)
        set(value) = putBoolean("delete_log_files", value)

    var cleanInterval: Int
        get() = getInt("clean_interval", 7)
        set(value) = putInt("clean_interval", value)

    var showHiddenFiles: Boolean
        get() = getBoolean("show_hidden_files", false)
        set(value) = putBoolean("show_hidden_files", value)

    var passwordHash: String
        get() = getString("password_hash", "")
        set(value) = putString("password_hash", value)

    var passwordHint: String
        get() = getString("password_hint", "")
        set(value) = putString("password_hint", value)

    companion object {
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2

        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context).also { instance = it }
            }
        }
    }
}
