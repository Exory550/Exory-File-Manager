package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.databinding.ActivityAuthenticationBinding
import com.exory550.exoryfilemanager.dialogs.PasswordAuthenticationDialog
import com.exory550.exoryfilemanager.dialogs.PinAuthenticationDialog
import com.exory550.exoryfilemanager.dialogs.PatternAuthenticationDialog
import com.exory550.exoryfilemanager.extensions.*
import com.exory550.exoryfilemanager.security.BiometricManager as ExoryBiometricManager
import com.exory550.exoryfilemanager.security.EncryptionManager
import com.exory550.exoryfilemanager.utils.Constants
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.exory550.exoryfilemanager.utils.SecurityUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.security.auth.Destroyable

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var cancellationSignal: CancellationSignal
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    @Inject
    lateinit var encryptionManager: EncryptionManager
    
    @Inject
    lateinit var exoryBiometricManager: ExoryBiometricManager
    
    private var authenticationCallback: (Boolean) -> Unit = { success ->
        if (success) {
            handleAuthenticationSuccess()
        } else {
            handleAuthenticationFailure()
        }
    }
    
    private var isAuthenticating = false
    private var authenticationAttempts = 0
    private var lockoutEndTime: Long = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyMaterialTransitions()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeBiometricPrompt()
        setupClickListeners()
        checkSecuritySettings()
        addFlagsSecure()
    }
    
    override fun onResume() {
        super.onResume()
        if (preferenceManager.wasAppProtectionHandled) {
            finish()
        } else if (!isAuthenticating && !isLockedOut()) {
            startAuthentication()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (::cancellationSignal.isInitialized && !cancellationSignal.isCanceled) {
            cancellationSignal.cancel()
        }
    }
    
    private fun applyMaterialTransitions() {
        window.apply {
            enterTransition = MaterialFadeThrough().apply {
                duration = 300
            }
            exitTransition = MaterialFadeThrough().apply {
                duration = 300
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            usePasswordButton.setOnClickListener(this@AuthenticationActivity)
            usePinButton.setOnClickListener(this@AuthenticationActivity)
            usePatternButton.setOnClickListener(this@AuthenticationActivity)
            useBiometricButton.setOnClickListener(this@AuthenticationActivity)
            forgotPasswordButton.setOnClickListener(this@AuthenticationActivity)
            switchUserButton.setOnClickListener(this@AuthenticationActivity)
            rootLayout.setOnClickListener(this@AuthenticationActivity)
        }
    }
    
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.usePasswordButton -> showPasswordDialog()
            R.id.usePinButton -> showPinDialog()
            R.id.usePatternButton -> showPatternDialog()
            R.id.useBiometricButton -> startBiometricAuthentication()
            R.id.forgotPasswordButton -> showForgotPasswordDialog()
            R.id.switchUserButton -> switchUser()
            else -> {
                if (!isAuthenticating && !isLockedOut()) {
                    startAuthentication()
                }
            }
        }
    }
    
    private fun checkSecuritySettings() {
        when {
            !preferenceManager.isAppLockEnabled -> {
                authenticationCallback(true)
                return
            }
            isLockedOut() -> {
                handleLockoutState()
                return
            }
        }
        
        updateUIForAvailableMethods()
    }
    
    private fun updateUIForAvailableMethods() {
        binding.apply {
            val hasPassword = preferenceManager.isPasswordSet
            val hasPin = preferenceManager.isPinSet
            val hasPattern = preferenceManager.isPatternSet
            val hasBiometric = exoryBiometricManager.isBiometricAvailable(this@AuthenticationActivity)
            
            usePasswordButton.visibility = if (hasPassword) View.VISIBLE else View.GONE
            usePinButton.visibility = if (hasPin) View.VISIBLE else View.GONE
            usePatternButton.visibility = if (hasPattern) View.VISIBLE else View.GONE
            useBiometricButton.visibility = if (hasBiometric) View.VISIBLE else View.GONE
            useBiometricButton.isEnabled = hasBiometric && !isLockedOut()
            
            if (hasBiometric && preferenceManager.isBiometricEnabled && !isLockedOut()) {
                useBiometricButton.callOnClick()
            }
        }
    }
    
    private fun startAuthentication() {
        when {
            preferenceManager.isBiometricEnabled && exoryBiometricManager.isBiometricAvailable(this) -> {
                startBiometricAuthentication()
            }
            preferenceManager.isPasswordSet -> {
                showPasswordDialog()
            }
            preferenceManager.isPinSet -> {
                showPinDialog()
            }
            preferenceManager.isPatternSet -> {
                showPatternDialog()
            }
            else -> {
                authenticationCallback(true)
            }
        }
    }
    
    private fun initializeBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isAuthenticating = false
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            handleBiometricLockout()
                        }
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            if (preferenceManager.isPasswordSet) {
                                showPasswordDialog()
                            } else if (preferenceManager.isPinSet) {
                                showPinDialog()
                            } else if (preferenceManager.isPatternSet) {
                                showPatternDialog()
                            }
                        }
                        else -> {
                            showError(errString.toString())
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticating = false
                    authenticationAttempts = 0
                    authenticationCallback(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    authenticationAttempts++
                    showError(getString(R.string.biometric_auth_failed))
                    
                    if (authenticationAttempts >= Constants.MAX_BIOMETRIC_ATTEMPTS) {
                        handleBiometricLockout()
                    }
                }
            })
        
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_auth_title))
            .setSubtitle(getString(R.string.biometric_auth_subtitle))
            .setDescription(getString(R.string.biometric_auth_description))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .apply {
                if (preferenceManager.isPasswordSet || 
                    preferenceManager.isPinSet || 
                    preferenceManager.isPatternSet) {
                    setNegativeButtonText(getString(R.string.use_other_method))
                }
            }.build()
    }
    
    private fun startBiometricAuthentication() {
        if (!exoryBiometricManager.isBiometricAvailable(this)) {
            showError(getString(R.string.biometric_not_available))
            return
        }
        
        if (isLockedOut()) {
            handleLockoutState()
            return
        }
        
        try {
            isAuthenticating = true
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            isAuthenticating = false
            showError(getString(R.string.biometric_auth_error))
        }
    }
    
    private fun showPasswordDialog() {
        if (isLockedOut()) {
            handleLockoutState()
            return
        }
        
        PasswordAuthenticationDialog(this,
            onSuccess = {
                authenticationAttempts = 0
                authenticationCallback(true)
            },
            onFailure = {
                authenticationAttempts++
                if (authenticationAttempts >= Constants.MAX_PASSWORD_ATTEMPTS) {
                    handleLockout()
                }
            },
            onForgotPassword = {
                showForgotPasswordDialog()
            }
        ).show()
    }
    
    private fun showPinDialog() {
        if (isLockedOut()) {
            handleLockoutState()
            return
        }
        
        PinAuthenticationDialog(this,
            onSuccess = {
                authenticationAttempts = 0
                authenticationCallback(true)
            },
            onFailure = {
                authenticationAttempts++
                if (authenticationAttempts >= Constants.MAX_PIN_ATTEMPTS) {
                    handleLockout()
                }
            },
            onForgotPin = {
                showForgotPasswordDialog()
            }
        ).show()
    }
    
    private fun showPatternDialog() {
        if (isLockedOut()) {
            handleLockoutState()
            return
        }
        
        PatternAuthenticationDialog(this,
            onSuccess = {
                authenticationAttempts = 0
                authenticationCallback(true)
            },
            onFailure = {
                authenticationAttempts++
                if (authenticationAttempts >= Constants.MAX_PATTERN_ATTEMPTS) {
                    handleLockout()
                }
            },
            onForgotPattern = {
                showForgotPasswordDialog()
            }
        ).show()
    }
    
    private fun handleAuthenticationSuccess() {
        preferenceManager.wasAppProtectionHandled = true
        preferenceManager.lastSuccessfulAuthTime = System.currentTimeMillis()
        authenticationAttempts = 0
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    private fun handleAuthenticationFailure() {
        if (!isLockedOut()) {
            showError(getString(R.string.authentication_failed))
        }
    }
    
    private fun handleLockout() {
        lockoutEndTime = System.currentTimeMillis() + Constants.LOCKOUT_DURATION
        preferenceManager.lockoutEndTime = lockoutEndTime
        handleLockoutState()
    }
    
    private fun handleBiometricLockout() {
        lockoutEndTime = System.currentTimeMillis() + Constants.BIOMETRIC_LOCKOUT_DURATION
        preferenceManager.lockoutEndTime = lockoutEndTime
        handleLockoutState()
    }
    
    private fun isLockedOut(): Boolean {
        return lockoutEndTime > System.currentTimeMillis() ||
               preferenceManager.lockoutEndTime > System.currentTimeMillis()
    }
    
    private fun handleLockoutState() {
        binding.apply {
            usePasswordButton.isEnabled = false
            usePinButton.isEnabled = false
            usePatternButton.isEnabled = false
            useBiometricButton.isEnabled = false
            
            val remainingTime = (lockoutEndTime - System.currentTimeMillis()) / 1000
            val minutes = remainingTime / 60
            val seconds = remainingTime % 60
            
            val message = getString(R.string.lockout_message, minutes, seconds)
            showError(message)
            
            lifecycleScope.launch {
                delay(Constants.LOCKOUT_DURATION)
                resetLockoutState()
            }
        }
    }
    
    private fun resetLockoutState() {
        lockoutEndTime = 0
        preferenceManager.lockoutEndTime = 0
        authenticationAttempts = 0
        
        binding.apply {
            usePasswordButton.isEnabled = true
            usePinButton.isEnabled = true
            usePatternButton.isEnabled = true
            useBiometricButton.isEnabled = true
        }
    }
    
    private fun showForgotPasswordDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.forgot_password_title)
            .setMessage(R.string.forgot_password_message)
            .setPositiveButton(R.string.reset) { _, _ ->
                performSecurityReset()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun performSecurityReset() {
        lifecycleScope.launch {
            try {
                // Clear all security preferences
                preferenceManager.clearSecurityPreferences()
                
                // Clear encrypted data
                encryptionManager.clearAllKeys()
                
                // Reset authentication attempts
                authenticationAttempts = 0
                lockoutEndTime = 0
                
                showSuccess(getString(R.string.security_reset_complete))
                
                // Restart activity
                recreate()
            } catch (e: Exception) {
                showError(getString(R.string.security_reset_failed))
            }
        }
    }
    
    private fun switchUser() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.switch_user_title)
            .setMessage(R.string.switch_user_message)
            .setPositiveButton(R.string.switch_user) { _, _ ->
                preferenceManager.clearCurrentUser()
                startActivity(LoginActivity.getIntent(this))
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.rootLayout, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.error_color))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.rootLayout, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success_color))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }
    
    private fun addFlagsSecure() {
        if (preferenceManager.preventScreenshots) {
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }
    
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, AuthenticationActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }
    }
}
