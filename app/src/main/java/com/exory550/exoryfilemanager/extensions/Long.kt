package com.exory550.exoryfilemanager.extensions

import android.content.Context
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Extension functions for Long
 */

/**
 * Convert long to formatted file size
 */
fun Long.toFormattedFileSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    var size = this.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format(Locale.US, "%.1f %s", size, units[unitIndex])
}

/**
 * Convert long to file size string with specific decimal places
 */
fun Long.toFormattedFileSize(decimals: Int): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
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
 * Convert long to binary file size (using 1024)
 */
fun Long.toBinaryFileSize(): String {
    val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB")
    var size = this.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format(Locale.US, "%.1f %s", size, units[unitIndex])
}

/**
 * Convert long to decimal file size (using 1000)
 */
fun Long.toDecimalFileSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    var size = this.toDouble()
    var unitIndex = 0
    
    while (size >= 1000 && unitIndex < units.size - 1) {
        size /= 1000
        unitIndex++
    }
    
    return String.format(Locale.US, "%.1f %s", size, units[unitIndex])
}

/**
 * Convert long to time string (HH:MM:SS)
 */
fun Long.toTimeString(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

/**
 * Convert long to duration string (Xh Ym Zs)
 */
fun Long.toDurationString(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    
    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0) append("${minutes}m ")
        append("${seconds}s")
    }
}

/**
 * Convert long to short duration string (e.g., "2h 30m")
 */
fun Long.toShortDurationString(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
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
 * Convert long to date string with context (Today, Yesterday, etc.)
 */
fun Long.toRelativeDateString(context: Context): String {
    val now = System.currentTimeMillis()
    val today = now.toDateString("yyyyMMdd")
    val yesterday = (now - TimeUnit.DAYS.toMillis(1)).toDateString("yyyyMMdd")
    val date = toDateString("yyyyMMdd")
    
    return when (date) {
        today -> context.getString(android.R.string.today)
        yesterday -> context.getString(android.R.string.yesterday)
        else -> toDateString("dd MMM yyyy")
    }
}

/**
 * Convert long to time ago string
 */
fun Long.toTimeAgo(context: Context): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    val months = days / 30
    val years = days / 365
    
    return when {
        years > 0 -> context.getString(if (years == 1L) R.string.year_ago else R.string.years_ago, years)
        months > 0 -> context.getString(if (months == 1L) R.string.month_ago else R.string.months_ago, months)
        days > 0 -> context.getString(if (days == 1L) R.string.day_ago else R.string.days_ago, days)
        hours > 0 -> context.getString(if (hours == 1L) R.string.hour_ago else R.string.hours_ago, hours)
        minutes > 0 -> context.getString(if (minutes == 1L) R.string.minute_ago else R.string.minutes_ago, minutes)
        seconds > 0 -> context.getString(if (seconds == 1L) R.string.second_ago else R.string.seconds_ago, seconds)
        else -> context.getString(R.string.just_now)
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
    return byteArrayOf(
        (this shr 56 and 0xFF).toByte(),
        (this shr 48 and 0xFF).toByte(),
        (this shr 40 and 0xFF).toByte(),
        (this shr 32 and 0xFF).toByte(),
        (this shr 24 and 0xFF).toByte(),
        (this shr 16 and 0xFF).toByte(),
        (this shr 8 and 0xFF).toByte(),
        (this and 0xFF).toByte()
    )
}

/**
 * Convert long to byte array (little-endian)
 */
fun Long.toByteArrayLittleEndian(): ByteArray {
    return byteArrayOf(
        (this and 0xFF).toByte(),
        (this shr 8 and 0xFF).toByte(),
        (this shr 16 and 0xFF).toByte(),
        (this shr 24 and 0xFF).toByte(),
        (this shr 32 and 0xFF).toByte(),
        (this shr 40 and 0xFF).toByte(),
        (this shr 48 and 0xFF).toByte(),
        (this shr 56 and 0xFF).toByte()
    )
}

/**
 * Convert long to hex string
 */
fun Long.toHexString(withPrefix: Boolean = true): String {
    val hex = java.lang.Long.toHexString(this).uppercase(Locale.US)
    return if (withPrefix) "0x$hex" else hex
}

/**
 * Convert long to binary string
 */
fun Long.toBinaryString(): String {
    return java.lang.Long.toBinaryString(this)
}

/**
 * Convert long to octal string
 */
fun Long.toOctalString(): String {
    return java.lang.Long.toOctalString(this)
}

/**
 * Convert long to formatted number with thousands separator
 */
fun Long.toFormattedNumber(locale: Locale = Locale.getDefault()): String {
    return NumberFormat.getNumberInstance(locale).format(this)
}

/**
 * Convert long to currency string
 */
fun Long.toCurrency(locale: Locale = Locale.getDefault()): String {
    return NumberFormat.getCurrencyInstance(locale).format(this)
}

/**
 * Convert long to percentage of another value
 */
fun Long.percentageOf(total: Long): Double {
    if (total == 0L) return 0.0
    return (this.toDouble() / total.toDouble()) * 100.0
}

/**
 * Convert long to percentage of another value with formatting
 */
fun Long.percentageOf(total: Long, decimals: Int): String {
    return String.format(Locale.US, "%.${decimals}f%%", percentageOf(total))
}

/**
 * Convert long to roman numerals
 */
fun Long.toRomanNumerals(): String {
    if (this < 1 || this > 3999) return this.toString()
    
    val romanNumerals = arrayOf(
        "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    )
    val values = longArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    
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
 * Convert long to ordinal string (1st, 2nd, 3rd, etc.)
 */
fun Long.toOrdinal(): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    
    return when (this % 100) {
        11L, 12L, 13L -> "${this}th"
        else -> "${this}${suffixes[(this % 10).toInt()]}"
    }
}

/**
 * Convert long to spoken form (e.g., 1234 -> "one thousand two hundred thirty-four")
 */
fun Long.toSpoken(locale: Locale = Locale.US): String {
    // This is a simplified version
    if (this == 0L) return "zero"
    
    val units = arrayOf("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")
    val teens = arrayOf("ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
    val tens = arrayOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")
    val thousands = arrayOf("", "thousand", "million", "billion", "trillion")
    
    fun convertLessThanThousand(n: Long): String {
        return when {
            n == 0L -> ""
            n < 10L -> units[n.toInt()]
            n < 20L -> teens[(n - 10).toInt()]
            n < 100L -> {
                val ten = tens[(n / 10).toInt()]
                val unit = units[(n % 10).toInt()]
                if (unit.isEmpty()) ten else "$ten-$unit"
            }
            else -> {
                val hundred = units[(n / 100).toInt()] + " hundred"
                val rest = convertLessThanThousand(n % 100)
                if (rest.isEmpty()) hundred else "$hundred $rest"
            }
        }
    }
    
    var num = this
    var result = ""
    var thousandIndex = 0
    
    while (num > 0) {
        if (num % 1000 != 0L) {
            val part = convertLessThanThousand(num % 1000)
            result = if (thousandIndex == 0) {
                part
            } else {
                "$part ${thousands[thousandIndex]} $result"
            }.trim()
        }
        num /= 1000
        thousandIndex++
    }
    
    return result.trim()
}

/**
 * Extension functions for Long? types
 */

/**
 * Check if long is null or zero
 */
fun Long?.isNullOrZero(): Boolean {
    return this == null || this == 0L
}

/**
 * Return long if not null, else default
 */
fun Long?.orDefault(default: Long): Long {
    return this ?: default
}

/**
 * Return long if not null and not zero, else default
 */
fun Long?.orDefaultIfZero(default: Long): Long {
    return if (this == null || this == 0L) default else this
}

/**
 * Safe division
 */
infix fun Long.safeDiv(other: Long): Long {
    return if (other == 0L) 0 else this / other
}

/**
 * Safe division returning double
 */
infix fun Long.safeDivDouble(other: Long): Double {
    return if (other == 0L) 0.0 else this.toDouble() / other.toDouble()
}

/**
 * Format as KB/MB/GB with specific unit
 */
fun Long.toUnit(unit: String): String {
    return when (unit.uppercase()) {
        "B" -> "$this B"
        "KB" -> String.format(Locale.US, "%.2f KB", this / 1024.0)
        "MB" -> String.format(Locale.US, "%.2f MB", this / (1024.0 * 1024.0))
        "GB" -> String.format(Locale.US, "%.2f GB", this / (1024.0 * 1024.0 * 1024.0))
        "TB" -> String.format(Locale.US, "%.2f TB", this / (1024.0 * 1024.0 * 1024.0 * 1024.0))
        else -> toFormattedFileSize()
    }
}

/**
 * Convert to human readable speed (e.g., "2.5 MB/s")
 */
fun Long.toSpeedString(): String {
    return "${toFormattedFileSize()}/s"
}

/**
 * Convert to human readable speed with specific time unit
 */
fun Long.toSpeedString(timeUnit: TimeUnit): String {
    val bytesPerSecond = when (timeUnit) {
        TimeUnit.SECONDS -> this
        TimeUnit.MINUTES -> this / 60
        TimeUnit.HOURS -> this / 3600
        else -> this
    }
    return "${bytesPerSecond.toFormattedFileSize()}/s"
}
