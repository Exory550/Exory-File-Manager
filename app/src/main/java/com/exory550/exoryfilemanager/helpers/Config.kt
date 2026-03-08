package com.exory550.exoryfilemanager.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.exory550.exoryfilemanager.BuildConfig

class Config private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: Config? = null

        fun getInstance(context: Context): Config {
            return instance ?: synchronized(this) {
                instance ?: Config(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                "exory_config",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } else {
            context.getSharedPreferences("exory_config", Context.MODE_PRIVATE)
        }
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return prefs.getLong(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun putStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }

    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return prefs.getStringSet(key, defaultValue) ?: defaultValue
    }

    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun getAll(): Map<String, *> {
        return prefs.all
    }

    object Keys {
        const val FIRST_RUN = "first_run"
        const val INTRO_COMPLETED = "intro_completed"
        const val THEME_MODE = "theme_mode"
        const val LANGUAGE = "language"
        const val APP_LOCK_ENABLED = "app_lock_enabled"
        const val LOCK_METHOD = "lock_method"
        const val PASSWORD_HASH = "password_hash"
        const val PASSWORD_HINT = "password_hint"
        const val PIN_HASH = "pin_hash"
        const val PATTERN_HASH = "pattern_hash"
        const val BIOMETRIC_ENABLED = "biometric_enabled"
        const val LOCK_TIMEOUT = "lock_timeout"
        const val AUTO_LOCK = "auto_lock"
        const val LOCK_ON_SCREEN_OFF = "lock_on_screen_off"
        const val SHOW_LOCK_NOTIFICATIONS = "show_lock_notifications"
        const val HIDE_NOTIFICATION_CONTENT = "hide_notification_content"
        const val ENCRYPTION_ENABLED = "encryption_enabled"
        const val ENCRYPTION_METHOD = "encryption_method"
        const val ENCRYPT_MEDIA = "encrypt_media"
        const val ENCRYPT_DOCUMENTS = "encrypt_documents"
        const val ENCRYPT_THUMBNAILS = "encrypt_thumbnails"
        const val SECURE_DELETE = "secure_delete"
        const val DEFAULT_STORAGE_PATH = "default_storage_path"
        const val SHOW_HIDDEN_FILES = "show_hidden_files"
        const val SHOW_THUMBNAILS = "show_thumbnails"
        const val THUMBNAIL_QUALITY = "thumbnail_quality"
        const val THUMBNAIL_SIZE = "thumbnail_size"
        const val CACHE_THUMBNAILS = "cache_thumbnails"
        const val GENERATE_VIDEO_THUMBNAILS = "generate_video_thumbnails"
        const val GENERATE_IMAGE_THUMBNAILS = "generate_image_thumbnails"
        const val GENERATE_DOCUMENT_THUMBNAILS = "generate_document_thumbnails"
        const val THUMBNAIL_CACHE_SIZE = "thumbnail_cache_size"
        const val AUTO_CLEAN_ENABLED = "auto_clean_enabled"
        const val CLEAN_INTERVAL = "clean_interval"
        const val DELETE_EMPTY_FOLDERS = "delete_empty_folders"
        const val DELETE_THUMBNAILS = "delete_thumbnails"
        const val DELETE_APK_FILES = "delete_apk_files"
        const val DELETE_LOG_FILES = "delete_log_files"
        const val PREVENT_SCREENSHOTS = "prevent_screenshots"
        const val ROOT_ACCESS_ENABLED = "root_access_enabled"
        const val FTP_SERVER_ENABLED = "ftp_server_enabled"
        const val CLOUD_SYNC_ENABLED = "cloud_sync_enabled"
        const val LAST_BACKUP_TIME = "last_backup_time"
        const val BACKUP_REMINDER = "backup_reminder"
    }
}
