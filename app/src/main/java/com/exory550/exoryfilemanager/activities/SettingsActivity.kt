package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.extensions.showToast
import com.exory550.exoryfilemanager.security.EncryptionManager
import com.exory550.exoryfilemanager.utils.Constants
import com.exory550.exoryfilemanager.utils.LocaleManager
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.exory550.exoryfilemanager.utils.ThemeManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    @Inject
    lateinit var themeManager: ThemeManager
    
    @Inject
    lateinit var localeManager: LocaleManager
    
    @Inject
    lateinit var encryptionManager: EncryptionManager
    
    private lateinit var securityCategory: PreferenceCategory
    private lateinit var appearanceCategory: PreferenceCategory
    private lateinit var storageCategory: PreferenceCategory
    private lateinit var advancedCategory: PreferenceCategory
    private lateinit var aboutCategory: PreferenceCategory
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        
        setupPreferences()
        updatePreferenceSummaries()
        setupClickListeners()
    }
    
    override fun onResume() {
        super.onResume()
        preferenceManager.registerOnSharedPreferenceChangeListener(this)
    }
    
    override fun onPause() {
        super.onPause()
        preferenceManager.unregisterOnSharedPreferenceChangeListener(this)
    }
    
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "theme_mode" -> handleThemeChange()
            "language" -> handleLanguageChange()
            "app_lock" -> handleAppLockChange()
            "biometric_lock" -> handleBiometricLockChange()
            "show_hidden_files" -> refreshSettings()
            "sort_files_by" -> refreshSettings()
            "default_view_mode" -> refreshSettings()
        }
    }
    
    private fun setupPreferences() {
        // Security Preferences
        securityCategory = findPreference("security_category")!!
        
        val appLockPreference = findPreference<SwitchPreferenceCompat>("app_lock")
        val biometricLockPreference = findPreference<SwitchPreferenceCompat>("biometric_lock")
        val lockTimeoutPreference = findPreference<ListPreference>("lock_timeout")
        val passwordPreference = findPreference<Preference>("change_password")
        val pinPreference = findPreference<Preference>("change_pin")
        val patternPreference = findPreference<Preference>("change_pattern")
        
        // Appearance Preferences
        appearanceCategory = findPreference("appearance_category")!!
        
        val themePreference = findPreference<ListPreference>("theme_mode")
        val languagePreference = findPreference<ListPreference>("language")
        val fontSizePreference = findPreference<SeekBarPreference>("font_size")
        val showThumbnailsPreference = findPreference<SwitchPreferenceCompat>("show_thumbnails")
        
        // Storage Preferences
        storageCategory = findPreference("storage_category")!!
        
        val defaultPathPreference = findPreference<Preference>("default_storage_path")
        val cacheSizePreference = findPreference<Preference>("cache_size")
        val autoDeleteCachePreference = findPreference<SwitchPreferenceCompat>("auto_delete_cache")
        
        // Advanced Preferences
        advancedCategory = findPreference("advanced_category")!!
        
        val rootAccessPreference = findPreference<SwitchPreferenceCompat>("root_access")
        val ftpServerPreference = findPreference<SwitchPreferenceCompat>("ftp_server")
        val cloudSyncPreference = findPreference<SwitchPreferenceCompat>("cloud_sync")
        
        // About Preferences
        aboutCategory = findPreference("about_category")!!
        
        val versionPreference = findPreference<Preference>("app_version")
        val checkUpdatesPreference = findPreference<Preference>("check_updates")
        val rateAppPreference = findPreference<Preference>("rate_app")
        val shareAppPreference = findPreference<Preference>("share_app")
        val privacyPolicyPreference = findPreference<Preference>("privacy_policy")
        val termsPreference = findPreference<Preference>("terms_of_service")
        val openSourceLicensesPreference = findPreference<Preference>("open_source_licenses")
        
        // Set version
        versionPreference?.summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        
        // Update biometric availability
        updateBiometricPreference()
        
        // Update cache size
        updateCacheSize()
    }
    
    private fun updatePreferenceSummaries() {
        // Theme
        findPreference<ListPreference>("theme_mode")?.summary = when (preferenceManager.themeMode) {
            ThemeManager.THEME_LIGHT -> getString(R.string.light)
            ThemeManager.THEME_DARK -> getString(R.string.dark)
            ThemeManager.THEME_SYSTEM -> getString(R.string.system_default)
            else -> getString(R.string.system_default)
        }
        
        // Language
        findPreference<ListPreference>("language")?.summary = when (preferenceManager.language) {
            "en" -> "English"
            "id" -> "Bahasa Indonesia"
            "ar" -> "العربية"
            "zh" -> "中文"
            "es" -> "Español"
            "hi" -> "हिन्दी"
            else -> "English"
        }
        
        // Lock timeout
        val timeoutValues = resources.getStringArray(R.array.lock_timeout_values)
        val timeoutLabels = resources.getStringArray(R.array.lock_timeout_labels)
        val timeoutIndex = timeoutValues.indexOf(preferenceManager.lockTimeout.toString())
        if (timeoutIndex >= 0) {
            findPreference<ListPreference>("lock_timeout")?.summary = timeoutLabels[timeoutIndex]
        }
        
        // Default storage path
        findPreference<Preference>("default_storage_path")?.summary = 
            preferenceManager.defaultStoragePath ?: getString(R.string.internal_storage)
    }
    
    private fun setupClickListeners() {
        // Change password
        findPreference<Preference>("change_password")?.setOnPreferenceClickListener {
            showChangePasswordDialog()
            true
        }
        
        // Change PIN
        findPreference<Preference>("change_pin")?.setOnPreferenceClickListener {
            showChangePinDialog()
            true
        }
        
        // Change pattern
        findPreference<Preference>("change_pattern")?.setOnPreferenceClickListener {
            showChangePatternDialog()
            true
        }
        
        // Default storage path
        findPreference<Preference>("default_storage_path")?.setOnPreferenceClickListener {
            showStoragePathDialog()
            true
        }
        
        // Clear cache
        findPreference<Preference>("cache_size")?.setOnPreferenceClickListener {
            showClearCacheDialog()
            true
        }
        
        // Check updates
        findPreference<Preference>("check_updates")?.setOnPreferenceClickListener {
            checkForUpdates()
            true
        }
        
        // Rate app
        findPreference<Preference>("rate_app")?.setOnPreferenceClickListener {
            rateApp()
            true
        }
        
        // Share app
        findPreference<Preference>("share_app")?.setOnPreferenceClickListener {
            shareApp()
            true
        }
        
        // Privacy policy
        findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
            openUrl("https://exory550.com/privacy")
            true
        }
        
        // Terms of service
        findPreference<Preference>("terms_of_service")?.setOnPreferenceClickListener {
            openUrl("https://exory550.com/terms")
            true
        }
        
        // Open source licenses
        findPreference<Preference>("open_source_licenses")?.setOnPreferenceClickListener {
            showOpenSourceLicenses()
            true
        }
    }
    
    private fun updateBiometricPreference() {
        val biometricPreference = findPreference<SwitchPreferenceCompat>("biometric_lock")
        val biometricManager = BiometricManager.from(requireContext())
        
        val canAuthenticate = when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                biometricPreference?.isEnabled = false
                biometricPreference?.summary = getString(R.string.biometric_not_supported)
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                biometricPreference?.isEnabled = false
                biometricPreference?.summary = getString(R.string.biometric_unavailable)
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                biometricPreference?.isEnabled = true
                biometricPreference?.summary = getString(R.string.biometric_not_enrolled)
                biometricPreference?.setOnPreferenceClickListener {
                    startActivity(Intent(Settings.ACTION_BIOMETRIC_ENROLL))
                    true
                }
                false
            }
            else -> false
        }
        
        if (canAuthenticate) {
            biometricPreference?.isEnabled = true
            biometricPreference?.summary = getString(R.string.biometric_available)
        }
    }
    
    private fun updateCacheSize() {
        lifecycleScope.launch {
            val cacheDir = requireContext().cacheDir
            val cacheSize = withContext(Dispatchers.IO) {
                calculateFolderSize(cacheDir)
            }
            
            val sizeText = android.text.format.Formatter.formatFileSize(requireContext(), cacheSize)
            findPreference<Preference>("cache_size")?.summary = sizeText
        }
    }
    
    private fun calculateFolderSize(file: File): Long {
        if (!file.exists()) return 0
        
        return if (file.isFile) {
            file.length()
        } else {
            file.listFiles()?.sumOf { calculateFolderSize(it) } ?: 0
        }
    }
    
    private fun handleThemeChange() {
        themeManager.applyTheme(requireActivity())
        requireActivity().recreate()
    }
    
    private fun handleLanguageChange() {
        localeManager.applyLocale(requireContext())
        requireActivity().recreate()
    }
    
    private fun handleAppLockChange() {
        if (preferenceManager.isAppLockEnabled) {
            showSetupSecurityDialog()
        } else {
            preferenceManager.clearSecurityPreferences()
        }
    }
    
    private fun handleBiometricLockChange() {
        if (preferenceManager.isBiometricEnabled) {
            if (!preferenceManager.isPasswordSet && 
                !preferenceManager.isPinSet && 
                !preferenceManager.isPatternSet) {
                showSetupSecurityDialog()
            }
        }
    }
    
    private fun showSetupSecurityDialog() {
        val options = arrayOf(
            getString(R.string.password),
            getString(R.string.pin),
            getString(R.string.pattern)
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.setup_security)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showChangePasswordDialog()
                    1 -> showChangePinDialog()
                    2 -> showChangePatternDialog()
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                preferenceManager.isAppLockEnabled = false
            }
            .show()
    }
    
    private fun showChangePasswordDialog() {
        // Implement password change dialog
        showToast(getString(R.string.coming_soon))
    }
    
    private fun showChangePinDialog() {
        // Implement PIN change dialog
        showToast(getString(R.string.coming_soon))
    }
    
    private fun showChangePatternDialog() {
        // Implement pattern change dialog
        showToast(getString(R.string.coming_soon))
    }
    
    private fun showStoragePathDialog() {
        val paths = arrayOf(
            getString(R.string.internal_storage),
            getString(R.string.external_storage_primary),
            getString(R.string.external_storage_secondary),
            getString(R.string.custom_path)
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.default_storage_path)
            .setItems(paths) { _, which ->
                when (which) {
                    0 -> preferenceManager.defaultStoragePath = 
                        Environment.getExternalStorageDirectory().absolutePath
                    1 -> {
                        // Get primary external storage
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            val externalFilesDirs = requireContext().getExternalFilesDirs(null)
                            if (externalFilesDirs.size > 1) {
                                val externalDir = externalFilesDirs[1]
                                if (externalDir != null) {
                                    preferenceManager.defaultStoragePath = 
                                        externalDir.absolutePath.substringBefore("/Android")
                                }
                            }
                        }
                    }
                    2 -> showCustomPathDialog()
                }
                refreshSettings()
            }
            .show()
    }
    
    private fun showCustomPathDialog() {
        // Implement custom path picker
        showToast(getString(R.string.coming_soon))
    }
    
    private fun showClearCacheDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_cache)
            .setMessage(R.string.clear_cache_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                clearCache()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun clearCache() {
        lifecycleScope.launch {
            showProgress()
            
            val success = withContext(Dispatchers.IO) {
                try {
                    val cacheDir = requireContext().cacheDir
                    cacheDir.deleteRecursively()
                    cacheDir.mkdirs()
                    true
                } catch (e: Exception) {
                    false
                }
            }
            
            dismissProgress()
            
            if (success) {
                showToast(getString(R.string.cache_cleared))
                updateCacheSize()
            } else {
                showToast(getString(R.string.cache_clear_error))
            }
        }
    }
    
    private fun checkForUpdates() {
        showToast(getString(R.string.checking_updates))
        // Implement update check
    }
    
    private fun rateApp() {
        val intent = Intent(Intent.ACTION_VIEW, 
            Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}"))
        startActivity(intent)
    }
    
    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, 
                getString(R.string.share_app_text, 
                    "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_title)))
    }
    
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
    
    private fun showOpenSourceLicenses() {
        startActivity(Intent(requireContext(), OssLicensesActivity::class.java))
    }
    
    private fun showProgress() {
        // Implement progress dialog
    }
    
    private fun dismissProgress() {
        // Implement progress dismiss
    }
    
    private fun refreshSettings() {
        preferenceManager.notifyChange()
        updatePreferenceSummaries()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requireActivity().onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    companion object {
        fun newInstance(): SettingsActivity {
            return SettingsActivity()
        }
    }
}
