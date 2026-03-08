package com.exory550.exoryfilemanager.fragments.intro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.databinding.FragmentIntroEncryptingBinding

class IntroEncryptingFragment : Fragment() {

    private var _binding: FragmentIntroEncryptingBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressStatus: TextView
    
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null
    private var currentProgress = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroEncryptingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupAnimations()
        startProgressSimulation()
    }
    
    private fun initViews() {
        ivIcon = binding.ivIcon
        tvTitle = binding.tvTitle
        tvDescription = binding.tvDescription
        progressBar = binding.progressBar
        tvProgressPercent = binding.tvProgressPercent
        tvProgressStatus = binding.tvProgressStatus
        
        // Set text
        tvTitle.text = getString(R.string.intro_encrypting_title)
        tvDescription.text = getString(R.string.intro_encrypting_description)
    }
    
    private fun setupAnimations() {
        // Icon pulse animation
        val pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        ivIcon.startAnimation(pulseAnimation)
        
        // Progress bar animation
        progressBar.alpha = 0f
        progressBar.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }
    
    private fun startProgressSimulation() {
        progressRunnable = object : Runnable {
            override fun run() {
                if (currentProgress < 100) {
                    // Simulate progress with varying speeds
                    currentProgress += when {
                        currentProgress < 30 -> 5
                        currentProgress < 60 -> 3
                        currentProgress < 80 -> 2
                        else -> 1
                    }
                    
                    if (currentProgress > 100) currentProgress = 100
                    
                    updateProgress(currentProgress)
                    
                    // Schedule next update
                    handler.postDelayed(this, 200)
                } else {
                    // Complete
                    tvProgressStatus.text = getString(R.string.encryption_complete)
                }
            }
        }
        
        handler.post(progressRunnable!!)
    }
    
    private fun updateProgress(progress: Int) {
        progressBar.progress = progress
        tvProgressPercent.text = "$progress%"
        
        tvProgressStatus.text = when {
            progress < 20 -> getString(R.string.encrypting_files)
            progress < 40 -> getString(R.string.encrypting_folders)
            progress < 60 -> getString(R.string.encrypting_metadata)
            progress < 80 -> getString(R.string.verifying_encryption)
            progress < 100 -> getString(R.string.finalizing)
            else -> getString(R.string.encryption_complete)
        }
    }
    
    fun setProgress(progress: Int) {
        currentProgress = progress.coerceIn(0, 100)
        updateProgress(currentProgress)
    }
    
    fun resetProgress() {
        currentProgress = 0
        progressBar.progress = 0
        tvProgressPercent.text = "0%"
        tvProgressStatus.text = getString(R.string.encrypting_files)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(progressRunnable!!)
        _binding = null
    }
    
    companion object {
        fun newInstance(): IntroEncryptingFragment {
            return IntroEncryptingFragment()
        }
    }
}
