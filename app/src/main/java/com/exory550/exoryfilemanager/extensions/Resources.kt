package com.exory550.exoryfilemanager.extensions

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.*

/**
 * Extension functions for Resources
 */

/**
 * Get color with theme attribute support
 */
fun Resources.getColorCompat(@ColorRes colorRes: Int): Int {
    return getColor(colorRes, null)
}

/**
 * Get color state list with theme attribute support
 */
fun Resources.getColorStateListCompat(@ColorRes colorRes: Int): android.content.res.ColorStateList {
    return getColorStateList(colorRes, null)
}

/**
 * Get dimension in pixels
 */
fun Resources.getDimensionPixelSizeCompat(@DimenRes dimenRes: Int): Int {
    return getDimensionPixelSize(dimenRes)
}

/**
 * Get dimension in float
 */
fun Resources.getDimensionCompat(@DimenRes dimenRes: Int): Float {
    return getDimension(dimenRes)
}

/**
 * Get integer from resources
 */
fun Resources.getIntegerCompat(@IntegerRes intRes: Int): Int {
    return getInteger(intRes)
}

/**
 * Get boolean from resources
 */
fun Resources.getBooleanCompat(@BoolRes boolRes: Int): Boolean {
    return getBoolean(boolRes)
}

/**
 * Get string array from resources
 */
fun Resources.getStringArrayCompat(@ArrayRes arrayRes: Int): Array<String> {
    return getStringArray(arrayRes)
}

/**
 * Get int array from resources
 */
fun Resources.getIntArrayCompat(@ArrayRes arrayRes: Int): IntArray {
    return getIntArray(arrayRes)
}

/**
 * Get typed array from resources
 */
fun Resources.obtainTypedArrayCompat(@ArrayRes arrayRes: Int): android.content.res.TypedArray {
    return obtainTypedArray(arrayRes)
}

/**
 * Get theme attribute value
 */
fun Resources.getThemeAttribute(
    @AttrRes attrRes: Int,
    resolveRefs: Boolean = true
): TypedValue {
    val typedValue = TypedValue()
    if (!getValue(attrRes, typedValue, resolveRefs)) {
        throw Resources.NotFoundException("Attribute resource ID #0x${attrRes.toHexString()}")
    }
    return typedValue
}

/**
 * Get theme attribute as dimension
 */
fun Resources.getThemeAttributeDimension(
    @AttrRes attrRes: Int,
    resolveRefs: Boolean = true
): Float {
    val typedValue = getThemeAttribute(attrRes, resolveRefs)
    return typedValue.getDimension(displayMetrics)
}

/**
 * Get theme attribute as color
 */
fun Resources.getThemeAttributeColor(
    @AttrRes attrRes: Int,
    resolveRefs: Boolean = true
): Int {
    val typedValue = getThemeAttribute(attrRes, resolveRefs)
    return typedValue.data
}

/**
 * Get theme attribute as resource ID
 */
fun Resources.getThemeAttributeResourceId(
    @AttrRes attrRes: Int,
    resolveRefs: Boolean = true
): Int {
    val typedValue = getThemeAttribute(attrRes, resolveRefs)
    return typedValue.resourceId
}

/**
 * Get theme attribute as string
 */
fun Resources.getThemeAttributeString(
    @AttrRes attrRes: Int,
    resolveRefs: Boolean = true
): String {
    val typedValue = getThemeAttribute(attrRes, resolveRefs)
    return typedValue.string?.toString() ?: ""
}

/**
 * Check if resource exists
 */
fun Resources.hasResource(@AnyRes resId: Int): Boolean {
    return try {
        getResourceName(resId)
        true
    } catch (e: Resources.NotFoundException) {
        false
    }
}

/**
 * Get resource entry name
 */
fun Resources.getResourceEntryName(@AnyRes resId: Int): String {
    return getResourceEntryName(resId)
}

/**
 * Get resource type name
 */
fun Resources.getResourceTypeName(@AnyRes resId: Int): String {
    return getResourceTypeName(resId)
}

/**
 * Get resource package name
 */
fun Resources.getResourcePackageName(@AnyRes resId: Int): String {
    return getResourcePackageName(resId)
}

/**
 * Convert dp to pixels
 */
fun Resources.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        displayMetrics
    ).toInt()
}

/**
 * Convert sp to pixels
 */
fun Resources.spToPx(sp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        displayMetrics
    ).toInt()
}

/**
 * Convert pixels to dp
 */
fun Resources.pxToDp(px: Float): Float {
    return px / displayMetrics.density
}

/**
 * Convert pixels to sp
 */
fun Resources.pxToSp(px: Float): Float {
    return px / displayMetrics.scaledDensity
}

/**
 * Get screen width in pixels
 */
val Resources.screenWidth: Int
    get() = displayMetrics.widthPixels

/**
 * Get screen height in pixels
 */
val Resources.screenHeight: Int
    get() = displayMetrics.heightPixels

/**
 * Get screen density
 */
val Resources.screenDensity: Float
    get() = displayMetrics.density

/**
 * Get screen density DPI
 */
val Resources.screenDensityDpi: Int
    get() = displayMetrics.densityDpi

/**
 * Get screen scaled density
 */
val Resources.screenScaledDensity: Float
    get() = displayMetrics.scaledDensity

/**
 * Get screen width in dp
 */
val Resources.screenWidthDp: Int
    get() = configuration.screenWidthDp

/**
 * Get screen height in dp
 */
val Resources.screenHeightDp: Int
    get() = configuration.screenHeightDp

/**
 * Get smallest screen width in dp
 */
val Resources.smallestScreenWidthDp: Int
    get() = configuration.smallestScreenWidthDp

/**
 * Check if device is in landscape orientation
 */
val Resources.isLandscape: Boolean
    get() = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * Check if device is in portrait orientation
 */
val Resources.isPortrait: Boolean
    get() = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

/**
 * Check if device is in dark mode
 */
val Resources.isDarkTheme: Boolean
    get() = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

/**
 * Check if device is in light mode
 */
val Resources.isLightTheme: Boolean
    get() = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO

/**
 * Get status bar height
 */
val Resources.statusBarHeight: Int
    get() {
        var result = 0
        val resourceId = getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = getDimensionPixelSize(resourceId)
        }
        return result
    }

/**
 * Get navigation bar height
 */
val Resources.navigationBarHeight: Int
    get() {
        var result = 0
        val resourceId = getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = getDimensionPixelSize(resourceId)
        }
        return result
    }

/**
 * Get navigation bar width
 */
val Resources.navigationBarWidth: Int
    get() {
        var result = 0
        val resourceId = getIdentifier("navigation_bar_width", "dimen", "android")
        if (resourceId > 0) {
            result = getDimensionPixelSize(resourceId)
        }
        return result
    }

/**
 * Get action bar height
 */
val Resources.actionBarHeight: Int
    get() {
        val typedValue = TypedValue()
        if (getValue(android.R.attr.actionBarSize, typedValue, true)) {
            return TypedValue.complexToDimensionPixelSize(typedValue.data, displayMetrics)
        }
        return 0
    }

/**
 * Check if device is tablet (screen width >= 600dp)
 */
val Resources.isTablet: Boolean
    get() = screenWidthDp >= 600

/**
 * Check if device is large tablet (screen width >= 720dp)
 */
val Resources.isLargeTablet: Boolean
    get() = screenWidthDp >= 720

/**
 * Check if device is xlarge tablet (screen width >= 960dp)
 */
val Resources.isXLargeTablet: Boolean
    get() = screenWidthDp >= 960

/**
 * Check if device has keyboard
 */
val Resources.hasKeyboard: Boolean
    get() = configuration.keyboard != Configuration.KEYBOARD_NOKEYS

/**
 * Check if device has touchscreen
 */
val Resources.hasTouchScreen: Boolean
    get() = configuration.touchscreen != Configuration.TOUCHSCREEN_NOTOUCH

/**
 * Check if device is in car mode
 */
val Resources.isCarMode: Boolean
    get() = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_CAR

/**
 * Check if device is in desk mode
 */
val Resources.isDeskMode: Boolean
    get() = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_DESK

/**
 * Check if device is in television mode
 */
val Resources.isTelevision: Boolean
    get() = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION

/**
 * Check if device is in appliance mode
 */
val Resources.isAppliance: Boolean
    get() = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_APPLIANCE

/**
 * Check if device is in watch mode
 */
val Resources.isWatch: Boolean
    get() = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_WATCH

/**
 * Check if device is in vr mode
 */
val Resources.isVrMode: Boolean
    get() = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_VR_HEADSET

/**
 * Get font scale
 */
val Resources.fontScale: Float
    get() = configuration.fontScale

/**
 * Get locale
 */
val Resources.currentLocale: java.util.Locale
    get() = configuration.locales.get(0) ?: java.util.Locale.getDefault()

/**
 * Get density name
 */
val Resources.densityName: String
    get() = when (displayMetrics.densityDpi) {
        DisplayMetrics.DENSITY_LOW -> "LDPI"
        DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
        DisplayMetrics.DENSITY_HIGH -> "HDPI"
        DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
        DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
        DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
        DisplayMetrics.DENSITY_TV -> "TVDPI"
        else -> "Unknown"
    }

/**
 * Parse a color string to int
 */
fun Resources.parseColor(colorString: String): Int {
    return android.graphics.Color.parseColor(colorString)
}

/**
 * Load drawable with theme
 */
fun Resources.getDrawableWithTheme(@DrawableRes drawableRes: Int): Drawable? {
    return getDrawable(drawableRes, null)
}

/**
 * Load drawable for density
 */
fun Resources.getDrawableForDensity(
    @DrawableRes drawableRes: Int,
    density: Int
): Drawable? {
    return getDrawableForDensity(drawableRes, density, null)
}

/**
 * Get resource as typed value
 */
fun Resources.getTypedValue(@AnyRes resId: Int): TypedValue {
    val value = TypedValue()
    getValue(resId, value, true)
    return value
}

/**
 * Get resource as XML block
 */
fun Resources.getXmlBlock(@XmlRes xmlRes: Int): android.content.res.XmlResourceParser {
    return getXml(xmlRes)
}

/**
 * Get animation from resource
 */
fun Resources.getAnimation(@AnimRes animRes: Int): android.view.animation.Animation {
    return android.view.animation.AnimationUtils.loadAnimation(
        android.app.Application(),
        animRes
    )
}

/**
 * Get interpolator from resource
 */
fun Resources.getInterpolator(@InterpolatorRes interpolatorRes: Int): android.view.animation.Interpolator {
    return android.view.animation.AnimationUtils.loadInterpolator(
        android.app.Application(),
        interpolatorRes
    )
}

/**
 * Get layout from resource
 */
fun Resources.getLayout(@LayoutRes layoutRes: Int): android.content.res.XmlResourceParser {
    return getLayout(layoutRes)
}

/**
 * Check if current configuration is in night mode
 */
val Resources.isNightMode: Boolean
    get() = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

/**
 * Get current UI mode type
 */
val Resources.uiModeType: Int
    get() = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK

/**
 * Get current night mode
 */
val Resources.nightMode: Int
    get() = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

/**
 * Get readable UI mode type string
 */
val Resources.uiModeTypeString: String
    get() = when (uiModeType) {
        Configuration.UI_MODE_TYPE_NORMAL -> "Normal"
        Configuration.UI_MODE_TYPE_DESK -> "Desk"
        Configuration.UI_MODE_TYPE_CAR -> "Car"
        Configuration.UI_MODE_TYPE_TELEVISION -> "Television"
        Configuration.UI_MODE_TYPE_APPLIANCE -> "Appliance"
        Configuration.UI_MODE_TYPE_WATCH -> "Watch"
        Configuration.UI_MODE_TYPE_VR_HEADSET -> "VR"
        else -> "Unknown"
    }

/**
 * Get readable night mode string
 */
val Resources.nightModeString: String
    get() = when (nightMode) {
        Configuration.UI_MODE_NIGHT_NO -> "Day"
        Configuration.UI_MODE_NIGHT_YES -> "Night"
        Configuration.UI_MODE_NIGHT_UNDEFINED -> "Undefined"
        else -> "Unknown"
    }
