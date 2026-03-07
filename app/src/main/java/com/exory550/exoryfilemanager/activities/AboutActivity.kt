package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.lifecycle.lifecycleScope
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.databinding.ActivityAboutBinding
import com.exory550.exoryfilemanager.utils.AppUtils
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyMaterialTransitions()
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupContent()
        setupClickListeners()
    }
    
    private fun applyMaterialTransitions() {
        window.apply {
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                duration = 300
            }
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
                duration = 300
            }
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.about_title)
        }
    }
    
    private fun setupContent() {
        binding.apply {
            appVersionText.text = String.format("Version %s (%d)", 
                BuildConfig.VERSION_NAME, 
                BuildConfig.VERSION_CODE
            )
            
            buildTypeText.text = if (BuildConfig.DEBUG) "Debug" else "Release"
            buildDateText.text = AppUtils.getBuildDate(this@AboutActivity)
            
            // Load app icon or use default
            try {
                appIconImage.setImageDrawable(packageManager.getApplicationIcon(packageName))
            } catch (e: Exception) {
                appIconImage.setImageResource(R.mipmap.ic_launcher)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            privacyPolicyCard.setOnClickListener {
                showPrivacyPolicy()
            }
            
            termsOfServiceCard.setOnClickListener {
                showTermsOfService()
            }
            
            openSourceLicensesCard.setOnClickListener {
                showOpenSourceLicenses()
            }
            
            rateAppCard.setOnClickListener {
                rateApp()
            }
            
            shareAppCard.setOnClickListener {
                shareApp()
            }
            
            checkUpdatesCard.setOnClickListener {
                checkForUpdates()
            }
            
            feedbackCard.setOnClickListener {
                sendFeedback()
            }
            
            // Developer info expand/collapse
            developerInfoLayout.setOnClickListener {
                val isVisible = developerInfoContent.visibility == android.view.View.VISIBLE
                developerInfoContent.visibility = if (isVisible) android.view.View.GONE else android.view.View.VISIBLE
                expandIcon.setImageResource(
                    if (isVisible) R.drawable.ic_expand_more 
                    else R.drawable.ic_expand_less
                )
            }
        }
    }
    
    private fun showPrivacyPolicy() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.privacy_policy_title)
            .setMessage(R.string.privacy_policy_content)
            .setPositiveButton(R.string.ok, null)
            .setNeutralButton(R.string.open_website) { _, _ ->
                AppUtils.openUrl(this, "https://exory550.com/privacy")
            }
            .show()
    }
    
    private fun showTermsOfService() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.terms_of_service_title)
            .setMessage(R.string.terms_of_service_content)
            .setPositiveButton(R.string.ok, null)
            .setNeutralButton(R.string.open_website) { _, _ ->
                AppUtils.openUrl(this, "https://exory550.com/terms")
            }
            .show()
    }
    
    private fun showOpenSourceLicenses() {
        startActivity(Intent(this, OssLicensesActivity::class.java))
    }
    
    private fun rateApp() {
        AppUtils.openUrl(this, "market://details?id=$packageName")
    }
    
    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, 
                getString(R.string.share_app_text, 
                    "https://play.google.com/store/apps/details?id=$packageName"
                )
            )
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_title)))
    }
    
    private fun checkForUpdates() {
        binding.checkUpdatesCard.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val hasUpdate = withContext(Dispatchers.IO) {
                    AppUtils.checkForUpdates(this@AboutActivity)
                }
                
                if (hasUpdate) {
                    showUpdateAvailableDialog()
                } else {
                    showNoUpdateDialog()
                }
            } catch (e: Exception) {
                showUpdateCheckError()
            } finally {
                binding.checkUpdatesCard.isEnabled = true
            }
        }
    }
    
    private fun showUpdateAvailableDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.update_available_title)
            .setMessage(R.string.update_available_message)
            .setPositiveButton(R.string.update) { _, _ ->
                AppUtils.openUrl(this, "market://details?id=$packageName")
            }
            .setNegativeButton(R.string.later, null)
            .show()
    }
    
    private fun showNoUpdateDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.no_update_title)
            .setMessage(R.string.no_update_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    private fun showUpdateCheckError() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error)
            .setMessage(R.string.update_check_error)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@exory550.com"))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject, 
                BuildConfig.VERSION_NAME))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_email_template))
        }
        
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback)))
        } catch (e: Exception) {
            AppUtils.showToast(this, R.string.no_email_app_found)
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AboutActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
