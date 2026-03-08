package com.exory550.exoryfilemanager.fragments.settings

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.dialogs.PasswordSetupDialog
import com.exory550.exoryfilemanager.dialogs.PinSetupDialog
import com.exory550.exoryfilemanager.dialogs.PatternSetupDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsLockFragment : SettingsAbstractFragment() {

    override val preferenceResource: Int = R.xml.preferences_lock

    private lateinit var enableLockPreference: SwitchPreferenceCompat
    private lateinit var lockMethodPreference: ListPreference
    private lateinit var setupPasswordPreference: Preference
    private lateinit var setupPinPreference: Preference
    private lateinit var setupPatternPreference: Preference
    private lateinit var enableBiometricPreference: SwitchPreferenceCompat
    private lateinit var lockTimeoutPreference: ListPreference
    private lateinit var autoLockPreference: SwitchPreferenceCompat
    private lateinit var lockOnScreenOffPreference: SwitchPreferenceCompat
    private lateinit var showNotificationsPreference: SwitchPreferenceCompat
    private lateinit var hideContentPreference: SwitchPreferenceCompat

    override fun setupPreferences() {
        enableLockPreference = findPreferenceOfType("enable_app_lock") ?: return
        lockMethodPreference = findPreferenceOfType("lock_method") ?: return
        setupPasswordPreference = findPreferenceCompat("setup_password") ?: return
        setupPinPreference = findPreferenceCompat("setup_pin") ?: return
        setupPatternPreference = findPreferenceCompat("setup_pattern") ?: return
        enableBiometricPreference = findPreferenceOfType("enable_biometric") ?: return
        lockTimeoutPreference = findPreferenceOfType("lock_timeout") ?: return
        autoLockPreference = findPreferenceOfType("auto_lock") ?: return
        lockOnScreenOffPreference = findPreferenceOfType("lock_on_screen_off") ?: return
        showNotificationsPreference = findPreferenceOfType("show_notifications") ?: return
        hideContentPreference = findPreferenceOfType("hide_notification_content") ?: return

        enableLockPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            preferenceManager.isAppLockEnabled = enabled
            updateLockPreferencesVisibility(enabled)
            true
        }

        lockMethodPreference.setOnPreferenceChangeListener { _, newValue ->
            val method = newValue as String
            preferenceManager.lockMethod = method
            updateMethodSpecificPreferences(method)
            true
        }

        setupPasswordPreference.setOnPreferenceClickListener {
            showSetupPasswordDialog()
            true
        }

        setupPinPreference.setOnPreferenceClickListener {
            showSetupPinDialog()
            true
        }

        setupPatternPreference.setOnPreferenceClickListener {
            showSetupPatternDialog()
            true
        }

        enableBiometricPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            if (enabled && !isBiometricAvailable()) {
                Toast.makeText(requireContext(), R.string.biometric_not_available, Toast.LENGTH_LONG).show()
                false
            } else {
                preferenceManager.isBiometricEnabled = enabled
                true
            }
        }

        lockTimeoutPreference.setOnPreferenceChangeListener { _, newValue ->
            val timeout = (newValue as String).toLong()
            preferenceManager.lockTimeout = timeout
            true
        }

        autoLockPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.autoLock = newValue as Boolean
            true
        }

        lockOnScreenOffPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.lockOnScreenOff = newValue as Boolean
            true
        }

        showNotificationsPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.showLockNotifications = newValue as Boolean
            updateHideContentPreferenceVisibility(newValue as Boolean)
            true
        }

        hideContentPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.hideNotificationContent = newValue as Boolean
            true
        }
    }

    override fun updatePreferenceSummaries() {
        val isEnabled = preferenceManager.isAppLockEnabled
        enableLockPreference.isChecked = isEnabled

        lockMethodPreference.value = preferenceManager.lockMethod
        lockMethodPreference.summary = lockMethodPreference.entry

        enableBiometricPreference.isChecked = preferenceManager.isBiometricEnabled
        enableBiometricPreference.isEnabled = isBiometricAvailable()

        lockTimeoutPreference.value = preferenceManager.lockTimeout.toString()
        lockTimeoutPreference.summary = lockTimeoutPreference.entry

        autoLockPreference.isChecked = preferenceManager.autoLock
        lockOnScreenOffPreference.isChecked = preferenceManager.lockOnScreenOff
        showNotificationsPreference.isChecked = preferenceManager.showLockNotifications
        hideContentPreference.isChecked = preferenceManager.hideNotificationContent

        updateLockPreferencesVisibility(isEnabled)
        updateMethodSpecificPreferences(preferenceManager.lockMethod)
        updateHideContentPreferenceVisibility(preferenceManager.showLockNotifications)
    }

    private fun updateLockPreferencesVisibility(enabled: Boolean) {
        lockMethodPreference.isEnabled = enabled
        enableBiometricPreference.isEnabled = enabled && isBiometricAvailable()
        lockTimeoutPreference.isEnabled = enabled
        autoLockPreference.isEnabled = enabled
        lockOnScreenOffPreference.isEnabled = enabled
        showNotificationsPreference.isEnabled = enabled
    }

    private fun updateMethodSpecificPreferences(method: String) {
        val passwordSetupNeeded = !preferenceManager.isPasswordSet
        val pinSetupNeeded = !preferenceManager.isPinSet
        val patternSetupNeeded = !preferenceManager.isPatternSet

        setupPasswordPreference.isVisible = method == "password"
        setupPinPreference.isVisible = method == "pin"
        setupPatternPreference.isVisible = method == "pattern"

        if (method == "password") {
            setupPasswordPreference.summary = if (passwordSetupNeeded) {
                getString(R.string.password_not_set)
            } else {
                getString(R.string.password_set)
            }
        } else if (method == "pin") {
            setupPinPreference.summary = if (pinSetupNeeded) {
                getString(R.string.pin_not_set)
            } else {
                getString(R.string.pin_set)
            }
        } else if (method == "pattern") {
            setupPatternPreference.summary = if (patternSetupNeeded) {
                getString(R.string.pattern_not_set)
            } else {
                getString(R.string.pattern_set)
            }
        }
    }

    private fun updateHideContentPreferenceVisibility(showNotifications: Boolean) {
        hideContentPreference.isEnabled = showNotifications
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(requireContext())
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    private fun showSetupPasswordDialog() {
        PasswordSetupDialog.setup(requireContext(),
            onSuccess = {
                preferenceManager.isPasswordSet = true
                updateMethodSpecificPreferences("password")
                Toast.makeText(requireContext(), R.string.password_set_success, Toast.LENGTH_SHORT).show()
            },
            onCancel = null
        )
    }

    private fun showSetupPinDialog() {
        PinSetupDialog.setup(requireContext(),
            onSuccess = {
                preferenceManager.isPinSet = true
                updateMethodSpecificPreferences("pin")
                Toast.makeText(requireContext(), R.string.pin_set_success, Toast.LENGTH_SHORT).show()
            },
            onCancel = null
        )
    }

    private fun showSetupPatternDialog() {
        PatternSetupDialog.setup(requireContext(),
            onSuccess = {
                preferenceManager.isPatternSet = true
                updateMethodSpecificPreferences("pattern")
                Toast.makeText(requireContext(), R.string.pattern_set_success, Toast.LENGTH_SHORT).show()
            },
            onCancel = null
        )
    }
}
