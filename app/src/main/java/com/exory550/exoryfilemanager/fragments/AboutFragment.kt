package com.exory550.exoryfilemanager.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.databinding.FragmentAboutBinding
import com.exory550.exoryfilemanager.dialogs.ConfirmationDialog
import com.exory550.exoryfilemanager.dialogs.LicensesDialog
import com.exory550.exoryfilemanager.extensions.openUrl
import com.exory550.exoryfilemanager.extensions.shareText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        binding.appVersionText.text = String.format(
            "%s (%d)",
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )

        binding.buildTypeText.text = if (BuildConfig.DEBUG) "Debug" else "Release"
    }

    private fun setupClickListeners() {
        binding.privacyPolicyCard.setOnClickListener {
            openUrl("https://exory550.com/privacy")
        }

        binding.termsOfServiceCard.setOnClickListener {
            openUrl("https://exory550.com/terms")
        }

        binding.openSourceLicensesCard.setOnClickListener {
            showLicensesDialog()
        }

        binding.rateAppCard.setOnClickListener {
            rateApp()
        }

        binding.shareAppCard.setOnClickListener {
            shareApp()
        }

        binding.checkUpdatesCard.setOnClickListener {
            checkForUpdates()
        }

        binding.feedbackCard.setOnClickListener {
            sendFeedback()
        }

        binding.developerInfoLayout.setOnClickListener {
            toggleDeveloperInfo()
        }
    }

    private fun showLicensesDialog() {
        LicensesDialog.show(childFragmentManager)
    }

    private fun rateApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")))
        }
    }

    private fun shareApp() {
        val shareText = getString(
            R.string.share_app_text,
            "https://play.google.com/store/apps/details?id=${requireContext().packageName}"
        )
        requireContext().shareText(shareText, getString(R.string.share_app_title))
    }

    private fun checkForUpdates() {
        com.exory550.exoryfilemanager.extensions.showToast(requireContext(), R.string.checking_updates)
    }

    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@exory550.com"))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject, BuildConfig.VERSION_NAME))
        }

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback)))
        } catch (e: Exception) {
            com.exory550.exoryfilemanager.extensions.showToast(requireContext(), R.string.no_email_app_found)
        }
    }

    private fun toggleDeveloperInfo() {
        if (binding.developerInfoContent.visibility == View.VISIBLE) {
            binding.developerInfoContent.visibility = View.GONE
            binding.expandIcon.setImageResource(R.drawable.ic_expand_more)
        } else {
            binding.developerInfoContent.visibility = View.VISIBLE
            binding.expandIcon.setImageResource(R.drawable.ic_expand_less)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }
}
