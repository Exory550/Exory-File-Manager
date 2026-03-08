package com.exory550.exoryfilemanager.fragments.intro

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.databinding.FragmentIntroOtherBinding

class IntroOtherFragment : Fragment() {

    private var _binding: FragmentIntroOtherBinding? = null
    private val binding get() = _binding!!

    private lateinit var ivIcon: ImageView
    private lateinit var ivCloud: ImageView
    private lateinit var ivShare: ImageView
    private lateinit var ivCompress: ImageView
    private lateinit var ivSearch: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvFeature1: TextView
    private lateinit var tvFeature2: TextView
    private lateinit var tvFeature3: TextView
    private lateinit var tvFeature4: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroOtherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupAnimations()
        animateFeatures()
    }

    private fun initViews() {
        ivIcon = binding.ivIcon
        ivCloud = binding.ivCloud
        ivShare = binding.ivShare
        ivCompress = binding.ivCompress
        ivSearch = binding.ivSearch
        tvTitle = binding.tvTitle
        tvDescription = binding.tvDescription
        tvFeature1 = binding.tvFeature1
        tvFeature2 = binding.tvFeature2
        tvFeature3 = binding.tvFeature3
        tvFeature4 = binding.tvFeature4

        tvTitle.text = getString(R.string.intro_other_title)
        tvDescription.text = getString(R.string.intro_other_description)
        tvFeature1.text = getString(R.string.feature_cloud_sync)
        tvFeature2.text = getString(R.string.feature_share)
        tvFeature3.text = getString(R.string.feature_compress)
        tvFeature4.text = getString(R.string.feature_search)

        ivCloud.alpha = 0f
        ivShare.alpha = 0f
        ivCompress.alpha = 0f
        ivSearch.alpha = 0f
        tvFeature1.alpha = 0f
        tvFeature2.alpha = 0f
        tvFeature3.alpha = 0f
        tvFeature4.alpha = 0f
    }

    private fun setupAnimations() {
        val rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_slow)
        ivIcon.startAnimation(rotateAnimation)

        val pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        ivIcon.startAnimation(pulseAnimation)
    }

    private fun animateFeatures() {
        ivCloud.animate()
            .alpha(1f)
            .translationYBy(-20f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        tvFeature1.animate()
            .alpha(1f)
            .translationXBy(20f)
            .setDuration(600)
            .setStartDelay(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        ivShare.animate()
            .alpha(1f)
            .translationYBy(-20f)
            .setDuration(600)
            .setStartDelay(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        tvFeature2.animate()
            .alpha(1f)
            .translationXBy(20f)
            .setDuration(600)
            .setStartDelay(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        ivCompress.animate()
            .alpha(1f)
            .translationYBy(-20f)
            .setDuration(600)
            .setStartDelay(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        tvFeature3.animate()
            .alpha(1f)
            .translationXBy(20f)
            .setDuration(600)
            .setStartDelay(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        ivSearch.animate()
            .alpha(1f)
            .translationYBy(-20f)
            .setDuration(600)
            .setStartDelay(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        tvFeature4.animate()
            .alpha(1f)
            .translationXBy(20f)
            .setDuration(600)
            .setStartDelay(1400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        val scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
            ivCloud,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f, 1f)
        )
        scaleAnimator.duration = 2000
        scaleAnimator.repeatCount = ObjectAnimator.INFINITE
        scaleAnimator.start()

        val shareAnimator = ObjectAnimator.ofPropertyValuesHolder(
            ivShare,
            PropertyValuesHolder.ofFloat("rotation", 0f, 360f)
        )
        shareAnimator.duration = 3000
        shareAnimator.repeatCount = ObjectAnimator.INFINITE
        shareAnimator.start()

        val compressAnimator = ObjectAnimator.ofPropertyValuesHolder(
            ivCompress,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 0.8f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 0.8f, 1f)
        )
        compressAnimator.duration = 1500
        compressAnimator.repeatCount = ObjectAnimator.INFINITE
        compressAnimator.start()

        val searchAnimator = ObjectAnimator.ofPropertyValuesHolder(
            ivSearch,
            PropertyValuesHolder.ofFloat("translationX", -10f, 10f, -10f)
        )
        searchAnimator.duration = 2000
        searchAnimator.repeatCount = ObjectAnimator.INFINITE
        searchAnimator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): IntroOtherFragment {
            return IntroOtherFragment()
        }
    }
}
