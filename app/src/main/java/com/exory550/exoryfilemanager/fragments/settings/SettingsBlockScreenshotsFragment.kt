package com.exory550.exoryfilemanager.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.preference.SwitchPreferenceCompat
import com.exory550.exoryfilemanager.R

class SettingsBlockScreenshotsFragment : SettingsAbstractFragment() {

    override val preferenceResource: Int = R.xml.preferences_block_screenshots

    private lateinit var blockScreenshotsPreference: SwitchPreferenceCompat

    override fun setupPreferences() {
        blockScreenshotsPreference = findPreferenceOfType("block_screenshots") ?: return

        blockScreenshotsPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            preferenceManager.preventScreenshots = enabled
            true
        }
    }

    override fun updatePreferenceSummaries() {
        blockScreenshotsPreference.isChecked = preferenceManager.preventScreenshots
    }
}
