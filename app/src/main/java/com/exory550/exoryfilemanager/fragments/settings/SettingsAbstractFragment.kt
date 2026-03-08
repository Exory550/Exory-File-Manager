package com.exory550.exoryfilemanager.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.preference.*
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class SettingsAbstractFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    protected abstract val preferenceResource: Int

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferenceResource, rootKey)
        setupPreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        updatePreferenceSummaries()
    }

    protected open fun setupPreferences() {}

    protected open fun setupClickListeners() {}

    protected open fun updatePreferenceSummaries() {}

    protected fun findPreferenceCompat(key: String): Preference? {
        return findPreference(key)
    }

    protected inline fun <reified T : Preference> findPreferenceOfType(key: String): T? {
        return findPreference(key) as? T
    }

    protected fun setPreferenceVisible(key: String, visible: Boolean) {
        findPreferenceCompat(key)?.isVisible = visible
    }

    protected fun setPreferenceEnabled(key: String, enabled: Boolean) {
        findPreferenceCompat(key)?.isEnabled = enabled
    }

    protected fun setPreferenceSummary(key: String, summary: String) {
        findPreferenceCompat(key)?.summary = summary
    }

    protected fun setPreferenceSummary(key: String, summaryRes: Int) {
        findPreferenceCompat(key)?.setSummary(summaryRes)
    }

    protected fun setListPreferenceValue(key: String, value: String) {
        (findPreference(key) as? ListPreference)?.value = value
    }

    protected fun getListPreferenceValue(key: String): String? {
        return (findPreference(key) as? ListPreference)?.value
    }

    protected fun setSwitchPreferenceValue(key: String, checked: Boolean) {
        (findPreference(key) as? SwitchPreferenceCompat)?.isChecked = checked
    }

    protected fun getSwitchPreferenceValue(key: String): Boolean {
        return (findPreference(key) as? SwitchPreferenceCompat)?.isChecked ?: false
    }

    protected fun setEditTextPreferenceValue(key: String, value: String) {
        (findPreference(key) as? EditTextPreference)?.text = value
    }

    protected fun getEditTextPreferenceValue(key: String): String? {
        return (findPreference(key) as? EditTextPreference)?.text
    }

    protected fun showDialog(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    protected fun showDialog(titleRes: Int, messageRes: Int) {
        showDialog(getString(titleRes), getString(messageRes))
    }

    protected fun showConfirmDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> onConfirm() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    protected fun showConfirmDialog(
        titleRes: Int,
        messageRes: Int,
        onConfirm: () -> Unit
    ) {
        showConfirmDialog(getString(titleRes), getString(messageRes), onConfirm)
    }

    protected fun restartActivity() {
        requireActivity().recreate()
    }

    protected fun openUrl(url: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            com.exory550.exoryfilemanager.extensions.showToast(requireContext(), R.string.cannot_open_url)
        }
    }
}
