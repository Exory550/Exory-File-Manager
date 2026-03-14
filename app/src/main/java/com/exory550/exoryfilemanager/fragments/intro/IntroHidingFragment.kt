package com.exory550.exoryfilemanager.fragments.intro

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.databinding.FragmentIntroHidingBinding

class IntroHidingFragment : Fragment() {

    private var _binding: FragmentIntroHidingBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var ivIcon: ImageView
    private lateinit var ivFile1: ImageView
    private lateinit var ivFile2: ImageView
    private lateinit var ivFile3: ImageView
    private lateinit var ivVault: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvStatus: TextView
    
    private val handler = Handler(Looper.getMainLooper())
    private var currentStep = 0
    private val totalSteps = 4
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroHidingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupAnimations()
        startHidingAnimation()
    }
    
    private fun initViews() {
        ivIcon = binding.ivIcon
        ivFile1 = binding.ivFile1
        ivFile2 = binding.ivFile2
        ivFile3 = binding.ivFile3
        ivVault = binding.ivVault
        tvTitle = binding.tvTitle
        tvDescription = binding.tvDescription
        tvStatus = binding.tvStatus
        
        // Set text
        tvTitle.text = getString(R.string.intro_hiding_title)
        tvDescription.text = getString(R.string.intro_hiding_description)
        
        // Initially hide vault
        ivVault.alpha = 0f
        ivVault.scaleX = 0f
        ivVault.scaleY = 0f
    }
    
    private fun setupAnimations() {
        // Icon rotation animation
        val rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_slow)
        ivIcon.startAnimation(rotateAnimation)
        
        // Files floating animation
        val floatAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.floating)
        ivFile1.startAnimation(floatAnimation)
        ivFile2.startAnimation(floatAnimation)
        ivFile3.startAnimation(floatAnimation)
    }
    
    private fun startHidingAnimation() {
        // Step 1: Show files
        handler.postDelayed({
            updateStep(1, R.string.hiding_step_selecting)
            animateFilesIn()
        }, 500)
    }
    
    private fun animateFilesIn() {
        // Animate file 1
        ivFile1.alpha = 0f
        ivFile1.scaleX = 0f
        ivFile1.scaleY = 0f
        ivFile1.visibility = View.VISIBLE
        
        ivFile1.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        
        // Animate file 2 with delay
        handler.postDelayed({
            ivFile2.alpha = 0f
            ivFile2.scaleX = 0f
            ivFile2.scaleY = 0f
            ivFile2.visibility = View.VISIBLE
            
            ivFile2.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }, 200)
        
        // Animate file 3 with more delay
        handler.postDelayed({
            ivFile3.alpha = 0f
            ivFile3.scaleX = 0f
            ivFile3.scaleY = 0f
            ivFile3.visibility = View.VISIBLE
            
            ivFile3.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }, 400)
        
        // Schedule next step
        handler.postDelayed({
            updateStep(2, R.string.hiding_step_preparing)
            animateVaultAppear()
        }, 1500)
    }
    
    private fun animateVaultAppear() {
        // Show vault
        ivVault.visibility = View.VISIBLE
        
        ivVault.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        
        // Schedule next step
        handler.postDelayed({
            updateStep(3, R.string.hiding_step_hiding)
            animateFilesToVault()
        }, 1000)
    }
    
    private fun animateFilesToVault() {
        // Get vault position
        val vaultX = ivVault.x + ivVault.width / 2f
        val vaultY = ivVault.y + ivVault.height / 2f
        
        // Animate file 1 to vault
        animateFileToVault(ivFile1, vaultX, vaultY, 0)
        
        // Animate file 2 to vault
        handler.postDelayed({
            animateFileToVault(ivFile2, vaultX, vaultY, 1)
        }, 300)
        
        // Animate file 3 to vault
        handler.postDelayed({
            animateFileToVault(ivFile3, vaultX, vaultY, 2)
        }, 600)
    }
    
    private fun animateFileToVault(file: ImageView, targetX: Float, targetY: Float, index: Int) {
        val startX = file.x + file.width / 2f
        val startY = file.y + file.height / 2f
        
        // Create path animation
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 800
        animator.interpolator = AccelerateDecelerateInterpolator()
        
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            
            // Calculate intermediate position with curve
            val currentX = startX + (targetX - startX) * progress
            val currentY = startY + (targetY - startY) * progress - (progress * (1 - progress) * 200)
            
            file.x = currentX - file.width / 2f
            file.y = currentY - file.height / 2f
            
            // Scale down as it approaches vault
            file.scaleX = 1f - progress * 0.5f
            file.scaleY = 1f - progress * 0.5f
            
            // Fade out at the end
            if (progress > 0.7f) {
                file.alpha = 1f - ((progress - 0.7f) / 0.3f)
            }
        }
        
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                file.visibility = View.GONE
                
                // If this is the last file, show completion
                if (index == 2) {
                    updateStep(4, R.string.hiding_step_complete)
                    
                    // Pulse vault
                    ivVault.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(200)
                        .withEndAction {
                            ivVault.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start()
                        }
                        .start()
                    
                    // Show completion message
                    tvStatus.text = getString(R.string.files_hidden_complete)
                }
            }
        })
        
        animator.start()
    }
    
    private fun updateStep(step: Int, statusResId: Int) {
        currentStep = step
        tvStatus.text = getString(statusResId)
        
        // Update progress indicator
        binding.progressIndicator.progress = (step * 100 / totalSteps)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
    
    companion object {
        fun newInstance(): IntroHidingFragment {
            return IntroHidingFragment()
        }
    }
}
