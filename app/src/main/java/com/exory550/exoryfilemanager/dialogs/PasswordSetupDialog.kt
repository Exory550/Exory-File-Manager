package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.security.EncryptionManager
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class PasswordSetupDialog(
    context: Context,
    private val mode: Int = MODE_SETUP,
    private val onSuccess: () -> Unit,
    private val onCancel: (() -> Unit)? = null
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    companion object {
        const val MODE_SETUP = 0
        const val MODE_CHANGE = 1
        const val MODE_RESET = 2
        const val MODE_VERIFY = 3
        
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_PASSWORD_LENGTH = 32
    }

    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tilCurrentPassword: TextInputLayout
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var tilHint: TextInputLayout
    private lateinit var etHint: TextInputEditText
    private lateinit var cbShowPassword: CheckBox
    private lateinit var btnOk: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var strengthProgressBar: ProgressBar
    private lateinit var strengthText: TextView
    private lateinit var requirementsLayout: LinearLayout
    private lateinit var reqLength: TextView
    private lateinit var reqLowercase: TextView
    private lateinit var reqUppercase: TextView
    private lateinit var reqNumber: TextView
    private lateinit var reqSpecial: TextView
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var encryptionManager: EncryptionManager
    private val handler = Handler(Looper.getMainLooper())
    
    private var passwordStrength = 0
    private var requirements = PasswordRequirements()
    
    data class PasswordRequirements(
        var hasMinLength: Boolean = false,
        var hasLowercase: Boolean = false,
        var hasUppercase: Boolean = false,
        var hasNumber: Boolean = false,
        var hasSpecial: Boolean = false
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(mode != MODE_VERIFY)
        setCanceledOnTouchOutside(mode != MODE_VERIFY)
        
        preferenceManager = PreferenceManager.getInstance(context)
        encryptionManager = EncryptionManager.getInstance(context)
        
        setupViews()
        setupListeners()
        updateUIForMode()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_password_setup, null)
        setContentView(binding)
        
        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvMessage = binding.findViewById(R.id.tvMessage)
        tilCurrentPassword = binding.findViewById(R.id.tilCurrentPassword)
        etCurrentPassword = binding.findViewById(R.id.etCurrentPassword)
        tilNewPassword = binding.findViewById(R.id.tilNewPassword)
        etNewPassword = binding.findViewById(R.id.etNewPassword)
        tilConfirmPassword = binding.findViewById(R.id.tilConfirmPassword)
        etConfirmPassword = binding.findViewById(R.id.etConfirmPassword)
        tilHint = binding.findViewById(R.id.tilHint)
        etHint = binding.findViewById(R.id.etHint)
        cbShowPassword = binding.findViewById(R.id.cbShowPassword)
        btnOk = binding.findViewById(R.id.btnOk)
        btnCancel = binding.findViewById(R.id.btnCancel)
        progressBar = binding.findViewById(R.id.progressBar)
        strengthProgressBar = binding.findViewById(R.id.strengthProgressBar)
        strengthText = binding.findViewById(R.id.strengthText)
        requirementsLayout = binding.findViewById(R.id.requirementsLayout)
        reqLength = binding.findViewById(R.id.reqLength)
        reqLowercase = binding.findViewById(R.id.reqLowercase)
        reqUppercase = binding.findViewById(R.id.reqUppercase)
        reqNumber = binding.findViewById(R.id.reqNumber)
        reqSpecial = binding.findViewById(R.id.reqSpecial)
        
        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
        
        // Set icon color based on mode
        val iconColor = when (mode) {
            MODE_SETUP -> R.color.primary_color
            MODE_CHANGE -> R.color.warning_color
            MODE_RESET -> R.color.error_color
            MODE_VERIFY -> R.color.primary_color
            else -> R.color.primary_color
        }
        ivIcon.setColorFilter(
            ContextCompat.getColor(context, iconColor),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
    
    private fun setupListeners() {
        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val transformation = if (isChecked) null else PasswordTransformationMethod.getInstance()
            etCurrentPassword.transformationMethod = transformation
            etNewPassword.transformationMethod = transformation
            etConfirmPassword.transformationMethod = transformation
        }
        
        btnOk.setOnClickListener {
            handleOkClick()
        }
        
        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
        
        etNewPassword.doOnTextChanged { text, _, _, _ ->
            if (mode != MODE_VERIFY) {
                validatePasswordRequirements(text.toString())
                checkPasswordStrength(text.toString())
            }
        }
        
        etConfirmPassword.doOnTextChanged { text, _, _, _ ->
            validatePasswordMatch()
        }
    }
    
    private fun updateUIForMode() {
        when (mode) {
            MODE_SETUP -> {
                tvTitle.text = context.getString(R.string.setup_password)
                tvMessage.text = context.getString(R.string.setup_password_message)
                tilCurrentPassword.visibility = View.GONE
                tilHint.visibility = View.VISIBLE
                requirementsLayout.visibility = View.VISIBLE
            }
            MODE_CHANGE -> {
                tvTitle.text = context.getString(R.string.change_password)
                tvMessage.text = context.getString(R.string.change_password_message)
                tilCurrentPassword.visibility = View.VISIBLE
                tilCurrentPassword.hint = context.getString(R.string.current_password)
                tilHint.visibility = View.VISIBLE
                requirementsLayout.visibility = View.VISIBLE
            }
            MODE_RESET -> {
                tvTitle.text = context.getString(R.string.reset_password)
                tvMessage.text = context.getString(R.string.reset_password_message)
                tilCurrentPassword.visibility = View.GONE
                tilHint.visibility = View.GONE
                requirementsLayout.visibility = View.VISIBLE
            }
            MODE_VERIFY -> {
                tvTitle.text = context.getString(R.string.verify_password)
                tvMessage.text = context.getString(R.string.verify_password_message)
                tilCurrentPassword.visibility = View.VISIBLE
                tilCurrentPassword.hint = context.getString(R.string.password)
                tilNewPassword.visibility = View.GONE
                tilConfirmPassword.visibility = View.GONE
                tilHint.visibility = View.GONE
                requirementsLayout.visibility = View.GONE
                strengthProgressBar.visibility = View.GONE
                strengthText.visibility = View.GONE
            }
        }
    }
    
    private fun handleOkClick() {
        when (mode) {
            MODE_VERIFY -> verifyPassword()
            MODE_SETUP -> setupPassword()
            MODE_CHANGE -> changePassword()
            MODE_RESET -> resetPassword()
        }
    }
    
    private fun verifyPassword() {
        val password = etCurrentPassword.text.toString()
        
        if (password.isEmpty()) {
            showError(tilCurrentPassword, context.getString(R.string.error_password_required))
            return
        }
        
        progressBar.visibility = View.VISIBLE
        btnOk.isEnabled = false
        
        handler.postDelayed({
            try {
                val storedHash = preferenceManager.passwordHash
                if (encryptionManager.verifyPassword(password, storedHash)) {
                    onSuccess.invoke()
                    dismiss()
                } else {
                    showError(tilCurrentPassword, context.getString(R.string.incorrect_password))
                    etCurrentPassword.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                }
            } catch (e: Exception) {
                showError(tilCurrentPassword, context.getString(R.string.authentication_error))
            } finally {
                progressBar.visibility = View.GONE
                btnOk.isEnabled = true
            }
        }, 500)
    }
    
    private fun setupPassword() {
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val hint = etHint.text.toString()
        
        // Validate passwords
        if (!validateNewPassword(newPassword, confirmPassword)) {
            return
        }
        
        progressBar.visibility = View.VISIBLE
        btnOk.isEnabled = false
        
        handler.postDelayed({
            try {
                val hash = encryptionManager.hashPassword(newPassword)
                preferenceManager.passwordHash = hash
                preferenceManager.passwordHint = hint
                
                onSuccess.invoke()
                dismiss()
            } catch (e: Exception) {
                showError(tilNewPassword, context.getString(R.string.password_setup_error))
            } finally {
                progressBar.visibility = View.GONE
                btnOk.isEnabled = true
            }
        }, 500)
    }
    
    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val hint = etHint.text.toString()
        
        // Verify current password first
        val storedHash = preferenceManager.passwordHash
        if (!encryptionManager.verifyPassword(currentPassword, storedHash)) {
            showError(tilCurrentPassword, context.getString(R.string.incorrect_password))
            etCurrentPassword.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
            return
        }
        
        // Validate new password
        if (!validateNewPassword(newPassword, confirmPassword)) {
            return
        }
        
        progressBar.visibility = View.VISIBLE
        btnOk.isEnabled = false
        
        handler.postDelayed({
            try {
                val hash = encryptionManager.hashPassword(newPassword)
                preferenceManager.passwordHash = hash
                preferenceManager.passwordHint = hint
                
                onSuccess.invoke()
                dismiss()
            } catch (e: Exception) {
                showError(tilNewPassword, context.getString(R.string.password_change_error))
            } finally {
                progressBar.visibility = View.GONE
                btnOk.isEnabled = true
            }
        }, 500)
    }
    
    private fun resetPassword() {
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        
        if (!validateNewPassword(newPassword, confirmPassword)) {
            return
        }
        
        // Show security verification (email, security questions, etc.)
        showSecurityVerification()
    }
    
    private fun validateNewPassword(newPassword: String, confirmPassword: String): Boolean {
        // Check if empty
        if (newPassword.isEmpty()) {
            showError(tilNewPassword, context.getString(R.string.error_password_required))
            return false
        }
        
        // Check length
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            showError(tilNewPassword, 
                context.getString(R.string.error_password_min_length, MIN_PASSWORD_LENGTH))
            return false
        }
        
        if (newPassword.length > MAX_PASSWORD_LENGTH) {
            showError(tilNewPassword, 
                context.getString(R.string.error_password_max_length, MAX_PASSWORD_LENGTH))
            return false
        }
        
        // Check password strength
        if (passwordStrength < 60) {
            showError(tilNewPassword, context.getString(R.string.error_password_weak))
            return false
        }
        
        // Check if passwords match
        if (newPassword != confirmPassword) {
            showError(tilConfirmPassword, context.getString(R.string.error_passwords_not_match))
            return false
        }
        
        return true
    }
    
    private fun validatePasswordRequirements(password: String) {
        requirements = PasswordRequirements(
            hasMinLength = password.length >= MIN_PASSWORD_LENGTH,
            hasLowercase = password.any { it.isLowerCase() },
            hasUppercase = password.any { it.isUpperCase() },
            hasNumber = password.any { it.isDigit() },
            hasSpecial = password.any { !it.isLetterOrDigit() }
        )
        
        updateRequirementView(reqLength, requirements.hasMinLength, 
            context.getString(R.string.requirement_min_length, MIN_PASSWORD_LENGTH))
        updateRequirementView(reqLowercase, requirements.hasLowercase, 
            context.getString(R.string.requirement_lowercase))
        updateRequirementView(reqUppercase, requirements.hasUppercase, 
            context.getString(R.string.requirement_uppercase))
        updateRequirementView(reqNumber, requirements.hasNumber, 
            context.getString(R.string.requirement_number))
        updateRequirementView(reqSpecial, requirements.hasSpecial, 
            context.getString(R.string.requirement_special))
    }
    
    private fun updateRequirementView(textView: TextView, isMet: Boolean, text: String) {
        textView.text = text
        textView.setCompoundDrawablesWithIntrinsicBounds(
            if (isMet) R.drawable.ic_check_circle else R.drawable.ic_circle,
            0, 0, 0
        )
        textView.setTextColor(
            ContextCompat.getColor(context,
                if (isMet) R.color.success_color else R.color.text_secondary
            )
        )
    }
    
    private fun validatePasswordMatch() {
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        
        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
            tilConfirmPassword.error = context.getString(R.string.error_passwords_not_match)
        } else {
            tilConfirmPassword.error = null
        }
    }
    
    private fun checkPasswordStrength(password: String) {
        if (password.isEmpty()) {
            strengthProgressBar.progress = 0
            strengthText.text = ""
            return
        }
        
        var strength = 0
        
        // Length contribution
        when {
            password.length >= 12 -> strength += 30
            password.length >= 8 -> strength += 20
            password.length >= MIN_PASSWORD_LENGTH -> strength += 10
        }
        
        // Character variety
        if (password.any { it.isLowerCase() }) strength += 15
        if (password.any { it.isUpperCase() }) strength += 15
        if (password.any { it.isDigit() }) strength += 20
        if (password.any { !it.isLetterOrDigit() }) strength += 20
        
        passwordStrength = strength
        strengthProgressBar.progress = strength
        
        strengthText.text = when {
            strength < 30 -> context.getString(R.string.password_very_weak)
            strength < 50 -> context.getString(R.string.password_weak)
            strength < 70 -> context.getString(R.string.password_medium)
            strength < 85 -> context.getString(R.string.password_strong)
            else -> context.getString(R.string.password_very_strong)
        }
        
        val colorRes = when {
            strength < 30 -> R.color.password_very_weak
            strength < 50 -> R.color.password_weak
            strength < 70 -> R.color.password_medium
            strength < 85 -> R.color.password_strong
            else -> R.color.password_very_strong
        }
        
        strengthText.setTextColor(ContextCompat.getColor(context, colorRes))
        strengthProgressBar.progressTintList = ContextCompat.getColorStateList(context, colorRes)
    }
    
    private fun showSecurityVerification() {
        // Implement security verification (email, security questions, etc.)
        Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
    }
    
    private fun showError(inputLayout: TextInputLayout, message: String) {
        inputLayout.error = message
        inputLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
    }
    
    override fun show() {
        super.show()
        when (mode) {
            MODE_VERIFY -> etCurrentPassword.requestFocus()
            else -> etNewPassword.requestFocus()
        }
    }
    
    companion object {
        fun setup(
            context: Context,
            onSuccess: () -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            PasswordSetupDialog(context, MODE_SETUP, onSuccess, onCancel).show()
        }
        
        fun change(
            context: Context,
            onSuccess: () -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            PasswordSetupDialog(context, MODE_CHANGE, onSuccess, onCancel).show()
        }
        
        fun verify(
            context: Context,
            onSuccess: () -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            PasswordSetupDialog(context, MODE_VERIFY, onSuccess, onCancel).show()
        }
        
        fun reset(
            context: Context,
            onSuccess: () -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            PasswordSetupDialog(context, MODE_RESET, onSuccess, onCancel).show()
        }
    }
}
