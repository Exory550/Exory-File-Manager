package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.exory550.exoryfilemanager.ExoryApplication
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.dialogs.ProgressDialog
import com.exory550.exoryfilemanager.extensions.hideKeyboard
import com.exory550.exoryfilemanager.extensions.isNetworkAvailable
import com.exory550.exoryfilemanager.utils.ConnectivityManager
import com.exory550.exoryfilemanager.utils.Constants
import com.exory550.exoryfilemanager.utils.LocaleManager
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.exory550.exoryfilemanager.utils.ThemeManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseAbstractActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    @Inject
    lateinit var themeManager: ThemeManager
    
    @Inject
    lateinit var localeManager: LocaleManager
    
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    
    private var progressDialog: ProgressDialog? = null
    private var isActivityVisible = false
    private var isDestroyedBySystem = false
    
    protected abstract val layoutRes: Int
    
    protected open val showToolbar: Boolean = true
    protected open val showBackButton: Boolean = false
    protected open val toolbarTitle: String? = null
    protected open val toolbarTitleRes: Int? = null
    
    protected open val requiresNetwork: Boolean = false
    protected open val handleNetworkChanges: Boolean = false
    
    protected open val enableDataBinding: Boolean = false
    protected open val enableViewModel: Boolean = false
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        val deniedPermanently = permissions.entries.any {
            !it.value && !shouldShowRequestPermissionRationale(it.key)
        }
        
        onPermissionResult(granted, deniedPermanently, permissions)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        applyLocale()
        super.onCreate(savedInstanceState)
        
        if (enableDataBinding) {
            // Data binding will be set up in child classes
        } else {
            setContentView(layoutRes)
        }
        
        setupToolbar()
        observeNetworkChanges()
        observeDataFlows()
        initializeViews()
        setupObservers()
        
        if (requiresNetwork && !isNetworkAvailable()) {
            handleNoNetwork()
        }
    }
    
    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        isDestroyedBySystem = false
        
        if (preferenceManager.preventScreenshots) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        
        if (handleNetworkChanges) {
            connectivityManager.registerNetworkCallback()
        }
    }
    
    override fun onPause() {
        super.onPause()
        isActivityVisible = false
        
        if (handleNetworkChanges) {
            connectivityManager.unregisterNetworkCallback()
        }
    }
    
    override fun onDestroy() {
        isDestroyedBySystem = !isChangingConfigurations
        dismissProgress()
        super.onDestroy()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && showBackButton) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun attachBaseContext(newBase: Context) {
        val context = localeManager.setLocale(newBase)
        super.attachBaseContext(context)
    }
    
    protected open fun initializeViews() {
        // Override in child classes
    }
    
    protected open fun setupObservers() {
        // Override in child classes
    }
    
    protected open fun observeDataFlows() {
        // Override in child classes
    }
    
    protected open fun onPermissionResult(
        granted: Boolean,
        deniedPermanently: Boolean,
        permissions: Map<String, Boolean>
    ) {
        // Override in child classes
    }
    
    private fun setupToolbar() {
        if (showToolbar) {
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(showBackButton)
                setDisplayShowHomeEnabled(showBackButton)
                
                when {
                    toolbarTitle != null -> title = toolbarTitle
                    toolbarTitleRes != null -> title = getString(toolbarTitleRes!!)
                }
            }
        } else {
            supportActionBar?.hide()
        }
    }
    
    private fun applyTheme() {
        themeManager.applyTheme(this)
    }
    
    private fun applyLocale() {
        localeManager.applyLocale(this)
    }
    
    private fun observeNetworkChanges() {
        if (handleNetworkChanges) {
            lifecycleScope.launch {
                connectivityManager.networkState.collectLatest { isConnected ->
                    onNetworkStateChanged(isConnected)
                }
            }
        }
    }
    
    protected open fun onNetworkStateChanged(isConnected: Boolean) {
        if (!isConnected && requiresNetwork) {
            handleNoNetwork()
        }
    }
    
    protected open fun handleNoNetwork() {
        showErrorSnackbar(R.string.error_no_network)
    }
    
    protected fun showProgress(
        message: String = getString(R.string.loading),
        cancelable: Boolean = false
    ) {
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
        
        progressDialog = ProgressDialog(this).apply {
            setMessage(message)
            setCancelable(cancelable)
            show()
        }
    }
    
    protected fun showProgress(@StringRes messageRes: Int, cancelable: Boolean = false) {
        showProgress(getString(messageRes), cancelable)
    }
    
    protected fun dismissProgress() {
        if (progressDialog?.isShowing == true && !isDestroyedBySystem) {
            progressDialog?.dismiss()
        }
        progressDialog = null
    }
    
    protected fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_LONG,
        actionText: String? = null,
        actionListener: View.OnClickListener? = null
    ) {
        val rootView = findViewById<View>(android.R.id.content) ?: return
        
        val snackbar = Snackbar.make(rootView, message, duration)
        
        if (actionText != null && actionListener != null) {
            snackbar.setAction(actionText, actionListener)
        }
        
        snackbar.show()
    }
    
    protected fun showSnackbar(
        @StringRes messageRes: Int,
        duration: Int = Snackbar.LENGTH_LONG,
        @StringRes actionTextRes: Int? = null,
        actionListener: View.OnClickListener? = null
    ) {
        showSnackbar(
            getString(messageRes),
            duration,
            actionTextRes?.let { getString(it) },
            actionListener
        )
    }
    
    protected fun showErrorSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val rootView = findViewById<View>(android.R.id.content) ?: return
        
        Snackbar.make(rootView, message, duration)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.error_color))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }
    
    protected fun showErrorSnackbar(@StringRes messageRes: Int, duration: Int = Snackbar.LENGTH_LONG) {
        showErrorSnackbar(getString(messageRes), duration)
    }
    
    protected fun showSuccessSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val rootView = findViewById<View>(android.R.id.content) ?: return
        
        Snackbar.make(rootView, message, duration)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success_color))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }
    
    protected fun showSuccessSnackbar(@StringRes messageRes: Int, duration: Int = Snackbar.LENGTH_LONG) {
        showSuccessSnackbar(getString(messageRes), duration)
    }
    
    protected fun showWarningSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val rootView = findViewById<View>(android.R.id.content) ?: return
        
        Snackbar.make(rootView, message, duration)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.warning_color))
            .setTextColor(ContextCompat.getColor(this, R.color.black))
            .show()
    }
    
    protected fun showWarningSnackbar(@StringRes messageRes: Int, duration: Int = Snackbar.LENGTH_LONG) {
        showWarningSnackbar(getString(messageRes), duration)
    }
    
    protected fun showDialog(
        title: String,
        message: String,
        positiveText: String = getString(R.string.ok),
        negativeText: String? = null,
        positiveAction: (() -> Unit)? = null,
        negativeAction: (() -> Unit)? = null,
        cancelable: Boolean = true
    ) {
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton(positiveText) { _, _ ->
                positiveAction?.invoke()
            }
        
        if (negativeText != null) {
            builder.setNegativeButton(negativeText) { _, _ ->
                negativeAction?.invoke()
            }
        }
        
        builder.show()
    }
    
    protected fun showDialog(
        @StringRes titleRes: Int,
        @StringRes messageRes: Int,
        @StringRes positiveTextRes: Int = R.string.ok,
        @StringRes negativeTextRes: Int? = null,
        positiveAction: (() -> Unit)? = null,
        negativeAction: (() -> Unit)? = null,
        cancelable: Boolean = true
    ) {
        showDialog(
            getString(titleRes),
            getString(messageRes),
            getString(positiveTextRes),
            negativeTextRes?.let { getString(it) },
            positiveAction,
            negativeAction,
            cancelable
        )
    }
    
    protected fun requestPermissions(permissions: Array<String>) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            onPermissionResult(true, false, emptyMap())
        }
    }
    
    protected fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    protected fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(it) }
    }
    
    protected fun hideKeyboard() {
        currentFocus?.hideKeyboard()
    }
    
    protected fun <T> Flow<T>.launchAndCollect(collector: suspend (T) -> Unit) {
        lifecycleScope.launch {
            this@launchAndcollect.collectLatest(collector)
        }
    }
    
    protected inline fun <reified T> logDebug(message: String) {
        Timber.tag(T::class.java.simpleName).d(message)
    }
    
    protected inline fun <reified T> logError(message: String, throwable: Throwable? = null) {
        Timber.tag(T::class.java.simpleName).e(throwable, message)
    }
    
    protected fun isActivityActive(): Boolean {
        return !isFinishing && !isDestroyed && isActivityVisible
    }
    
    protected fun startActivityWithAnimation(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    
    protected fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
    
    protected fun navigateUpWithAnimation() {
        supportFinishAfterTransition()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
    
    protected fun getAppVersion(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    protected fun getAppVersionCode(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionCode
            }
        } catch (e: Exception) {
            1
        }
    }
    
    protected fun isDebugBuild(): Boolean {
        return ExoryApplication.isDebug()
    }
    
    companion object {
        private const val TAG = "BaseAbstractActivity"
    }
}
