package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.viewpager2.widget.ViewPager2
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.IntroPagerAdapter
import com.exory550.exoryfilemanager.databinding.ActivityIntroBinding
import com.exory550.exoryfilemanager.extensions.addFlagsSecure
import com.exory550.exoryfilemanager.extensions.setupWithViewPager
import com.exory550.exoryfilemanager.models.IntroItem
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IntroActivity : BaseAbstractActivity() {

    override val layoutRes: Int = R.layout.activity_intro
    
    private lateinit var binding: ActivityIntroBinding
    private lateinit var pagerAdapter: IntroPagerAdapter
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var currentPosition = 0
    private val introItems = listOf(
        IntroItem(
            titleRes = R.string.intro_title_1,
            descriptionRes = R.string.intro_description_1,
            imageRes = R.drawable.ic_intro_files,
            backgroundColorRes = R.color.intro_1
        ),
        IntroItem(
            titleRes = R.string.intro_title_2,
            descriptionRes = R.string.intro_description_2,
            imageRes = R.drawable.ic_intro_organize,
            backgroundColorRes = R.color.intro_2
        ),
        IntroItem(
            titleRes = R.string.intro_title_3,
            descriptionRes = R.string.intro_description_3,
            imageRes = R.drawable.ic_intro_secure,
            backgroundColorRes = R.color.intro_3
        ),
        IntroItem(
            titleRes = R.string.intro_title_4,
            descriptionRes = R.string.intro_description_4,
            imageRes = R.drawable.ic_intro_cloud,
            backgroundColorRes = R.color.intro_4
        )
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        applyMaterialTransitions()
        super.onCreate(savedInstanceState)
        
        // Keep screen on during intro
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Add secure flag if needed
        if (preferenceManager.preventScreenshots) {
            addFlagsSecure()
        }
    }
    
    override fun initializeViews() {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        setupIndicators()
        setupButtons()
    }
    
    private fun applyMaterialTransitions() {
        window.apply {
            enterTransition = MaterialFadeThrough().apply {
                duration = 500
            }
            exitTransition = MaterialFadeThrough().apply {
                duration = 500
            }
        }
    }
    
    private fun setupViewPager() {
        pagerAdapter = IntroPagerAdapter(this, introItems)
        
        binding.viewPager.apply {
            adapter = pagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentPosition = position
                    updateIndicators(position)
                    updateButtons(position)
                    updateBackgroundColor(position)
                }
            })
        }
    }
    
    private fun setupIndicators() {
        binding.indicatorContainer.setupWithViewPager(binding.viewPager, introItems.size)
    }
    
    private fun updateIndicators(position: Int) {
        for (i in 0 until binding.indicatorContainer.childCount) {
            val indicator = binding.indicatorContainer.getChildAt(i)
            indicator.isSelected = i == position
        }
    }
    
    private fun setupButtons() {
        binding.apply {
            btnNext.setOnClickListener {
                if (currentPosition < introItems.size - 1) {
                    viewPager.currentItem = currentPosition + 1
                } else {
                    completeIntro()
                }
            }
            
            btnSkip.setOnClickListener {
                completeIntro()
            }
            
            btnPrev.setOnClickListener {
                if (currentPosition > 0) {
                    viewPager.currentItem = currentPosition - 1
                }
            }
        }
    }
    
    private fun updateButtons(position: Int) {
        binding.apply {
            if (position == introItems.size - 1) {
                btnNext.text = getString(R.string.get_started)
                btnSkip.visibility = View.GONE
            } else {
                btnNext.text = getString(R.string.next)
                btnSkip.visibility = View.VISIBLE
            }
            
            btnPrev.visibility = if (position > 0) View.VISIBLE else View.GONE
            
            // Animate button changes
            btnNext.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .duration = 200
            
            btnPrev.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .duration = 200
        }
    }
    
    private fun updateBackgroundColor(position: Int) {
        val colorRes = introItems[position].backgroundColorRes
        val color = androidx.core.content.ContextCompat.getColor(this, colorRes)
        
        binding.rootLayout.animate()
            .backgroundColor(color)
            .setDuration(300)
            .start()
        
        // Update status bar color
        window.statusBarColor = color
        window.navigationBarColor = color
    }
    
    private fun completeIntro() {
        // Mark intro as completed
        preferenceManager.isIntroCompleted = true
        
        // Animate exit
        binding.rootLayout.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                // Navigate to main activity
                val intent = when {
                    preferenceManager.isAppLockEnabled -> {
                        AuthenticationActivity.getIntent(this)
                    }
                    else -> {
                        MainActivity.getIntent(this)
                    }
                }
                
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }.start()
    }
    
    override fun onBackPressed() {
        if (currentPosition > 0) {
            binding.viewPager.currentItem = currentPosition - 1
        } else {
            // Show exit confirmation on first page
            showExitConfirmation()
        }
    }
    
    private fun showExitConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.exit_intro_title)
            .setMessage(R.string.exit_intro_message)
            .setPositiveButton(R.string.exit) { _, _ ->
                finish()
            }
            .setNegativeButton(R.string.continue_text, null)
            .show()
    }
    
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, IntroActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        
        fun isIntroNeeded(context: Context): Boolean {
            val prefs = PreferenceManager.getInstance(context)
            return !prefs.isIntroCompleted
        }
    }
}
