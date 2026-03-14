package com.exory550.exoryfilemanager.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners(view)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<View>(R.id.privacyPolicyCard)?.setOnClickListener {
            openUrl("https://exory550.com/privacy")
        }
        view.findViewById<View>(R.id.termsOfServiceCard)?.setOnClickListener {
            openUrl("https://exory550.com/terms")
        }
        view.findViewById<View>(R.id.rateAppCard)?.setOnClickListener {
            rateApp()
        }
        view.findViewById<View>(R.id.shareAppCard)?.setOnClickListener {
            shareApp()
        }
        view.findViewById<View>(R.id.checkUpdatesCard)?.setOnClickListener {
            Toast.makeText(requireContext(), R.string.checking_updates, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.feedbackCard)?.setOnClickListener {
            sendFeedback()
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.cannot_open_url, Toast.LENGTH_SHORT).show()
        }
    }

    private fun rateApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")))
        }
    }

    private fun shareApp() {
        val shareText = "Check out Exory File Manager: https://play.google.com/store/apps/details?id=${requireContext().packageName}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_app_title)))
    }

    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@exory550.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback - Exory File Manager v${BuildConfig.VERSION_NAME}")
        }
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.no_email_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(): AboutFragment = AboutFragment()
    }
}
