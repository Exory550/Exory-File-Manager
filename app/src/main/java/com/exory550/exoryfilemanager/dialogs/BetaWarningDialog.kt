package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.utils.PreferenceManager

class BetaWarningDialog(
    context: Context,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvBuildInfo: TextView
    private lateinit var cbDontShowAgain: CheckBox
    private lateinit var btnContinue: Button
    private lateinit var btnCancel: Button
    
    private val preferenceManager = PreferenceManager.getInstance(context)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        
        setupViews()
        setupClickListeners()
        loadBetaInfo()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_beta_warning, null)
        setContentView(binding)
        
        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvMessage = binding.findViewById(R.id.tvMessage)
        tvBuildInfo = binding.findViewById(R.id.tvBuildInfo)
        cbDontShowAgain = binding.findViewById(R.id.cbDontShowAgain)
        btnContinue = binding.findViewById(R.id.btnContinue)
        btnCancel = binding.findViewById(R.id.btnCancel)
        
        // Set dialog width to match parent with margins
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set background with rounded corners
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
    }
    
    private fun setupClickListeners() {
        btnContinue.setOnClickListener {
            if (cbDontShowAgain.isChecked) {
                preferenceManager.setBetaWarningDismissed(true)
            }
            onConfirm.invoke()
            dismiss()
        }
        
        btnCancel.setOnClickListener {
            onCancel.invoke()
            dismiss()
        }
    }
    
    private fun loadBetaInfo() {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val buildType = if (BuildConfig.DEBUG) "Debug" else "Release"
        val buildTime = BuildConfig.BUILD_TIME
        
        tvBuildInfo.text = context.getString(
            R.string.beta_build_info,
            versionName,
            versionCode,
            buildType,
            buildTime
        )
        
        // Set warning icon color
        ivIcon.setColorFilter(
            ContextCompat.getColor(context, R.color.warning_color),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
    
    /**
     * Check if dialog should be shown
     */
    fun shouldShow(): Boolean {
        return !preferenceManager.isBetaWarningDismissed() && 
               (BuildConfig.DEBUG || BuildConfig.VERSION_NAME.contains("beta", ignoreCase = true) ||
                BuildConfig.VERSION_NAME.contains("alpha", ignoreCase = true))
    }
    
    companion object {
        fun showIfNeeded(
            context: Context,
            onConfirm: () -> Unit,
            onCancel: () -> Unit
        ) {
            val dialog = BetaWarningDialog(context, onConfirm, onCancel)
            if (dialog.shouldShow()) {
                dialog.show()
            } else {
                onConfirm.invoke()
            }
        }
    }
}
