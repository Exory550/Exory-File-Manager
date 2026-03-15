package com.exory550.exoryfilemanager.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.activities.ManageStorageActivity
import com.exory550.exoryfilemanager.dialogs.ConfirmationDialog
import com.exory550.exoryfilemanager.dialogs.StoragePickerDialog
import com.exory550.exoryfilemanager.extensions.clearCache
import com.exory550.exoryfilemanager.extensions.clearExternalCache
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class SettingsManageStorageFragment : SettingsAbstractFragment() {

    override val preferenceResource: Int = R.xml.preferences_manage_storage

    private lateinit var storageAnalyzerPreference: Preference
    private lateinit var defaultPathPreference: Preference
    private lateinit var cacheSizePreference: Preference
    private lateinit var clearCachePreference: Preference
    private lateinit var autoCleanPreference: SwitchPreferenceCompat
    private lateinit var cleanIntervalPreference: Preference
    private lateinit var deleteEmptyFoldersPreference: SwitchPreferenceCompat
    private lateinit var deleteThumbnailsPreference: SwitchPreferenceCompat
    private lateinit var deleteApkPreference: SwitchPreferenceCompat
    private lateinit var deleteLogsPreference: SwitchPreferenceCompat
    private lateinit var manageStorageAccessPreference: Preference

    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun setupPreferences() {
        storageAnalyzerPreference = findPreferenceCompat("storage_analyzer") ?: return
        defaultPathPreference = findPreferenceCompat("default_storage_path") ?: return
        cacheSizePreference = findPreferenceCompat("cache_size") ?: return
        clearCachePreference = findPreferenceCompat("clear_cache") ?: return
        autoCleanPreference = findPreferenceOfType("auto_clean") ?: return
        cleanIntervalPreference = findPreferenceCompat("clean_interval") ?: return
        deleteEmptyFoldersPreference = findPreferenceOfType("delete_empty_folders") ?: return
        deleteThumbnailsPreference = findPreferenceOfType("delete_thumbnails") ?: return
        deleteApkPreference = findPreferenceOfType("delete_apk") ?: return
        deleteLogsPreference = findPreferenceOfType("delete_logs") ?: return
        manageStorageAccessPreference = findPreferenceCompat("manage_storage_access") ?: return

        storageAnalyzerPreference.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), ManageStorageActivity::class.java))
            true
        }

        defaultPathPreference.setOnPreferenceClickListener {
            showStoragePathDialog()
            true
        }

        clearCachePreference.setOnPreferenceClickListener {
            showClearCacheDialog()
            true
        }

        autoCleanPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            preferenceManager.autoCleanEnabled = enabled
            cleanIntervalPreference.isEnabled = enabled
            true
        }

        manageStorageAccessPreference.setOnPreferenceClickListener {
            requestStorageAccess()
            true
        }
    }

    override fun updatePreferenceSummaries() {
        updateCacheSize()
        defaultPathPreference.summary = preferenceManager.defaultStoragePath ?: getString(R.string.internal_storage)
        autoCleanPreference.isChecked = preferenceManager.autoCleanEnabled
        cleanIntervalPreference.isEnabled = preferenceManager.autoCleanEnabled
        cleanIntervalPreference.summary = getCleanIntervalSummary()
        deleteEmptyFoldersPreference.isChecked = preferenceManager.deleteEmptyFolders
        deleteThumbnailsPreference.isChecked = preferenceManager.deleteThumbnails
        deleteApkPreference.isChecked = preferenceManager.deleteApkFiles
        deleteLogsPreference.isChecked = preferenceManager.deleteLogFiles

        updateStorageAccessSummary()
    }

    private fun updateCacheSize() {
        mainScope.launch {
            val cacheSize = withContext(Dispatchers.IO) {
                requireContext().cacheSize + requireContext().externalCacheSize
            }
            cacheSizePreference.summary = cacheSize.toFormattedFileSize()
        }
    }

    private fun getCleanIntervalSummary(): String {
        val interval = preferenceManager.cleanInterval
        return when (interval) {
            1 -> getString(R.string.every_day)
            7 -> getString(R.string.every_week)
            30 -> getString(R.string.every_month)
            else -> getString(R.string.every_day)
        }
    }

    private fun updateStorageAccessSummary() {
        val hasAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }

        manageStorageAccessPreference.summary = if (hasAccess) {
            getString(R.string.storage_access_granted)
        } else {
            getString(R.string.storage_access_denied)
        }
    }

    private fun showStoragePathDialog() {
        StoragePickerDialog.showForWriteAccess(requireContext()) { path ->
            preferenceManager.defaultStoragePath = path
            defaultPathPreference.summary = path
        }
    }

    private fun showClearCacheDialog() {
        ConfirmationDialog.show(
            requireContext(),
            R.string.clear_cache,
            R.string.clear_cache_message,
            R.string.clear,
            R.string.cancel,
            onPositive = {
                performClearCache()
            }
        )
    }

    private fun performClearCache() {
        mainScope.launch {
            val cacheCleared = withContext(Dispatchers.IO) {
                requireContext().clearCache()
            }
            val externalCacheCleared = withContext(Dispatchers.IO) {
                requireContext().clearExternalCache()
            }

            if (cacheCleared || externalCacheCleared) {
                updateCacheSize()
                Toast.makeText(requireContext(), R.string.cache_cleared, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), R.string.cache_clear_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivityForResult(intent, REQUEST_MANAGE_STORAGE)
            } else {
                Toast.makeText(requireContext(), R.string.storage_access_already_granted, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), R.string.not_available_for_android_version, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MANAGE_STORAGE) {
            updateStorageAccessSummary()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    companion object {
        private const val REQUEST_MANAGE_STORAGE = 1001
    }
}
