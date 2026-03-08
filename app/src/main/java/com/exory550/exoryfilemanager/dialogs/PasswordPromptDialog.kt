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
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class PasswordPromptDialog(
    context: Context,
    private val config: Config
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    data class Config(
        val title: String = context.getString(R.string.enter_password),
        val message: String = context.getString(R.string.enter_password_message),
        val hint: String = context.getString(R.string.password),
        val confirmHint: String = context.getString(R.string.confirm_password),
        val mode: Int = MODE_AUTHENTICATE,
        val showForgotPassword: Boolean = true,
        val showBiometric: Boolean = false,
        val allowCancel: Boolean = true,
        val maxAttempts: Int = 5,
        val lockoutDuration: Long = 30000, // 30 seconds
        val onPasswordEntered: (String) -> Unit,
        val onBiometricClick: (() -> Unit)? = null,
        val onForgotPassword: (() -> Unit)? = null,
        val onCancel: (() -> Unit)? = null
    ) {
        companion object {
            const val MODE_AUTHENTICATE = 0
            const val MODE_CONFIRM = 1
            const val MODE_SET_PASSWORD = 2
            const val MODE_CHANGE_PASSWORD = 3
        }
    }

    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var cbShowPassword: CheckBox
    private lateinit var btnBiometric: ImageButton
    private lateinit var btnOk: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var tvForgotPassword: TextView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var tvAttemptsLeft: TextView
    private lateinit var strengthIndicator: ProgressBar
    private lateinit var strengthText: TextView
    
    private lateinit var encryptionManager: EncryptionManager
    
    private var currentAttempts = 0
    private var lockoutEndTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(config.allowCancel)
        setCanceledOnTouchOutside(config.allowCancel)
        
        encryptionManager = EncryptionManager.getInstance(context)
        
        setupViews()
        setupListeners()
        updateUIForMode()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_password_prompt, null)
        setContentView(binding)
        
        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvMessage = binding.findViewById(R.id.tvMessage)
        tilPassword = binding.findViewById(R.id.tilPassword)
        etPassword = binding.findViewById(R.id.etPassword)
        tilConfirmPassword = binding.findViewById(R.id.tilConfirmPassword)
        etConfirmPassword = binding.findViewById(R.id.etConfirmPassword)
        cbShowPassword = binding.findViewById(R.id.cbShowPassword)
        btnBiometric = binding.findViewById(R.id.btnBiometric)
        btnOk = binding.findViewById(R.id.btnOk)
        btnCancel = binding.findViewById(R.id.btnCancel)
        tvForgotPassword = binding.findViewById(R.id.tvForgotPassword)
        progressBar = binding.findViewById(R.id.progressBar)
        tvAttemptsLeft = binding.findViewById(R.id.tvAttemptsLeft)
        strengthIndicator = binding.findViewById(R.id.strengthIndicator)
        strengthText = binding.findViewById(R.id.strengthText)
        
        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
        
        // Configure views based on config
        tilPassword.hint = config.hint
        tilConfirmPassword.hint = config.confirmHint
        tvTitle.text = config.title
        tvMessage.text = config.message
        
        // Biometric button
        if (config.showBiometric && config.onBiometricClick != null) {
            btnBiometric.visibility = View.VISIBLE
        } else {
            btnBiometric.visibility = View.GONE
        }
        
        // Forgot password link
        tvForgotPassword.visibility = if (config.showForgotPassword) View.VISIBLE else View.GONE
        
        // Cancel button
        btnCancel.visibility = if (config.allowCancel) View.VISIBLE else View.GONE
    }
    
    private fun setupListeners() {
        cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            val transformation = if (isChecked) null else PasswordTransformationMethod.getInstance()
            etPassword.transformationMethod = transformation
            etConfirmPassword.transformationMethod = transformation
            etPassword.setSelection(etPassword.text?.length ?: 0)
            etConfirmPassword.setSelection(etConfirmPassword.text?.length ?: 0)
        }
        
        btnOk.setOnClickListener {
            handleOkClick()
        }
        
        btnCancel.setOnClickListener {
            config.onCancel?.invoke()
            dismiss()
        }
        
        btnBiometric.setOnClickListener {
            config.onBiometricClick?.invoke()
            dismiss()
        }
        
        tvForgotPassword.setOnClickListener {
            config.onForgotPassword?.invoke()
            dismiss()
        }
        
        etPassword.doOnTextChanged { text, _, _, _ ->
            if (config.mode == Config.MODE_SET_PASSWORD || config.mode == Config.MODE_CHANGE_PASSWORD) {
                checkPasswordStrength(text.toString())
            }
        }
    }
    
    private fun updateUIForMode() {
        when (config.mode) {
            Config.MODE_AUTHENTICATE, Config.MODE_CONFIRM -> {
                tilConfirmPassword.visibility = View.GONE
                strengthIndicator.visibility = View.GONE
                strengthText.visibility = View.GONE
            }
            Config.MODE_SET_PASSWORD, Config.MODE_CHANGE_PASSWORD -> {
                tilConfirmPassword.visibility = View.VISIBLE
                strengthIndicator.visibility = View.VISIBLE
                strengthText.visibility = View.VISIBLE
            }
        }
    }
    
    private fun handleOkClick() {
        if (isLockedOut()) {
            showLockoutMessage()
            return
        }
        
        val password = etPassword.text.toString()
        
        when (config.mode) {
            Config.MODE_AUTHENTICATE, Config.MODE_CONFIRM -> {
                authenticate(password)
            }
            Config.MODE_SET_PASSWORD -> {
                setPassword(password)
            }
            Config.MODE_CHANGE_PASSWORD -> {
                changePassword(password)
            }
        }
    }
    
    private fun authenticate(password: String) {
        if (password.isEmpty()) {
            showError(context.getString(R.string.error_password_required))
            return
        }
        
        progressBar.visibility = View.VISIBLE
        btnOk.isEnabled = false
        
        handler.postDelayed({
            try {
                config.onPasswordEntered(password)
                dismiss()
            } catch (e: Exception) {
                handleAuthenticationFailure()
            } finally {
                progressBar.visibility = View.GONE
                btnOk.isEnabled = true
            }
        }, 500) // Delay to prevent brute force
    }
    
    private fun setPassword(password: String) {
        val confirmPassword = etConfirmPassword.text.toString()
        
        val validation = validatePassword(password, confirmPassword)
        if (!validation.isValid) {
            showError(validation.message)
            return
        }
        
        progressBar.visibility = View.VISIBLE
        btnOk.isEnabled = false
        
        handler.postDelayed({
            config.onPasswordEntered(password)
            dismiss()
        }, 300)
    }
    
    private fun changePassword(currentPassword: String) {
        // For change password, current password is in etPassword, new in etConfirmPassword
        val newPassword = etConfirmPassword.text.toString()
        
        if (currentPassword.isEmpty()) {
            showError(context.getString(R.string.error_current_password_required))
            return
        }
        
        val validation = validatePassword(newPassword, newPassword)
        if (!validation.isValid) {
            showError(validation.message)
            return
        }
        
        progressBar.visibility = View.VISIBLE
        btnOk.isEnabled = false
        
        handler.postDelayed({
            // Pass both old and new password
            config.onPasswordEntered("$currentPassword:$newPassword")
            dismiss()
        }, 300)
    }
    
    private fun validatePassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult(false, context.getString(R.string.error_password_required))
            password.length < 4 -> ValidationResult(false, context.getString(R.string.error_password_too_short))
            password.length > 32 -> ValidationResult(false, context.getString(R.string.error_password_too_long))
            password != confirmPassword -> ValidationResult(false, context.getString(R.string.error_passwords_not_match))
            !hasRequiredStrength(password) -> ValidationResult(false, context.getString(R.string.error_password_weak))
            else -> ValidationResult(true, "")
        }
    }
    
    private fun hasRequiredStrength(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
    
    private fun checkPasswordStrength(password: String) {
        if (password.isEmpty()) {
            strengthIndicator.progress = 0
            strengthText.text = ""
            return
        }
        
        var strength = 0
        
        // Length check
        when {
            password.length >= 12 -> strength += 40
            password.length >= 8 -> strength += 30
            password.length >= 6 -> strength += 20
        }
        
        // Contains numbers
        if (password.any { it.isDigit() }) strength += 20
        
        // Contains lowercase
        if (password.any { it.isLowerCase() }) strength += 15
        
        // Contains uppercase
        if (password.any { it.isUpperCase() }) strength += 15
        
        // Contains special characters
        if (password.any { !it.isLetterOrDigit() }) strength += 10
        
        strengthIndicator.progress = strength
        
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
    }
    
    private fun handleAuthenticationFailure() {
        currentAttempts++
        val attemptsLeft = config.maxAttempts - currentAttempts
        
        if (attemptsLeft > 0) {
            tvAttemptsLeft.text = context.getString(R.string.attempts_left, attemptsLeft)
            tvAttemptsLeft.visibility = View.VISIBLE
            showError(context.getString(R.string.incorrect_password))
            
            if (currentAttempts >= config.maxAttempts) {
                handleLockout()
            }
        }
    }
    
    private fun handleLockout() {
        lockoutEndTime = System.currentTimeMillis() + config.lockoutDuration
        showLockoutMessage()
        
        handler.postDelayed({
            lockoutEndTime = 0
            currentAttempts = 0
            tvAttemptsLeft.visibility = View.GONE
            btnOk.isEnabled = true
        }, config.lockoutDuration)
    }
    
    private fun isLockedOut(): Boolean {
        return lockoutEndTime > System.currentTimeMillis()
    }
    
    private fun showLockoutMessage() {
        val remainingSeconds = (lockoutEndTime - System.currentTimeMillis()) / 1000
        tilPassword.error = context.getString(R.string.lockout_message, remainingSeconds)
        btnOk.isEnabled = false
    }
    
    private fun showError(message: String) {
        tilPassword.error = message
        etPassword.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
    }
    
    data class ValidationResult(val isValid: Boolean, val message: String)
    
    class Builder(private val context: Context) {
        private var title: String = context.getString(R.string.enter_password)
        private var message: String = context.getString(R.string.enter_password_message)
        private var hint: String = context.getString(R.string.password)
        private var confirmHint: String = context.getString(R.string.confirm_password)
        private var mode: Int = Config.MODE_AUTHENTICATE
        private var showForgotPassword: Boolean = true
        private var showBiometric: Boolean = false
        private var allowCancel: Boolean = true
        private var maxAttempts: Int = 5
        private var lockoutDuration: Long = 30000
        private var onPasswordEntered: (String) -> Unit = {}
        private var onBiometricClick: (() -> Unit)? = null
        private var onForgotPassword: (() -> Unit)? = null
        private var onCancel: (() -> Unit)? = null
        
        fun setTitle(title: String) = apply { this.title = title }
        fun setMessage(message: String) = apply { this.message = message }
        fun setHint(hint: String) = apply { this.hint = hint }
        fun setConfirmHint(confirmHint: String) = apply { this.confirmHint = confirmHint }
        fun setMode(mode: Int) = apply { this.mode = mode }
        fun setShowForgotPassword(show: Boolean) = apply { this.showForgotPassword = show }
        fun setShowBiometric(show: Boolean) = apply { this.showBiometric = show }
        fun setAllowCancel(allow: Boolean) = apply { this.allowCancel = allow }
        fun setMaxAttempts(max: Int) = apply { this.maxAttempts = max }
        fun setLockoutDuration(duration: Long) = apply { this.lockoutDuration = duration }
        fun setOnPasswordEntered(listener: (String) -> Unit) = apply { this.onPasswordEntered = listener }
        fun setOnBiometricClick(listener: () -> Unit) = apply { this.onBiometricClick = listener }
        fun setOnForgotPassword(listener: () -> Unit) = apply { this.onForgotPassword = listener }
        fun setOnCancel(listener: () -> Unit) = apply { this.onCancel = listener }
        
        fun build(): Config {
            return Config(
                title = title,
                message = message,
                hint = hint,
                confirmHint = confirmHint,
                mode = mode,
                showForgotPassword = showForgotPassword,
                showBiometric = showBiometric,
                allowCancel = allowCancel,
                maxAttempts = maxAttempts,
                lockoutDuration = lockoutDuration,
                onPasswordEntered = onPasswordEntered,
                onBiometricClick = onBiometricClick,
                onForgotPassword = onForgotPassword,
                onCancel = onCancel
            )
        }
        
        fun show() {
            PasswordPromptDialog(context, build()).show()
        }
    }
    
    companion object {
        fun show(context: Context, config: Config.() -> Unit) {
            val builder = Builder(context)
            config.invoke(builder)
            builder.show()
        }
        
        fun authenticate(
            context: Context,
            onPasswordEntered: (String) -> Unit,
            onBiometricClick: (() -> Unit)? = null,
            onForgotPassword: (() -> Unit)? = null,
            onCancel: (() -> Unit)? = null
        ) {
            Builder(context)
                .setMode(Config.MODE_AUTHENTICATE)
                .setOnPasswordEntered(onPasswordEntered)
                .setOnBiometricClick(onBiometricClick)
                .setOnForgotPassword(onForgotPassword)
                .setOnCancel(onCancel)
                .show()
        }
        
        fun setPassword(
            context: Context,
            onPasswordSet: (String) -> Unit
        ) {
            Builder(context)
                .setMode(Config.MODE_SET_PASSWORD)
                .setTitle(context.getString(R.string.set_password))
                .setMessage(context.getString(R.string.set_password_message))
                .setShowForgotPassword(false)
                .setOnPasswordEntered(onPasswordSet)
                .show()
        }
    }
}
