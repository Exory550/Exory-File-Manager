package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.net.Uri
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Patterns
import androidx.annotation.ColorInt
import androidx.core.text.HtmlCompat
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

/**
 * Extension functions for String
 */

/**
 * Check if string is null or empty
 */
fun String?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

/**
 * Check if string is null or blank
 */
fun String?.isNullOrBlank(): Boolean {
    return this == null || this.isBlank()
}

/**
 * Return string if not null, otherwise default
 */
fun String?.orDefault(default: String = ""): String {
    return this ?: default
}

/**
 * Return string if not empty, otherwise default
 */
fun String?.orDefaultIfEmpty(default: String): String {
    return if (this.isNullOrEmpty()) default else this
}

/**
 * Return string if not blank, otherwise default
 */
fun String?.orDefaultIfBlank(default: String): String {
    return if (this.isNullOrBlank()) default else this
}

/**
 * Capitalize first letter
 */
fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) {
        replaceFirstChar { it.titlecase(Locale.getDefault()) }
    } else {
        this
    }
}

/**
 * Capitalize all words
 */
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.capitalizeFirst() }
}

/**
 * Convert to title case
 */
fun String.toTitleCase(): String {
    return split(" ").joinToString(" ") { word ->
        if (word.length > 2) {
            word.capitalizeFirst()
        } else {
            word.lowercase(Locale.getDefault())
        }
    }
}

/**
 * Convert to sentence case
 */
fun String.toSentenceCase(): String {
    if (isEmpty()) return this
    return this[0].uppercase() + substring(1).lowercase(Locale.getDefault())
}

/**
 * Truncate string with ellipsis
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (length <= maxLength) {
        this
    } else {
        substring(0, maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Truncate at word boundary
 */
fun String.truncateAtWord(maxLength: Int, ellipsis: String = "..."): String {
    if (length <= maxLength) return this
    
    val truncated = substring(0, maxLength - ellipsis.length)
    val lastSpace = truncated.lastIndexOf(' ')
    
    return if (lastSpace > 0) {
        truncated.substring(0, lastSpace) + ellipsis
    } else {
        truncated + ellipsis
    }
}

/**
 * Remove accents/diacritics
 */
fun String.removeAccents(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(normalized).replaceAll("")
}

/**
 * Convert to slug (URL-friendly string)
 */
fun String.toSlug(): String {
    return removeAccents()
        .lowercase(Locale.getDefault())
        .replace("[^a-z0-9]+".toRegex(), "-")
        .trim('-')
}

/**
 * Convert to safe filename
 */
fun String.toSafeFileName(): String {
    return replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .replace(" ", "_")
        .trim()
}

/**
 * Extract numbers from string
 */
fun String.extractNumbers(): String {
    return filter { it.isDigit() }
}

/**
 * Extract letters from string
 */
fun String.extractLetters(): String {
    return filter { it.isLetter() }
}

/**
 * Extract alphanumeric characters
 */
fun String.extractAlphanumeric(): String {
    return filter { it.isLetterOrDigit() }
}

/**
 * Check if string contains only digits
 */
fun String.isNumeric(): Boolean {
    return all { it.isDigit() }
}

/**
 * Check if string contains only letters
 */
fun String.isAlpha(): Boolean {
    return all { it.isLetter() }
}

/**
 * Check if string contains only letters and digits
 */
fun String.isAlphanumeric(): Boolean {
    return all { it.isLetterOrDigit() }
}

/**
 * Check if string is a valid email
 */
fun String.isValidEmail(): Boolean {
    return isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Check if string is a valid phone number
 */
fun String.isValidPhone(): Boolean {
    val phonePattern = Patterns.PHONE
    return isNotEmpty() && phonePattern.matcher(this).matches()
}

/**
 * Check if string is a valid URL
 */
fun String.isValidUrl(): Boolean {
    return isNotEmpty() && Patterns.WEB_URL.matcher(this).matches()
}

/**
 * Check if string is a valid IP address
 */
fun String.isValidIpAddress(): Boolean {
    val ipPattern = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )
    return ipPattern.matcher(this).matches()
}

/**
 * Check if string is a valid hex color
 */
fun String.isValidHexColor(): Boolean {
    val hexPattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    return hexPattern.matcher(this).matches()
}

/**
 * Convert hex color to ColorInt
 */
@ColorInt
fun String.parseHexColor(): Int {
    return android.graphics.Color.parseColor(this)
}

/**
 * Convert to boolean
 */
fun String.toBooleanOrDefault(default: Boolean = false): Boolean {
    return when (lowercase(Locale.US)) {
        "true", "yes", "1", "y", "on" -> true
        "false", "no", "0", "n", "off" -> false
        else -> default
    }
}

/**
 * Convert to int safely
 */
fun String.toIntOrNull(): Int? {
    return try {
        toInt()
    } catch (e: NumberFormatException) {
        null
    }
}

/**
 * Convert to long safely
 */
fun String.toLongOrNull(): Long? {
    return try {
        toLong()
    } catch (e: NumberFormatException) {
        null
    }
}

/**
 * Convert to float safely
 */
fun String.toFloatOrNull(): Float? {
    return try {
        toFloat()
    } catch (e: NumberFormatException) {
        null
    }
}

/**
 * Convert to double safely
 */
fun String.toDoubleOrNull(): Double? {
    return try {
        toDouble()
    } catch (e: NumberFormatException) {
        null
    }
}

/**
 * URL encode string
 */
fun String.urlEncode(): String {
    return try {
        URLEncoder.encode(this, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        this
    }
}

/**
 * URL decode string
 */
fun String.urlDecode(): String {
    return try {
        URLDecoder.decode(this, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        this
    }
}

/**
 * Convert to Base64
 */
fun String.toBase64(): String {
    return try {
        android.util.Base64.encodeToString(toByteArray(), android.util.Base64.NO_WRAP)
    } catch (e: Exception) {
        this
    }
}

/**
 * Convert from Base64
 */
fun String.fromBase64(): String {
    return try {
        android.util.Base64.decode(this, android.util.Base64.NO_WRAP).toString(Charsets.UTF_8)
    } catch (e: Exception) {
        this
    }
}

/**
 * Convert to MD5 hash
 */
fun String.md5(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        this
    }
}

/**
 * Convert to SHA-1 hash
 */
fun String.sha1(): String {
    return try {
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(toByteArray())
        digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        this
    }
}

/**
 * Convert to SHA-256 hash
 */
fun String.sha256(): String {
    return try {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(toByteArray())
        digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        this
    }
}

/**
 * Convert to HTML spanned text
 */
fun String.toHtml(): Spanned {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

/**
 * Strip HTML tags
 */
fun String.stripHtml(): String {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}

/**
 * Escape for JSON
 */
fun String.escapeJson(): String {
    return replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

/**
 * Escape for XML
 */
fun String.escapeXml(): String {
    return replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

/**
 * Convert to camel case
 */
fun String.toCamelCase(): String {
    return split(" ", "_", "-")
        .filter { it.isNotEmpty() }
        .joinToString("") { it.capitalizeFirst() }
        .replaceFirstChar { it.lowercase(Locale.getDefault()) }
}

/**
 * Convert to snake case
 */
fun String.toSnakeCase(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .lowercase(Locale.getDefault())
        .replace(Regex("[\\s-]+"), "_")
}

/**
 * Convert to kebab case
 */
fun String.toKebabCase(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1-$2")
        .lowercase(Locale.getDefault())
        .replace(Regex("[\\s_]+"), "-")
}

/**
 * Count words
 */
val String.wordCount: Int
    get() = split(Regex("\\s+")).filter { it.isNotEmpty() }.size

/**
 * Count characters (excluding whitespace)
 */
val String.characterCount: Int
    get() = count { !it.isWhitespace() }

/**
 * Count sentences
 */
val String.sentenceCount: Int
    get() = split(Regex("[.!?]+")).filter { it.isNotBlank() }.size

/**
 * Count lines
 */
val String.lineCount: Int
    get() = split("\n").size

/**
 * Reverse string
 */
fun String.reverse(): String {
    return reversed()
}

/**
 * Check if string is palindrome
 */
fun String.isPalindrome(): Boolean {
    val cleaned = filter { it.isLetterOrDigit() }.lowercase(Locale.getDefault())
    return cleaned == cleaned.reversed()
}

/**
 * Check if string contains any of the given strings
 */
fun String.containsAny(vararg strings: String, ignoreCase: Boolean = false): Boolean {
    return strings.any { contains(it, ignoreCase) }
}

/**
 * Check if string contains all of the given strings
 */
fun String.containsAll(vararg strings: String, ignoreCase: Boolean = false): Boolean {
    return strings.all { contains(it, ignoreCase) }
}

/**
 * Count occurrences of substring
 */
fun String.countOccurrences(substring: String, ignoreCase: Boolean = false): Int {
    var count = 0
    var index = 0
    val text = if (ignoreCase) lowercase(Locale.getDefault()) else this
    val search = if (ignoreCase) substring.lowercase(Locale.getDefault()) else substring
    
    while (index < text.length) {
        val foundIndex = text.indexOf(search, index)
        if (foundIndex == -1) break
        count++
        index = foundIndex + search.length
    }
    return count
}

/**
 * Get string between two delimiters
 */
fun String.between(start: String, end: String): String? {
    val startIndex = indexOf(start)
    if (startIndex == -1) return null
    
    val endIndex = indexOf(end, startIndex + start.length)
    if (endIndex == -1) return null
    
    return substring(startIndex + start.length, endIndex)
}

/**
 * Repeat string n times
 */
fun String.repeat(n: Int): String {
    return repeat(n)
}

/**
 * Indent string
 */
fun String.indent(spaces: Int): String {
    val indent = " ".repeat(spaces)
    return split("\n").joinToString("\n") { indent + it }
}

/**
 * Center string in field of given width
 */
fun String.center(width: Int, padChar: Char = ' '): String {
    if (length >= width) return this
    
    val leftPadding = (width - length) / 2
    val rightPadding = width - length - leftPadding
    
    return padChar.toString().repeat(leftPadding) + this + padChar.toString().repeat(rightPadding)
}

/**
 * Left pad string
 */
fun String.padLeft(width: Int, padChar: Char = ' '): String {
    if (length >= width) return this
    return padChar.toString().repeat(width - length) + this
}

/**
 * Right pad string
 */
fun String.padRight(width: Int, padChar: Char = ' '): String {
    if (length >= width) return this
    return this + padChar.toString().repeat(width - length)
}

/**
 * Format with arguments
 */
fun String.format(vararg args: Any?): String {
    return String.format(this, *args)
}

/**
 * Format with locale
 */
fun String.format(locale: Locale, vararg args: Any?): String {
    return String.format(locale, this, *args)
}

/**
 * Get initials (e.g., "John Doe" -> "JD")
 */
fun String.initials(): String {
    return split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
}

/**
 * Get first letter of each word
 */
fun String.firstLetters(): String {
    return split(" ")
        .filter { it.isNotEmpty() }
        .map { it.first().uppercase() }
        .joinToString("")
}

/**
 * Abbreviate name (e.g., "Johnathan" -> "John.")
 */
fun String.abbreviate(maxLength: Int): String {
    if (length <= maxLength) return this
    return substring(0, maxLength - 1) + "."
}

/**
 * Convert to human readable format (e.g., "camelCase" -> "Camel Case")
 */
fun String.humanize(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replace(Regex("[_-]"), " ")
        .lowercase(Locale.getDefault())
        .capitalizeWords()
}

/**
 * Convert to plural form (simple)
 */
fun String.pluralize(count: Int): String {
    return if (count == 1) this else "${this}s"
}

/**
 * Check if string matches glob pattern
 */
fun String.matchesGlob(pattern: String): Boolean {
    val regex = pattern
        .replace(".", "\\.")
        .replace("*", ".*")
        .replace("?", ".")
        .toRegex()
    return regex.matches(this)
}

/**
 * Extract URI from string
 */
fun String.toUriOrNull(): Uri? {
    return try {
        Uri.parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * Get file extension from path
 */
fun String.fileExtension(): String {
    return substringAfterLast(".", "").lowercase(Locale.getDefault())
}

/**
 * Get file name from path
 */
fun String.fileName(): String {
    return substringAfterLast("/", "")
}

/**
 * Get file name without extension
 */
fun String.fileNameWithoutExtension(): String {
    val name = fileName()
    return name.substringBeforeLast(".", name)
}

/**
 * Get parent path
 */
fun String.parentPath(): String {
    return substringBeforeLast("/", "")
}

/**
 * Join strings with separator
 */
fun Iterable<String>.joinWith(separator: String = ", "): String {
    return joinToString(separator)
}

/**
 * Join strings with natural language
 */
fun Collection<String>.joinNaturalLanguage(): String {
    return when (size) {
        0 -> ""
        1 -> first()
        2 -> "${first()} and ${last()}"
        else -> {
            val allButLast = take(size - 1).joinToString(", ")
            "$allButLast, and ${last()}"
        }
    }
}

/**
 * Format file size string
 */
fun String.formatFileSize(): String {
    return try {
        toLong().toFormattedFileSize()
    } catch (e: Exception) {
        this
    }
}
