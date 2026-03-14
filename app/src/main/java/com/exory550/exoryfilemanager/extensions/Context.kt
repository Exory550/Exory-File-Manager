package com.exory550.exoryfilemanager.extensions

import android.app.Activity
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Vibrator
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.ExoryApplication
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Extension functions for Context
 */

/**
 * Get application instance
 */
val Context.application: ExoryApplication
    get() = applicationContext as ExoryApplication

/**
 * Check if device is in dark mode
 */
val Context.isDarkTheme: Boolean
    get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

/**
 * Get screen width in pixels
 */
val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

/**
 * Get screen height in pixels
 */
val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

/**
 * Get screen density
 */
val Context.screenDensity: Float
    get() = resources.displayMetrics.density

/**
 * Get status bar height
 */
val Context.statusBarHeight: Int
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

/**
 * Get navigation bar height
 */
val Context.navigationBarHeight: Int
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

/**
 * Get navigation bar width
 */
val Context.navigationBarWidth: Int
    get() {
        var result = 0
        val resourceId = resources.getIdentifier("navigation_bar_width", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

/**
 * Check if navigation bar is on the bottom
 */
val Context.isNavigationBarBottom: Boolean
    get() = screenWidth > screenHeight

/**
 * Convert dp to pixels
 */
fun Context.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
}

/**
 * Convert pixels to dp
 */
fun Context.pxToDp(px: Float): Float {
    return px / resources.displayMetrics.density
}

/**
 * Convert sp to pixels
 */
fun Context.spToPx(sp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics).toInt()
}

/**
 * Get color from resources
 */
fun Context.getColorCompat(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

/**
 * Get drawable from resources
 */
fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}

/**
 * Get string from resources
 */
fun Context.getString(@StringRes stringRes: Int, vararg formatArgs: Any): String {
    return resources.getString(stringRes, *formatArgs)
}

/**
 * Get quantity string from resources
 */
fun Context.getQuantityString(@PluralsRes pluralRes: Int, quantity: Int, vararg formatArgs: Any): String {
    return resources.getQuantityString(pluralRes, quantity, *formatArgs)
}

/**
 * Get dimension in pixels
 */
fun Context.getDimensionPixelSize(@DimenRes dimenRes: Int): Int {
    return resources.getDimensionPixelSize(dimenRes)
}

/**
 * Get integer from resources
 */
fun Context.getInteger(@IntegerRes intRes: Int): Int {
    return resources.getInteger(intRes)
}

/**
 * Get boolean from resources
 */
fun Context.getBoolean(@BoolRes boolRes: Int): Boolean {
    return resources.getBoolean(boolRes)
}

/**
 * Get theme attribute
 */
fun Context.getThemeAttribute(@AttrRes attrRes: Int): TypedValue {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue
}

/**
 * Get theme attribute color
 */
fun Context.getThemeAttributeColor(@AttrRes attrRes: Int): Int {
    val typedValue = getThemeAttribute(attrRes)
    return typedValue.data
}

/**
 * Get theme attribute drawable
 */
fun Context.getThemeAttributeDrawable(@AttrRes attrRes: Int): Drawable? {
    val typedValue = getThemeAttribute(attrRes)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getDrawable(this, typedValue.resourceId)
    } else {
        null
    }
}

/**
 * Show toast message
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Show toast message from resource
 */
fun Context.showToast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageRes, duration).show()
}

/**
 * Show long toast message
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

/**
 * Show long toast message from resource
 */
fun Context.showLongToast(@StringRes messageRes: Int) {
    Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
}

/**
 * Get layout inflater
 */
val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

/**
 * Hide keyboard
 */
fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * Show keyboard
 */
fun Context.showKeyboard(view: View) {
    view.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Toggle keyboard
 */
fun Context.toggleKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

/**
 * Check if keyboard is visible
 */
fun Context.isKeyboardVisible(): Boolean {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.isAcceptingText
}

/**
 * Vibrate device
 */
fun Context.vibrate(duration: Long = 50) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
}

/**
 * Check if network is available
 */
val Context.isNetworkAvailable: Boolean
    get() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            networkInfo.isConnected
        }
    }

/**
 * Check if WiFi is connected
 */
val Context.isWifiConnected: Boolean
    get() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            @Suppress("DEPRECATION")
            networkInfo?.isConnected ?: false
        }
    }

/**
 * Check if mobile data is connected
 */
val Context.isMobileDataConnected: Boolean
    get() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            @Suppress("DEPRECATION")
            networkInfo?.isConnected ?: false
        }
    }

/**
 * Check if Ethernet is connected
 */
val Context.isEthernetConnected: Boolean
    get() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)
            @Suppress("DEPRECATION")
            networkInfo?.isConnected ?: false
        }
    }

/**
 * Get external storage path
 */
val Context.externalStoragePath: String
    get() = Environment.getExternalStorageDirectory().absolutePath

/**
 * Get cache directory size
 */
val Context.cacheSize: Long
    get() {
        val cacheDir = cacheDir
        return getFolderSize(cacheDir)
    }

/**
 * Get external cache directory size
 */
val Context.externalCacheSize: Long
    get() {
        val externalCacheDir = externalCacheDir ?: return 0
        return getFolderSize(externalCacheDir)
    }

private fun getFolderSize(file: File): Long {
    return if (file.isDirectory) {
        file.listFiles()?.sumOf { getFolderSize(it) } ?: 0
    } else {
        file.length()
    }
}

/**
 * Clear cache directory
 */
fun Context.clearCache(): Boolean {
    return try {
        val cacheDir = cacheDir
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Clear external cache directory
 */
fun Context.clearExternalCache(): Boolean {
    return try {
        val externalCacheDir = externalCacheDir ?: return true
        externalCacheDir.deleteRecursively()
        externalCacheDir.mkdirs()
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Get application version name
 */
val Context.appVersionName: String
    get() = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }

/**
 * Get application version code
 */
val Context.appVersionCode: Int
    get() = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionCode
        }
    } catch (e: Exception) {
        1
    }

/**
 * Get application name
 */
val Context.appName: String
    get() = try {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        ""
    }

/**
 * Get installer package name
 */
val Context.installerPackage: String?
    get() = packageManager.getInstallerPackageName(packageName)

/**
 * Check if app is installed
 */
fun Context.isAppInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

/**
 * Open app settings
 */
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
    }
    startActivity(intent)
}

/**
 * Open URL in browser
 */
fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    } catch (e: Exception) {
        showToast(R.string.cannot_open_url)
    }
}

/**
 * Send email
 */
fun Context.sendEmail(
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

/**
 * Share text
 */
fun Context.shareText(text: String, subject: String? = null) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
    }
    startActivity(Intent.createChooser(intent, getString(R.string.share)))
}

/**
 * Share file
 */
fun Context.shareFile(file: File, mimeType: String = "*/*") {
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

/**
 * Share multiple files
 */
fun Context.shareMultipleFiles(files: List<File>, mimeType: String = "*/*") {
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

/**
 * Copy text to clipboard
 */
fun Context.copyToClipboard(text: String, label: String = "Copied text") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    showToast(R.string.copied_to_clipboard)
}

/**
 * Paste text from clipboard
 */
fun Context.pasteFromClipboard(): String? {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.text?.toString()
}

/**
 * Check if clipboard has text
 */
fun Context.hasClipboardText(): Boolean {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.hasPrimaryClip() && clipboard.primaryClip?.itemCount ?: 0 > 0
}

/**
 * Clear clipboard
 */
fun Context.clearClipboard() {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
}

/**
 * Get raw resource as string
 */
fun Context.getRawString(@RawRes rawRes: Int): String {
    return resources.openRawResource(rawRes).bufferedReader().use { it.readText() }
}

/**
 * Get assets file as string
 */
fun Context.getAssetString(assetPath: String): String {
    return assets.open(assetPath).bufferedReader().use { it.readText() }
}

/**
 * Get resource URI
 */
fun Context.getResourceUri(@RawRes rawRes: Int): Uri {
    return Uri.parse("android.resource://$packageName/$rawRes")
}

/**
 * Get file provider URI for file
 */
fun Context.getFileUri(file: File): Uri {
    return FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
}

/**
 * Check if permission is granted
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Check if multiple permissions are granted
 */
fun Context.hasPermissions(vararg permissions: String): Boolean {
    return permissions.all { hasPermission(it) }
}

/**
 * Start activity with animation
 */
fun Context.startActivityWithAnimation(intent: Intent) {
    if (this is Activity) {
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    } else {
        startActivity(intent)
    }
}

/**
 * Get device language
 */
val Context.deviceLanguage: String
    get() = Locale.getDefault().language

/**
 * Get device country
 */
val Context.deviceCountry: String
    get() = Locale.getDefault().country

/**
 * Get device timezone
 */
val Context.deviceTimeZone: String
    get() = TimeZone.getDefault().id

/**
 * Check if device is tablet
 */
val Context.isTablet: Boolean
    get() = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

/**
 * Get display density name
 */
val Context.displayDensityName: String
    get() = when (resources.displayMetrics.densityDpi) {
        DisplayMetrics.DENSITY_LOW -> "LDPI"
        DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
        DisplayMetrics.DENSITY_HIGH -> "HDPI"
        DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
        DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
        DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
        else -> "Unknown"
    }

val android.content.Context.cacheSize: Long
    get() = cacheDir?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L

val android.content.Context.externalCacheSize: Long
    get() = externalCacheDir?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L

fun android.content.Context.clearCache(): Boolean {
    return try { cacheDir?.deleteRecursively() ?: false } catch (e: Exception) { false }
}

fun android.content.Context.clearExternalCache(): Boolean {
    return try { externalCacheDir?.deleteRecursively() ?: false } catch (e: Exception) { false }
}
