package com.exory550.exoryfilemanager.fragments.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.dialogs.PasswordSetupDialog
import com.exory550.exoryfilemanager.security.EncryptionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsEncryptionFragment : SettingsAbstractFragment() {

    override val preferenceResource: Int = R.xml.preferences_encryption

    @Inject
    lateinit var encryptionManager: EncryptionManager

    private lateinit var enableEncryptionPreference: SwitchPreferenceCompat
    private lateinit var encryptionMethodPreference: Preference
    private lateinit var changePasswordPreference: Preference
    private lateinit var encryptMediaPreference: SwitchPreferenceCompat
    private lateinit var encryptDocumentsPreference: SwitchPreferenceCompat
    private lateinit var encryptThumbnailsPreference: SwitchPreferenceCompat
    private lateinit var secureDeletePreference: SwitchPreferenceCompat
    private lateinit var wipeDataPreference: Preference

    override fun setupPreferences() {
        enableEncryptionPreference = findPreferenceOfType("enable_encryption") ?: return
        encryptionMethodPreference = findPreferenceCompat("encryption_method") ?: return
        changePasswordPreference = findPreferenceCompat("change_encryption_password") ?: return
        encryptMediaPreference = findPreferenceOfType("encrypt_media") ?: return
        encryptDocumentsPreference = findPreferenceOfType("encrypt_documents") ?: return
        encryptThumbnailsPreference = findPreferenceOfType("encrypt_thumbnails") ?: return
        secureDeletePreference = findPreferenceOfType("secure_delete") ?: return
        wipeDataPreference = findPreferenceCompat("wipe_encrypted_data") ?: return

        enableEncryptionPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            if (enabled) {
                showEnableEncryptionDialog()
            } else {
                showDisableEncryptionDialog()
            }
            true
        }

        changePasswordPreference.setOnPreferenceClickListener {
            showChangePasswordDialog()
            true
        }

        wipeDataPreference.setOnPreferenceClickListener {
            showWipeDataDialog()
            true
        }
    }

    override fun updatePreferenceSummaries() {
        enableEncryptionPreference.isChecked = preferenceManager.isEncryptionEnabled
        encryptionMethodPreference.summary = getEncryptionMethodSummary()
        encryptMediaPreference.isChecked = preferenceManager.isEncryptMediaEnabled
        encryptDocumentsPreference.isChecked = preferenceManager.isEncryptDocumentsEnabled
        encryptThumbnailsPreference.isChecked = preferenceManager.isEncryptThumbnailsEnabled
        secureDeletePreference.isChecked = preferenceManager.isSecureDeleteEnabled

        val isEnabled = preferenceManager.isEncryptionEnabled
        encryptionMethodPreference.isEnabled = isEnabled
        changePasswordPreference.isEnabled = isEnabled
        encryptMediaPreference.isEnabled = isEnabled
        encryptDocumentsPreference.isEnabled = isEnabled
        encryptThumbnailsPreference.isEnabled = isEnabled
        secureDeletePreference.isEnabled = isEnabled
        wipeDataPreference.isEnabled = isEnabled
    }

    private fun getEncryptionMethodSummary(): String {
        return when (preferenceManager.encryptionMethod) {
            "aes" -> getString(R.string.encryption_aes)
            "chacha" -> getString(R.string.encryption_chacha)
            else -> getString(R.string.encryption_aes)
        }
    }

    private fun showEnableEncryptionDialog() {
        PasswordSetupDialog.setup(requireContext(),
            onSuccess = {
                preferenceManager.isEncryptionEnabled = true
                updatePreferenceSummaries()
                Toast.makeText(requireContext(), R.string.encryption_enabled, Toast.LENGTH_SHORT).show()
            },
            onCancel = {
                enableEncryptionPreference.isChecked = false
            }
        )
    }

    private fun showDisableEncryptionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.disable_encryption)
            .setMessage(R.string.disable_encryption_message)
            .setPositiveButton(R.string.disable) { _, _ ->
                preferenceManager.isEncryptionEnabled = false
                updatePreferenceSummaries()
                Toast.makeText(requireContext(), R.string.encryption_disabled, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                enableEncryptionPreference.isChecked = true
            }
            .show()
    }

    private fun showChangePasswordDialog() {
        PasswordSetupDialog.change(requireContext(),
            onSuccess = {
                Toast.makeText(requireContext(), R.string.password_changed, Toast.LENGTH_SHORT).show()
            },
            onCancel = null
        )
    }

    private fun showWipeDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.wipe_encrypted_data)
            .setMessage(R.string.wipe_encrypted_data_message)
            .setPositiveButton(R.string.wipe) { _, _ ->
                performWipeData()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performWipeData() {
        encryptionManager.wipeAllEncryptedData()
        preferenceManager.isEncryptionEnabled = false
        updatePreferenceSummaries()
        Toast.makeText(requireContext(), R.string.encrypted_data_wiped, Toast.LENGTH_SHORT).show()
    }
}
