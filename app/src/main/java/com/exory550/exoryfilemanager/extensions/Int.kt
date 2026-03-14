package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * Extension functions for Int and numeric types
 */

/**
 * Convert int to dp
 */
val Int.dp: Int
    get() = (this * android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        android.content.res.Resources.getSystem().displayMetrics
    )).toInt()

/**
 * Convert int to sp
 */
val Int.sp: Int
    get() = (this * android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        android.content.res.Resources.getSystem().displayMetrics
    )).toInt()

/**
 * Convert int to px
 */
val Int.px: Int
    get() = this

/**
 * Convert int to formatted file size
 */
fun Int.toFormattedFileSize(): String {
    return this.toLong().toFormattedFileSize()
}

/**
 * Convert int to file size string (B, KB, MB, etc.)
 */
fun Int.toFileSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = this.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", size, units[unitIndex])
}

/**
 * Convert int to percentage string
 */
fun Int.toPercentage(): String {
    return "${this}%"
}

/**
 * Convert int to roman numerals
 */
fun Int.toRomanNumerals(): String {
    if (this < 1 || this > 3999) return this.toString()
    
    val romanNumerals = arrayOf(
        "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    )
    val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    
    var num = this
    val result = StringBuilder()
    
    for (i in values.indices) {
        while (num >= values[i]) {
            num -= values[i]
            result.append(romanNumerals[i])
        }
    }
    
    return result.toString()
}

/**
 * Convert int to ordinal string (1st, 2nd, 3rd, etc.)
 */
fun Int.toOrdinal(): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    
    return when (this % 100) {
        11, 12, 13 -> "${this}th"
        else -> "${this}${suffixes[this % 10]}"
    }
}

/**
 * Convert int to time string (HH:MM:SS)
 */
fun Int.toTimeString(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Check if int is between min and max (inclusive)
 */
infix fun Int.between(range: Pair<Int, Int>): Boolean {
    return this >= range.first && this <= range.second
}

/**
 * Check if int is between min and max (inclusive)
 */
fun Int.isBetween(min: Int, max: Int): Boolean {
    return this in min..max
}

/**
 * Clamp int between min and max
 */
fun Int.clamp(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Convert int to boolean (true if non-zero)
 */
fun Int.toBoolean(): Boolean {
    return this != 0
}

/**
 * Convert int to hex string
 */
fun Int.toHexString(withPrefix: Boolean = true): String {
    val hex = Integer.toHexString(this).uppercase(Locale.US)
    return if (withPrefix) "0x$hex" else hex
}

/**
 * Convert int to binary string
 */
fun Int.toBinaryString(): String {
    return Integer.toBinaryString(this)
}

/**
 * Convert int to octal string
 */
fun Int.toOctalString(): String {
    return Integer.toOctalString(this)
}

/**
 * Convert int to formatted number with thousands separator
 */
fun Int.toFormattedNumber(locale: Locale = Locale.getDefault()): String {
    return NumberFormat.getNumberInstance(locale).format(this)
}

/**
 * Convert int to currency string
 */
fun Int.toCurrency(locale: Locale = Locale.getDefault()): String {
    return NumberFormat.getCurrencyInstance(locale).format(this)
}

/**
 * Get ordinal suffix for number
 */
val Int.ordinalSuffix: String
    get() = when (this % 10) {
        1 -> if (this % 100 == 11) "th" else "st"
        2 -> if (this % 100 == 12) "th" else "nd"
        3 -> if (this % 100 == 13) "th" else "rd"
        else -> "th"
    }

/**
 * Get number with ordinal suffix
 */
val Int.withOrdinal: String
    get() = "$this$ordinalSuffix"

/**
 * Extension functions for Long
 */

/**
 * Convert long to formatted file size
 */

/**
 * Convert long to file size string with specific decimal places
 */
fun Long.toFormattedFileSize(decimals: Int): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    var size = this.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    val format = DecimalFormat("#.##")
    format.maximumFractionDigits = decimals
    return "${format.format(size)} ${units[unitIndex]}"
}

/**
 * Convert long to time string (HH:MM:SS)
 */
fun Long.toTimeString(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Convert long to duration string (Xh Ym Zs)
 */
fun Long.toDurationString(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    
    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        append("${seconds}s")
    }
}

/**
 * Convert long to date string
 */
fun Long.toDateString(format: String = "dd/MM/yyyy HH:mm"): String {
    return try {
        val sdf = java.text.SimpleDateFormat(format, Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        this.toString()
    }
}

/**
 * Check if long is between min and max (inclusive)
 */
fun Long.isBetween(min: Long, max: Long): Boolean {
    return this in min..max
}

/**
 * Clamp long between min and max
 */
fun Long.clamp(min: Long, max: Long): Long {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Convert long to byte array (8 bytes, big-endian)
 */
fun Long.toByteArray(): ByteArray {
    val bytes = ByteArray(8)
    bytes[0] = (this shr 56 and 0xFF).toByte()
    bytes[1] = (this shr 48 and 0xFF).toByte()
    bytes[2] = (this shr 40 and 0xFF).toByte()
    bytes[3] = (this shr 32 and 0xFF).toByte()
    bytes[4] = (this shr 24 and 0xFF).toByte()
    bytes[5] = (this shr 16 and 0xFF).toByte()
    bytes[6] = (this shr 8 and 0xFF).toByte()
    bytes[7] = (this and 0xFF).toByte()
    return bytes
}

/**
 * Extension functions for Float
 */

/**
 * Convert float to percentage string
 */
fun Float.toPercentage(decimals: Int = 1): String {
    return String.format("%.${decimals}f%%", this)
}

/**
 * Convert float to dp
 */
val Float.dp: Float
    get() = this * android.content.res.Resources.getSystem().displayMetrics.density

/**
 * Convert float to sp
 */
val Float.sp: Float
    get() = this * android.content.res.Resources.getSystem().displayMetrics.scaledDensity

/**
 * Convert float to px
 */
val Float.px: Float
    get() = this

/**
 * Clamp float between min and max
 */
fun Float.clamp(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Check if float is between min and max (inclusive)
 */
fun Float.isBetween(min: Float, max: Float): Boolean {
    return this >= min && this <= max
}

/**
 * Extension functions for Double
 */

/**
 * Convert double to percentage string
 */
fun Double.toPercentage(decimals: Int = 2): String {
    return String.format("%.${decimals}f%%", this)
}

/**
 * Clamp double between min and max
 */
fun Double.clamp(min: Double, max: Double): Double {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Check if double is between min and max (inclusive)
 */
fun Double.isBetween(min: Double, max: Double): Boolean {
    return this >= min && this <= max
}

/**
 * Convert double to formatted number
 */
fun Double.toFormattedNumber(decimals: Int = 2): String {
    return String.format("%,.${decimals}f", this)
}

/**
 * Extension functions for Boolean
 */

/**
 * Convert boolean to int (1 for true, 0 for false)
 */
fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

/**
 * Convert boolean to string (Yes/No)
 */
fun Boolean.toYesNo(context: Context): String {
    return if (this) {
        context.getString(android.R.string.yes)
    } else {
        context.getString(android.R.string.no)
    }
}

/**
 * Convert boolean to on/off string
 */
fun Boolean.toOnOff(context: Context): String {
    return if (this) {
        context.getString(android.R.string.on)
    } else {
        context.getString(android.R.string.off)
    }
}

/**
 * Execute block if true
 */
inline fun Boolean.ifTrue(block: () -> Unit): Boolean {
    if (this) block()
    return this
}

/**
 * Execute block if false
 */
inline fun Boolean.ifFalse(block: () -> Unit): Boolean {
    if (!this) block()
    return this
}

/**
 * Return value based on boolean
 */
inline fun <T> Boolean.ifElse(onTrue: () -> T, onFalse: () -> T): T {
    return if (this) onTrue() else onFalse()
}

/**
 * Extension functions for Color Int
 */

/**
 * Convert color to hex string
 */
@ColorInt
fun Int.toColorHex(withAlpha: Boolean = true): String {
    return if (withAlpha) {
        String.format("#%08X", this)
    } else {
        String.format("#%06X", this and 0xFFFFFF)
    }
}

/**
 * Get color brightness
 */
@ColorInt
fun Int.getBrightness(): Float {
    return (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
}

/**
 * Check if color is light
 */
@ColorInt
fun Int.isLight(): Boolean {
    return getBrightness() > 0.5
}

/**
 * Check if color is dark
 */
@ColorInt
fun Int.isDark(): Boolean {
    return getBrightness() <= 0.5
}

/**
 * Darken color by percentage
 */
@ColorInt
fun Int.darken(percentage: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    hsv[2] = (hsv[2] * (1 - percentage)).coerceIn(0f, 1f)
    return Color.HSVToColor(Color.alpha(this), hsv)
}

/**
 * Lighten color by percentage
 */
@ColorInt
fun Int.lighten(percentage: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    hsv[2] = (hsv[2] * (1 + percentage)).coerceIn(0f, 1f)
    return Color.HSVToColor(Color.alpha(this), hsv)
}

/**
 * Set color alpha
 */
@ColorInt
fun Int.withAlpha(alpha: Int): Int {
    return (this and 0x00FFFFFF) or (alpha shl 24)
}

/**
 * Set color alpha by percentage
 */
@ColorInt
fun Int.withAlphaPercentage(percentage: Float): Int {
    val alpha = (255 * percentage.coerceIn(0f, 1f)).toInt()
    return withAlpha(alpha)
}

/**
 * Blend two colors
 */
@ColorInt
fun Int.blendWith(@ColorInt other: Int, ratio: Float): Int {
    val inverse = 1 - ratio
    val a = (Color.alpha(this) * ratio + Color.alpha(other) * inverse).toInt()
    val r = (Color.red(this) * ratio + Color.red(other) * inverse).toInt()
    val g = (Color.green(this) * ratio + Color.green(other) * inverse).toInt()
    val b = (Color.blue(this) * ratio + Color.blue(other) * inverse).toInt()
    return Color.argb(a, r, g, b)
}
