package com.exory550.exoryfilemanager.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.activities.AboutActivity
import com.exory550.exoryfilemanager.activities.ManageStorageActivity
import com.exory550.exoryfilemanager.dialogs.ConfirmationDialog
import com.exory550.exoryfilemanager.dialogs.WritePermissionDialog
import com.exory550.exoryfilemanager.utils.LocaleManager
import com.exory550.exoryfilemanager.utils.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : SettingsAbstractFragment() {

    override val preferenceResource: Int = R.xml.preferences

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var localeManager: LocaleManager

    private lateinit var themePreference: ListPreference
    private lateinit var languagePreference: ListPreference
    private lateinit var appLockPreference: SwitchPreferenceCompat
    private lateinit var storageManagementPreference: Preference
    private lateinit var aboutPreference: Preference
    private lateinit var versionPreference: Preference

    override fun setupPreferences() {
        themePreference = findPreferenceOfType("theme_mode") ?: return
        languagePreference = findPreferenceOfType("language") ?: return
        appLockPreference = findPreferenceOfType("app_lock") ?: return
        storageManagementPreference = findPreferenceCompat("storage_management") ?: return
        aboutPreference = findPreferenceCompat("about") ?: return
        versionPreference = findPreferenceCompat("app_version") ?: return

        versionPreference.summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        themePreference.setOnPreferenceChangeListener { _, newValue ->
            val theme = newValue as String
            themeManager.setThemeMode(
                when (theme) {
                    "light" -> ThemeManager.THEME_LIGHT
                    "dark" -> ThemeManager.THEME_DARK
                    else -> ThemeManager.THEME_SYSTEM
                }
            )
            restartActivity()
            true
        }

        languagePreference.setOnPreferenceChangeListener { _, newValue ->
            val language = newValue as String
            localeManager.setLanguage(language)
            restartActivity()
            true
        }

        appLockPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            if (enabled) {
                navigateToSecuritySettings()
            }
            true
        }

        storageManagementPreference.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), ManageStorageActivity::class.java))
            true
        }

        aboutPreference.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            true
        }
    }

    override fun updatePreferenceSummaries() {
        themePreference.value = when (preferenceManager.themeMode) {
            ThemeManager.THEME_LIGHT -> "light"
            ThemeManager.THEME_DARK -> "dark"
            else -> "system"
        }
        themePreference.summary = themePreference.entry

        languagePreference.value = preferenceManager.language
        languagePreference.summary = languagePreference.entry

        appLockPreference.isChecked = preferenceManager.isAppLockEnabled
    }

    private fun navigateToSecuritySettings() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsSecurityFragment())
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
