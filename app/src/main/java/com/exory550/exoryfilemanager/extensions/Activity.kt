package com.exory550.exoryfilemanager.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import java.io.File

/**
 * Extension functions for Activity and Fragment classes
 */

// ============== Activity Extensions ==============

fun Activity.hideKeyboard() {
    val view = currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    window.decorView.clearFocus()
}

fun Activity.showKeyboard(view: View) {
    view.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.toggleKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Activity.isKeyboardVisible(): Boolean {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.isAcceptingText
}

fun Activity.setFullScreen() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun Activity.setTranslucentStatusBar() {
    window.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun Activity.setStatusBarColor(@ColorRes colorRes: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = ContextCompat.getColor(this, colorRes)
    }
}

fun Activity.setNavigationBarColor(@ColorRes colorRes: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.navigationBarColor = ContextCompat.getColor(this, colorRes)
    }
}

fun Activity.isDarkTheme(): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }
}

fun Activity.addFragment(
    containerId: Int,
    fragment: Fragment,
    tag: String? = null,
    addToBackStack: Boolean = false
) {
    supportFragmentManager.beginTransaction()
        .add(containerId, fragment, tag)
        .apply {
            if (addToBackStack) addToBackStack(tag)
        }
        .commit()
}

fun Activity.replaceFragment(
    containerId: Int,
    fragment: Fragment,
    tag: String? = null,
    addToBackStack: Boolean = false
) {
    supportFragmentManager.beginTransaction()
        .replace(containerId, fragment, tag)
        .apply {
            if (addToBackStack) addToBackStack(tag)
        }
        .commit()
}

fun Activity.showFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .show(fragment)
        .commit()
}

fun Activity.hideFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .hide(fragment)
        .commit()
}

inline fun FragmentManager.transact(
    action: FragmentTransaction.() -> Unit
) {
    beginTransaction().apply {
        action()
        commit()
    }
}

fun Activity.shareText(text: String, subject: String? = null) {
    ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setText(text)
        .apply {
            if (subject != null) setSubject(subject)
        }
        .startChooser()
}

fun Activity.shareFile(file: File, mimeType: String = "*/*") {
    try {
        val uri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(intent, getString(R.string.share_file)))
    } catch (e: Exception) {
        showToast(R.string.cannot_share_file)
    }
}

fun Activity.shareMultipleFiles(files: List<File>, mimeType: String = "*/*") {
    try {
        val uris = files.map { file ->
            FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        }
        
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(intent, getString(R.string.share_files)))
    } catch (e: Exception) {
        showToast(R.string.cannot_share_files)
    }
}

fun Activity.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (e: Exception) {
        showToast(R.string.cannot_open_url)
    }
}

fun Activity.sendEmail(
    to: Array<String>,
    subject: String? = null,
    body: String? = null,
    attachment: File? = null
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (attachment != null) "*/" else "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, to)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        body?.let { putExtra(Intent.EXTRA_TEXT, it) }
        
        attachment?.let { file ->
            val uri = FileProvider.getUriForFile(
                this@sendEmail,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    try {
        startActivity(Intent.createChooser(intent, getString(R.string.send_email)))
    } catch (e: Exception) {
        showToast(R.string.no_email_app_found)
    }
}

fun Activity.copyToClipboard(text: String, label: String = "Copied text") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    showToast(R.string.copied_to_clipboard)
}

fun Activity.pasteFromClipboard(): String? {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.text?.toString()
}

fun Activity.hasClipboardText(): Boolean {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.hasPrimaryClip() && clipboard.primaryClip?.itemCount ?: 0 > 0
}

fun Activity.clearClipboard() {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
}

fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Activity.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageRes, duration).show()
}

fun Activity.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.showLongToast(@StringRes messageRes: Int) {
    Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
}

fun Activity.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.requestPermissionsIfNeeded(
    permissions: Array<String>,
    requestCode: Int
) {
    val permissionsToRequest = permissions.filter {
        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }.toTypedArray()
    
    if (permissionsToRequest.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, permissionsToRequest, requestCode)
    }
}

fun Activity.shouldShowPermissionRationale(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}

fun Activity.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
    }
    startActivity(intent)
}

fun Activity.isInMultiWindowMode(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        isInMultiWindowMode
    } else {
        false
    }
}

fun Activity.isInPictureInPictureMode(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        isInPictureInPictureMode
    } else {
        false
    }
}

fun Activity.enterPictureInPictureMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val params = PictureInPictureParams.Builder().build()
        enterPictureInPictureMode(params)
    }
}

fun Activity.setKeepScreenOn(keepOn: Boolean) {
    if (keepOn) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

fun Activity.setSecure(secure: Boolean) {
    if (secure) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

fun Activity.disableScreenshots() {
    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

fun Activity.enableScreenshots() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
}

fun Activity.getScreenWidth(): Int {
    return resources.displayMetrics.widthPixels
}

fun Activity.getScreenHeight(): Int {
    return resources.displayMetrics.heightPixels
}

fun Activity.getScreenDensity(): Float {
    return resources.displayMetrics.density
}

fun Activity.getDimensionInPx(dimenRes: Int): Int {
    return resources.getDimensionPixelSize(dimenRes)
}

fun Activity.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Activity.getDrawableCompat(@DrawableRes drawableRes: Int): android.graphics.drawable.Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}

fun Activity.startActivityWithAnimation(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun Activity.finishWithAnimation() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

fun Activity.startActivityForResultWithAnimation(intent: Intent, requestCode: Int) {
    startActivityForResult(intent, requestCode)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun <T : Parcelable> Activity.getParcelableExtraCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(key)
    }
}

fun <T : Parcelable> Activity.getParcelableArrayListExtraCompat(key: String, clazz: Class<T>): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableArrayListExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelableArrayListExtra(key)
    }
}

fun Activity.isActivityDestroyed(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        isDestroyed
    } else {
        isFinishing
    }
}

fun Activity.runOnUiThreadSafe(action: () -> Unit) {
    if (isActivityDestroyed()) return
    runOnUiThread {
        if (!isActivityDestroyed()) {
            action()
        }
    }
}

// ============== Fragment Extensions ==============

fun Fragment.hideKeyboard() {
    activity?.hideKeyboard()
}

fun Fragment.showKeyboard(view: View) {
    activity?.showKeyboard(view)
}

fun Fragment.showToast(message: String) {
    activity?.showToast(message)
}

fun Fragment.showToast(@StringRes messageRes: Int) {
    activity?.showToast(messageRes)
}

fun Fragment.getColorCompat(@ColorRes colorRes: Int): Int {
    return requireContext().getColorCompat(colorRes)
}

fun Fragment.getDrawableCompat(@DrawableRes drawableRes: Int): android.graphics.drawable.Drawable? {
    return ContextCompat.getDrawable(requireContext(), drawableRes)
}

fun Fragment.getScreenWidth(): Int {
    return resources.displayMetrics.widthPixels
}

fun Fragment.getScreenHeight(): Int {
    return resources.displayMetrics.heightPixels
}

fun Fragment.isDarkTheme(): Boolean {
    return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }
}

fun Fragment.openUrl(url: String) {
    activity?.openUrl(url)
}

fun Fragment.copyToClipboard(text: String) {
    activity?.copyToClipboard(text)
}

fun Fragment.shareText(text: String) {
    activity?.shareText(text)
}

// ============== Context Extensions ==============

fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.getAppVersionName(): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }
}

fun Context.getAppVersionCode(): Int {
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

fun Context.getApplicationName(): String {
    return try {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        ""
    }
}

fun Context.getInstallerPackage(): String? {
    return packageManager.getInstallerPackageName(packageName)
}

fun Context.isDebugBuild(): Boolean {
    return BuildConfig.DEBUG
}
