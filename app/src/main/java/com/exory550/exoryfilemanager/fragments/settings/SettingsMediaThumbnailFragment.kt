package com.exory550.exoryfilemanager.fragments.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import com.exory550.exoryfilemanager.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsMediaThumbnailFragment : SettingsAbstractFragment() {

    override val preferenceResource: Int = R.xml.preferences_media_thumbnail

    private lateinit var showThumbnailsPreference: SwitchPreferenceCompat
    private lateinit var thumbnailQualityPreference: ListPreference
    private lateinit var thumbnailSizePreference: ListPreference
    private lateinit var cacheThumbnailsPreference: SwitchPreferenceCompat
    private lateinit var generateVideoThumbnailsPreference: SwitchPreferenceCompat
    private lateinit var generateImageThumbnailsPreference: SwitchPreferenceCompat
    private lateinit var generateDocumentThumbnailsPreference: SwitchPreferenceCompat
    private lateinit var thumbnailCacheSizePreference: ListPreference
    private lateinit var clearThumbnailCachePreference: androidx.preference.Preference

    override fun setupPreferences() {
        showThumbnailsPreference = findPreferenceOfType("show_thumbnails") ?: return
        thumbnailQualityPreference = findPreferenceOfType("thumbnail_quality") ?: return
        thumbnailSizePreference = findPreferenceOfType("thumbnail_size") ?: return
        cacheThumbnailsPreference = findPreferenceOfType("cache_thumbnails") ?: return
        generateVideoThumbnailsPreference = findPreferenceOfType("generate_video_thumbnails") ?: return
        generateImageThumbnailsPreference = findPreferenceOfType("generate_image_thumbnails") ?: return
        generateDocumentThumbnailsPreference = findPreferenceOfType("generate_document_thumbnails") ?: return
        thumbnailCacheSizePreference = findPreferenceOfType("thumbnail_cache_size") ?: return
        clearThumbnailCachePreference = findPreferenceCompat("clear_thumbnail_cache") ?: return

        showThumbnailsPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            updateThumbnailPreferencesVisibility(enabled)
            preferenceManager.showThumbnails = enabled
            true
        }

        thumbnailQualityPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.thumbnailQuality = (newValue as String).toInt()
            true
        }

        thumbnailSizePreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.thumbnailSize = (newValue as String).toInt()
            true
        }

        cacheThumbnailsPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.cacheThumbnails = newValue as Boolean
            true
        }

        generateVideoThumbnailsPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.generateVideoThumbnails = newValue as Boolean
            true
        }

        generateImageThumbnailsPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.generateImageThumbnails = newValue as Boolean
            true
        }

        generateDocumentThumbnailsPreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.generateDocumentThumbnails = newValue as Boolean
            true
        }

        thumbnailCacheSizePreference.setOnPreferenceChangeListener { _, newValue ->
            preferenceManager.thumbnailCacheSize = (newValue as String).toInt()
            true
        }

        clearThumbnailCachePreference.setOnPreferenceClickListener {
            clearThumbnailCache()
            true
        }
    }

    override fun updatePreferenceSummaries() {
        val showThumbnails = preferenceManager.showThumbnails
        showThumbnailsPreference.isChecked = showThumbnails

        thumbnailQualityPreference.value = preferenceManager.thumbnailQuality.toString()
        thumbnailQualityPreference.summary = getThumbnailQualitySummary()

        thumbnailSizePreference.value = preferenceManager.thumbnailSize.toString()
        thumbnailSizePreference.summary = getThumbnailSizeSummary()

        cacheThumbnailsPreference.isChecked = preferenceManager.cacheThumbnails
        generateVideoThumbnailsPreference.isChecked = preferenceManager.generateVideoThumbnails
        generateImageThumbnailsPreference.isChecked = preferenceManager.generateImageThumbnails
        generateDocumentThumbnailsPreference.isChecked = preferenceManager.generateDocumentThumbnails

        thumbnailCacheSizePreference.value = preferenceManager.thumbnailCacheSize.toString()
        thumbnailCacheSizePreference.summary = getThumbnailCacheSizeSummary()

        updateThumbnailPreferencesVisibility(showThumbnails)
    }

    private fun getThumbnailQualitySummary(): String {
        return when (preferenceManager.thumbnailQuality) {
            10 -> getString(R.string.quality_low)
            30 -> getString(R.string.quality_medium)
            50 -> getString(R.string.quality_high)
            70 -> getString(R.string.quality_best)
            else -> getString(R.string.quality_medium)
        }
    }

    private fun getThumbnailSizeSummary(): String {
        return when (preferenceManager.thumbnailSize) {
            64 -> "64x64"
            128 -> "128x128"
            256 -> "256x256"
            512 -> "512x512"
            else -> "128x128"
        }
    }

    private fun getThumbnailCacheSizeSummary(): String {
        return when (preferenceManager.thumbnailCacheSize) {
            50 -> "50 MB"
            100 -> "100 MB"
            200 -> "200 MB"
            500 -> "500 MB"
            else -> "100 MB"
        }
    }

    private fun updateThumbnailPreferencesVisibility(enabled: Boolean) {
        thumbnailQualityPreference.isEnabled = enabled
        thumbnailSizePreference.isEnabled = enabled
        cacheThumbnailsPreference.isEnabled = enabled
        generateVideoThumbnailsPreference.isEnabled = enabled
        generateImageThumbnailsPreference.isEnabled = enabled
        generateDocumentThumbnailsPreference.isEnabled = enabled
        thumbnailCacheSizePreference.isEnabled = enabled && preferenceManager.cacheThumbnails
        clearThumbnailCachePreference.isEnabled = enabled && preferenceManager.cacheThumbnails
    }

    private fun clearThumbnailCache() {
        com.exory550.exoryfilemanager.extensions.showToast(requireContext(), R.string.thumbnail_cache_cleared)
    }
}
