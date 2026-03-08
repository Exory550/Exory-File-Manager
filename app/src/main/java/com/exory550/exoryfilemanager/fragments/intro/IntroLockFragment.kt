package com.exory550.exoryfilemanager.fragments.intro

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
import com.exory550.exoryfilemanager.databinding.FragmentIntroLockBinding

class IntroLockFragment : Fragment() {

    private var _binding: FragmentIntroLockBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var ivIcon: ImageView
    private lateinit var ivLock: ImageView
    private lateinit var ivKey: ImageView
    private lateinit var ivShield: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvStatus: TextView
    
    private val handler = Handler(Looper.getMainLooper())
    private var isLocked = true
    private var isAnimating = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroLockBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupAnimations()
        startLockAnimation()
    }
    
    private fun initViews() {
        ivIcon = binding.ivIcon
        ivLock = binding.ivLock
        ivKey = binding.ivKey
        ivShield = binding.ivShield
        tvTitle = binding.tvTitle
        tvDescription = binding.tvDescription
        tvStatus = binding.tvStatus
        
        // Set text
        tvTitle.text = getString(R.string.intro_lock_title)
        tvDescription.text = getString(R.string.intro_lock_description)
        
        // Initial states
        ivLock.alpha = 1f
        ivKey.alpha = 0f
        ivKey.translationY = 100f
        ivShield.scaleX = 0f
        ivShield.scaleY = 0f
        ivShield.alpha = 0f
    }
    
    private fun setupAnimations() {
        // Icon pulse animation
        val pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        ivIcon.startAnimation(pulseAnimation)
        
        // Lock subtle animation
        val lockAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_slow)
        ivLock.startAnimation(lockAnimation)
    }
    
    private fun startLockAnimation() {
        // Phase 1: Lock is secure
        tvStatus.text = getString(R.string.lock_secure)
        binding.progressIndicator.progress = 25
        
        handler.postDelayed({
            animateKeyAppear()
        }, 1500)
    }
    
    private fun animateKeyAppear() {
        tvStatus.text = getString(R.string.lock_unlocking)
        binding.progressIndicator.progress = 50
        
        // Key appears from bottom
        ivKey.visibility = View.VISIBLE
        
        ivKey.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                animateKeyInsert()
            }
            .start()
    }
    
    private fun animateKeyInsert() {
        // Key moves to lock
        val lockX = ivLock.x + ivLock.width / 2f
        val keyX = ivKey.x + ivKey.width / 2f
        val translationX = lockX - keyX
        
        ivKey.animate()
            .translationXBy(translationX)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                animateLockOpen()
            }
            .start()
    }
    
    private fun animateLockOpen() {
        tvStatus.text = getString(R.string.lock_opening)
        binding.progressIndicator.progress = 75
        
        // Lock disappears (opens)
        ivLock.animate()
            .alpha(0f)
            .scaleX(0f)
            .scaleY(0f)
            .rotationBy(90f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                ivLock.visibility = View.GONE
                animateShieldAppear()
            }
            .start()
        
        // Key also fades out
        ivKey.animate()
            .alpha(0f)
            .setDuration(400)
            .start()
    }
    
    private fun animateShieldAppear() {
        tvStatus.text = getString(R.string.lock_protected)
        binding.progressIndicator.progress = 100
        
        // Shield appears and expands
        ivShield.visibility = View.VISIBLE
        
        val scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
            ivShield,
            PropertyValuesHolder.ofFloat("scaleX", 0f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 0f, 1f),
            PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        )
        scaleAnimator.duration = 600
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.start()
        
        // Pulse effect for shield
        handler.postDelayed({
            ivShield.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction {
                    ivShield.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }, 800)
        
        // Complete
        tvStatus.text = getString(R.string.lock_complete)
    }
    
    fun resetAnimation() {
        if (isAnimating) return
        
        isAnimating = true
        
        // Reset states
        ivLock.visibility = View.VISIBLE
        ivKey.visibility = View.GONE
        ivShield.visibility = View.GONE
        
        ivLock.alpha = 1f
        ivLock.scaleX = 1f
        ivLock.scaleY = 1f
        ivLock.rotation = 0f
        
        ivKey.alpha = 0f
        ivKey.translationX = 0f
        ivKey.translationY = 100f
        
        ivShield.alpha = 0f
        ivShield.scaleX = 0f
        ivShield.scaleY = 0f
        
        tvStatus.text = getString(R.string.lock_secure)
        binding.progressIndicator.progress = 0
        
        handler.postDelayed({
            isAnimating = false
            startLockAnimation()
        }, 500)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
    
    companion object {
        fun newInstance(): IntroLockFragment {
            return IntroLockFragment()
        }
    }
}
