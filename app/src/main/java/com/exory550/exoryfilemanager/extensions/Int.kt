package com.exory550.exoryfilemanager.extensions

fun Int.toFormattedFileSize(decimals: Int = 1): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = this.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return "%.${decimals}f %s".format(size, units[unitIndex])
}

fun Int.toDurationString(): String {
    val seconds = this % 60
    val minutes = (this / 60) % 60
    val hours = this / 3600
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

fun Int.toByteArray(): ByteArray {
    return byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}
