package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.security.EncryptionManager
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PasswordAuthenticationDialog(
    context: Context,
    private val mode: Int = MODE_AUTHENTICATE,
    private val onSuccess: () -> Unit,
    private val onFailure: (() -> Unit)? = null,
    private val onForgotPassword: (() -> Unit)? = null
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    companion object {
        const val MODE_AUTHENTICATE = 0
        const val MODE_SET_PASSWORD = 1
        const val MODE_CHANGE_PASSWORD = 2
        const val MODE_VERIFY_PASSWORD = 3
    }

    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var tilHint: TextInputLayout
    private lateinit var etHint: TextInputEditText
    private lateinit var cbShowPassword: CheckBox
    private lateinit var btnBiometric: ImageButton
    private lateinit var btnOk: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var tvForgotPassword: TextView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var strengthIndicator: ProgressBar
    private lateinit var strengthText: TextView
    private lateinit var passwordRequirements: LinearLayout
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var encryptionManager: EncryptionManager
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    
    private var currentPassword = ""
    private var isBiometricAvailable = false
    private var failedAttempts = 0
    private val maxAttempts = 5
    private val handler = Handler(Looper.getMainLooper())
    private var lockoutEndTime: Long = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(mode != MODE_AUTHENTICATE)
        setCanceledOnTouchOutside(mode != MODE_AUTHENTICATE)
        
        setupDependencies()
        setupViews()
        setupBiometric()
        setupListeners()
        updateUIForMode()
    }
    
    private fun setupDependencies() {
        preferenceManager = PreferenceManager.getInstance(context)
        encryptionManager = EncryptionManager.getInstance(context)
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_password_authentication, null)
        setContentView(binding)
        
        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvMessage = binding.findViewById(R.id.tvMessage)
        tilPassword = binding.findViewById(R.id.tilPassword)
        etPassword = binding.findViewById(R.id.etPassword)
        tilConfirmPassword = binding.findViewById(R.id.tilConfirmPassword)
        etConfirmPassword = binding.findViewById(R.id.etConfirmPassword)
        tilHint = binding.findViewById(R.id.tilHint)
        etHint = binding.findViewById(R.id.etHint)
        cbShowPassword = binding.findViewById(R.id.cbShowPassword)
        btnBiometric = binding.findViewById(R.id.btnBiometric)
        btnOk = binding.findViewById(R.id.btnOk)
        btnCancel = binding.findViewById(R.id.btnCancel)
        tvForgotPassword = binding.findViewById(R.id.tvForgotPassword)
        progressBar = binding.findViewById(R.id.progressBar)
        strengthIndicator = binding.findViewById(R.id.strengthIndicator)
        strengthText = binding.findViewById(R.id.strengthText)
        passwordRequirements = binding.findViewById(R.id.passwordRequirements)
        
        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
        
        // Set icon color
        ivIcon.setColorFilter(
            ContextCompat.getColor(context, when (mode) {
                MODE_AUTHENTICATE -> R.color.primary_color
                MODE_SET_PASSWORD -> R.color.success_color
                MODE_CHANGE_PASSWORD -> R.color.warning_color
                else -> R.color.primary_color
            }),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
    
    private fun setupBiometric() {
        val biometricManager = BiometricManager.from(context)
        isBiometricAvailable = when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
        
        if (isBiometricAvailable && mode == MODE_AUTHENTICATE && preferenceManager.isBiometricEnabled) {
            btnBiometric.visibility = View.VISIBLE
            
            val executor = ContextCompat.getMainExecutor(context)
            
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess.invoke()
                        dismiss()
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                        }
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(context, R.string.biometric_auth_failed, Toast.LENGTH_SHORT).show()
                    }
                })
            
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.biometric_auth_title))
                .setSubtitle(context.getString(R.string.biometric_auth_subtitle))
                .setNegativeButtonText(context.getString(R.string.use_password))
                .build()
            
            btnBiometric.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            btnBiometric.visibility = View.GONE
        }
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
            if (mode == MODE_AUTHENTICATE) {
                onFailure?.invoke()
            }
            dismiss()
        }
        
        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
        
        etPassword.doOnTextChanged { text, _, _, _ ->
            if (mode != MODE_AUTHENTICATE) {
                checkPasswordStrength(text.toString())
            }
        }
    }
    
    private fun updateUIForMode() {
        when (mode) {
            MODE_AUTHENTICATE -> {
                tvTitle.text = context.getString(R.string.enter_password)
                tvMessage.text = context.getString(R.string.enter_password_message)
                tilConfirmPassword.visibility = View.GONE
                tilHint.visibility = View.GONE
                tvForgotPassword.visibility = View.VISIBLE
                passwordRequirements.visibility = View.GONE
                btnCancel.visibility = View.VISIBLE
            }
            MODE_SET_PASSWORD -> {
                tvTitle.text = context.getString(R.string.set_password)
                tvMessage.text = context.getString(R.string.set_password_message)
                tilConfirmPassword.visibility = View.VISIBLE
                tilHint.visibility = View.VISIBLE
                tvForgotPassword.visibility = View.GONE
                passwordRequirements.visibility = View.VISIBLE
                btnCancel.visibility = View.VISIBLE
            }
            MODE_CHANGE_PASSWORD -> {
                tvTitle.text = context.getString(R.string.change_password)
                tvMessage.text = context.getString(R.string.change_password_message)
                tilPassword.hint = context.getString(R.string.current_password)
                tilConfirmPassword.visibility = View.VISIBLE
                tilHint.visibility = View.VISIBLE
                tvForgotPassword.visibility = View.GONE
                passwordRequirements.visibility = View.VISIBLE
                btnCancel.visibility = View.VISIBLE
            }
            MODE_VERIFY_PASSWORD -> {
                tvTitle.text = context.getString(R.string.verify_password)
                tvMessage.text = context.getString(R.string.verify_password_message)
                tilConfirmPassword.visibility = View.GONE
                tilHint.visibility = View.GONE
                tvForgotPassword.visibility = View.VISIBLE
                passwordRequirements.visibility = View.GONE
                btnCancel.visibility = View.VISIBLE
            }
        }
    }
    
    private fun handleOkClick() {
        if (isLockedOut()) {
            showLockoutMessage()
            return
        }
        
        val password = etPassword.text.toString()
        
        when (mode) {
            MODE_AUTHENTICATE, MODE_VERIFY_PASSWORD -> {
                authenticate(password)
            }
            MODE_SET_PASSWORD -> {
                setPassword(password)
            }
            MODE_CHANGE_PASSWORD -> {
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
                val storedHash = preferenceManager.passwordHash
                if (encryptionManager.verifyPassword(password, storedHash)) {
                    failedAttempts = 0
                    onSuccess.invoke()
                    dismiss()
                } else {
                    failedAttempts++
                    showError(context.getString(R.string.incorrect_password))
                    
                    if (failedAttempts >= maxAttempts) {
                        handleLockout()
                    }
                }
            } catch (e: Exception) {
                showError(context.getString(R.string.authentication_error))
            } finally {
                progressBar.visibility = View.GONE
                btnOk.isEnabled = true
            }
        }, 500) // Add delay to prevent brute force
    }
    
    private fun setPassword(password: String) {
        val confirmPassword = etConfirmPassword.text.toString()
        val hint = etHint.text.toString()
        
        // Validate password
        val validation = validatePassword(password, confirmPassword)
        if (!validation.isValid) {
            showError(validation.message)
            return
        }
        
        progressBar.visibility = View.VISIBLE
        btnOk.isEnabled = false
        
        handler.postDelayed({
            try {
                val hash = encryptionManager.hashPassword(password)
                preferenceManager.passwordHash = hash
                preferenceManager.passwordHint = hint
                
                onSuccess.invoke()
                dismiss()
            } catch (e: Exception) {
                showError(context.getString(R.string.password_set_error))
            } finally {
                progressBar.visibility = View.GONE
                btnOk.isEnabled = true
            }
        }, 300)
    }
    
    private fun changePassword(currentPassword: String) {
        // First verify current password
        val storedHash = preferenceManager.passwordHash
        if (!encryptionManager.verifyPassword(currentPassword, storedHash)) {
            showError(context.getString(R.string.incorrect_password))
            return
        }
        
        // Then set new password
        val newPassword = etConfirmPassword.text.toString()
        val hint = etHint.text.toString()
        
        val validation = validatePassword(newPassword, newPassword)
        if (!validation.isValid) {
            showError(validation.message)
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
                showError(context.getString(R.string.password_change_error))
            } finally {
                progressBar.visibility = View.GONE
                btnOk.isEnabled = true
            }
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
        // Password must contain at least one number and one letter
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
        if (password.length >= 8) strength += 25
        else if (password.length >= 6) strength += 15
        
        // Contains numbers
        if (password.any { it.isDigit() }) strength += 25
        
        // Contains letters
        if (password.any { it.isLetter() }) strength += 25
        
        // Contains special characters
        if (password.any { !it.isLetterOrDigit() }) strength += 25
        
        strengthIndicator.progress = strength
        
        strengthText.text = when {
            strength < 25 -> context.getString(R.string.password_very_weak)
            strength < 50 -> context.getString(R.string.password_weak)
            strength < 75 -> context.getString(R.string.password_medium)
            strength < 90 -> context.getString(R.string.password_strong)
            else -> context.getString(R.string.password_very_strong)
        }
        
        strengthText.setTextColor(
            ContextCompat.getColor(context, when {
                strength < 25 -> R.color.password_very_weak
                strength < 50 -> R.color.password_weak
                strength < 75 -> R.color.password_medium
                strength < 90 -> R.color.password_strong
                else -> R.color.password_very_strong
            })
        )
    }
    
    private fun handleLockout() {
        lockoutEndTime = System.currentTimeMillis() + 30000 // 30 seconds
        showLockoutMessage()
        
        handler.postDelayed({
            lockoutEndTime = 0
            failedAttempts = 0
        }, 30000)
    }
    
    private fun isLockedOut(): Boolean {
        return lockoutEndTime > System.currentTimeMillis()
    }
    
    private fun showLockoutMessage() {
        val remainingSeconds = (lockoutEndTime - System.currentTimeMillis()) / 1000
        showError(context.getString(R.string.lockout_message, remainingSeconds))
        btnOk.isEnabled = false
        
        handler.postDelayed({
            btnOk.isEnabled = true
        }, lockoutEndTime - System.currentTimeMillis())
    }
    
    private fun showForgotPasswordDialog() {
        if (onForgotPassword != null) {
            dismiss()
            onForgotPassword.invoke()
        } else {
            val hint = preferenceManager.passwordHint
            if (hint.isNotEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle(R.string.password_hint)
                    .setMessage(hint)
                    .setPositiveButton(R.string.ok, null)
                    .show()
            } else {
                AlertDialog.Builder(context)
                    .setTitle(R.string.forgot_password)
                    .setMessage(R.string.forgot_password_message)
                    .setPositiveButton(R.string.reset) { _, _ ->
                        // Implement password reset
                        showPasswordResetDialog()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }
    
    private fun showPasswordResetDialog() {
        // Implement password reset via email or security questions
        Toast.makeText(context, R.string.coming_soon, Toast.LENGTH_SHORT).show()
    }
    
    private fun showError(message: String) {
        tilPassword.error = message
        etPassword.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
    }
    
    data class ValidationResult(val isValid: Boolean, val message: String)
    
    override fun show() {
        super.show()
        etPassword.requestFocus()
    }
    
    companion object {
        fun authenticate(
            context: Context,
            onSuccess: () -> Unit,
            onFailure: (() -> Unit)? = null,
            onForgotPassword: (() -> Unit)? = null
        ) {
            PasswordAuthenticationDialog(
                context,
                MODE_AUTHENTICATE,
                onSuccess,
                onFailure,
                onForgotPassword
            ).show()
        }
        
        fun setPassword(
            context: Context,
            onSuccess: () -> Unit
        ) {
            PasswordAuthenticationDialog(
                context,
                MODE_SET_PASSWORD,
                onSuccess
            ).show()
        }
        
        fun changePassword(
            context: Context,
            onSuccess: () -> Unit
        ) {
            PasswordAuthenticationDialog(
                context,
                MODE_CHANGE_PASSWORD,
                onSuccess
            ).show()
        }
        
        fun verify(
            context: Context,
            onSuccess: () -> Unit,
            onFailure: (() -> Unit)? = null
        ) {
            PasswordAuthenticationDialog(
                context,
                MODE_VERIFY_PASSWORD,
                onSuccess,
                onFailure
            ).show()
        }
    }
}
