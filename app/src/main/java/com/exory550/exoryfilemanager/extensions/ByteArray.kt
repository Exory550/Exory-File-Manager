package com.exory550.exoryfilemanager.extensions

import android.util.Base64
import java.io.*
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Extension functions for ByteArray
 */

/**
 * Converts byte array to hex string
 */
fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}

/**
 * Converts byte array to hex string with spaces
 */
fun ByteArray.toHexStringWithSpaces(): String {
    return joinToString(" ") { "%02x".format(it) }
}

/**
 * Converts byte array to Base64 string
 */
fun ByteArray.toBase64(flags: Int = Base64.NO_WRAP): String {
    return Base64.encodeToString(this, flags)
}

/**
 * Converts byte array to Base64 URL-safe string
 */
fun ByteArray.toBase64UrlSafe(): String {
    return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_WRAP)
}

/**
 * Converts byte array to string with specified charset
 */
fun ByteArray.toString(charset: Charset = Charsets.UTF_8): String {
    return String(this, charset)
}

/**
 * Converts byte array to UTF-8 string
 */
fun ByteArray.toUtf8String(): String {
    return String(this, Charsets.UTF_8)
}

/**
 * Converts byte array to ASCII string
 */
fun ByteArray.toAsciiString(): String {
    return String(this, Charsets.US_ASCII)
}

/**
 * Returns MD5 hash of byte array
 */
fun ByteArray.md5(): ByteArray {
    return MessageDigest.getInstance("MD5").digest(this)
}

/**
 * Returns MD5 hash as hex string
 */
fun ByteArray.md5Hex(): String {
    return md5().toHexString()
}

/**
 * Returns SHA-1 hash of byte array
 */
fun ByteArray.sha1(): ByteArray {
    return MessageDigest.getInstance("SHA-1").digest(this)
}

/**
 * Returns SHA-1 hash as hex string
 */
fun ByteArray.sha1Hex(): String {
    return sha1().toHexString()
}

/**
 * Returns SHA-256 hash of byte array
 */
fun ByteArray.sha256(): ByteArray {
    return MessageDigest.getInstance("SHA-256").digest(this)
}

/**
 * Returns SHA-256 hash as hex string
 */
fun ByteArray.sha256Hex(): String {
    return sha256().toHexString()
}

/**
 * Returns SHA-512 hash of byte array
 */
fun ByteArray.sha512(): ByteArray {
    return MessageDigest.getInstance("SHA-512").digest(this)
}

/**
 * Returns SHA-512 hash as hex string
 */
fun ByteArray.sha512Hex(): String {
    return sha512().toHexString()
}

/**
 * Compresses byte array using GZIP
 */
fun ByteArray.gzipCompress(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    GZIPOutputStream(byteArrayOutputStream).use { gzip ->
        gzip.write(this)
    }
    return byteArrayOutputStream.toByteArray()
}

/**
 * Decompresses byte array using GZIP
 */
fun ByteArray.gzipDecompress(): ByteArray {
    val byteArrayInputStream = ByteArrayInputStream(this)
    val byteArrayOutputStream = ByteArrayOutputStream()
    GZIPInputStream(byteArrayInputStream).use { gzip ->
        gzip.copyTo(byteArrayOutputStream)
    }
    return byteArrayOutputStream.toByteArray()
}

/**
 * Writes byte array to file
 */
fun ByteArray.writeToFile(file: File): Boolean {
    return try {
        file.parentFile?.mkdirs()
        file.writeBytes(this)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Writes byte array to file at path
 */
fun ByteArray.writeToFile(path: String): Boolean {
    return writeToFile(File(path))
}

/**
 * Returns a copy of byte array from start index to end index
 */
fun ByteArray.slice(start: Int, end: Int): ByteArray {
    return copyOfRange(start, end.coerceAtMost(size))
}

/**
 * Returns true if byte array starts with prefix
 */
fun ByteArray.startsWith(prefix: ByteArray): Boolean {
    if (size < prefix.size) return false
    return prefix.indices.all { this[it] == prefix[it] }
}

/**
 * Returns true if byte array ends with suffix
 */
fun ByteArray.endsWith(suffix: ByteArray): Boolean {
    if (size < suffix.size) return false
    val offset = size - suffix.size
    return suffix.indices.all { this[offset + it] == suffix[it] }
}

/**
 * Returns index of first occurrence of pattern
 */
fun ByteArray.indexOf(pattern: ByteArray, startIndex: Int = 0): Int {
    if (pattern.isEmpty()) return startIndex
    if (startIndex < 0) return -1
    
    outer@ for (i in startIndex..size - pattern.size) {
        for (j in pattern.indices) {
            if (this[i + j] != pattern[j]) {
                continue@outer
            }
        }
        return i
    }
    return -1
}

/**
 * Returns index of last occurrence of pattern
 */
fun ByteArray.lastIndexOf(pattern: ByteArray): Int {
    if (pattern.isEmpty()) return size - 1
    if (size < pattern.size) return -1
    
    for (i in size - pattern.size downTo 0) {
        var match = true
        for (j in pattern.indices) {
            if (this[i + j] != pattern[j]) {
                match = false
                break
            }
        }
        if (match) return i
    }
    return -1
}

/**
 * Returns true if byte array contains pattern
 */
fun ByteArray.contains(pattern: ByteArray): Boolean {
    return indexOf(pattern) >= 0
}

/**
 * Replaces all occurrences of oldBytes with newBytes
 */
fun ByteArray.replace(oldBytes: ByteArray, newBytes: ByteArray): ByteArray {
    val output = ByteArrayOutputStream()
    var i = 0
    while (i <= size - oldBytes.size) {
        if (startsWith(oldBytes, i)) {
            output.write(newBytes)
            i += oldBytes.size
        } else {
            output.write(this[i].toInt())
            i++
        }
    }
    while (i < size) {
        output.write(this[i].toInt())
        i++
    }
    return output.toByteArray()
}

private fun ByteArray.startsWith(prefix: ByteArray, start: Int): Boolean {
    if (size - start < prefix.size) return false
    for (j in prefix.indices) {
        if (this[start + j] != prefix[j]) return false
    }
    return true
}

/**
 * Returns a copy of byte array with bytes reversed
 */
fun ByteArray.reversed(): ByteArray {
    return reversedArray()
}

/**
 * Returns a copy of byte array with bytes XORed with key
 */
fun ByteArray.xor(key: Byte): ByteArray {
    val result = ByteArray(size)
    for (i in indices) {
        result[i] = (this[i].toInt() xor key.toInt()).toByte()
    }
    return result
}

/**
 * Returns a copy of byte array with bytes XORed with key array
 */
fun ByteArray.xor(key: ByteArray): ByteArray {
    val result = ByteArray(size)
    for (i in indices) {
        result[i] = (this[i].toInt() xor key[i % key.size].toInt()).toByte()
    }
    return result
}

/**
 * Converts byte array to unsigned byte list (0-255)
 */
fun ByteArray.toUByteList(): List<Int> {
    return map { it.toInt() and 0xFF }
}

/**
 * Converts byte array to integer (big-endian)
 */
fun ByteArray.toInt(): Int {
    require(size >= 4) { "Byte array too small" }
    return (this[0].toInt() and 0xFF shl 24) or
            (this[1].toInt() and 0xFF shl 16) or
            (this[2].toInt() and 0xFF shl 8) or
            (this[3].toInt() and 0xFF)
}

/**
 * Converts byte array to long (big-endian)
 */
fun ByteArray.toLong(): Long {
    require(size >= 8) { "Byte array too small" }
    var result = 0L
    for (i in 0..7) {
        result = result shl 8
        result = result or (this[i].toLong() and 0xFF)
    }
    return result
}

/**
 * Returns a copy of byte array without leading zero bytes
 */
fun ByteArray.trimLeadingZeros(): ByteArray {
    val firstNonZero = indexOfFirst { it != 0.toByte() }
    return if (firstNonZero < 0) byteArrayOf() else copyOfRange(firstNonZero, size)
}

/**
 * Returns a copy of byte array without trailing zero bytes
 */
fun ByteArray.trimTrailingZeros(): ByteArray {
    val lastNonZero = indexOfLast { it != 0.toByte() }
    return if (lastNonZero < 0) byteArrayOf() else copyOfRange(0, lastNonZero + 1)
}

/**
 * Returns a copy of byte array trimmed of leading and trailing zero bytes
 */
fun ByteArray.trimZeros(): ByteArray {
    return trimLeadingZeros().trimTrailingZeros()
}
