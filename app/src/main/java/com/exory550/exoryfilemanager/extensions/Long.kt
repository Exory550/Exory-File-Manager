package com.exory550.exoryfilemanager.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedFileSize(decimals: Int = 1): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = this.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return "%.${decimals}f %s".format(size, units[unitIndex])
}

fun Long.toTimeString(): String {
    val seconds = (this / 1000) % 60
    val minutes = (this / (1000 * 60)) % 60
    val hours = (this / (1000 * 60 * 60)) % 24
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

fun Long.toDurationString(): String {
    val totalSeconds = this / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

fun Long.toDateString(format: String = "dd/MM/yyyy HH:mm"): String {
    return try {
        SimpleDateFormat(format, Locale.getDefault()).format(Date(this))
    } catch (e: Exception) { toString() }
}

fun Long.toByteArray(): ByteArray {
    return byteArrayOf(
        (this shr 56).toByte(), (this shr 48).toByte(),
        (this shr 40).toByte(), (this shr 32).toByte(),
        (this shr 24).toByte(), (this shr 16).toByte(),
        (this shr 8).toByte(), this.toByte()
    )
}

fun Long.isBetween(min: Long, max: Long): Boolean = this in min..max

fun Long.clamp(min: Long, max: Long): Long = when {
    this < min -> min
    this > max -> max
    else -> this
}
