package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.util.Log
import com.exory550.exoryfilemanager.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions for Any/Object classes
 */

// ============== Any Extensions ==============

/**
 * Returns the object as a JSON string
 */
fun Any.toJson(): String {
    return try {
        GsonBuilder().setPrettyPrinting().create().toJson(this)
    } catch (e: Exception) {
        "{}"
    }
}

/**
 * Returns the object as a JSON string without pretty printing
 */
fun Any.toCompactJson(): String {
    return try {
        Gson().toJson(this)
    } catch (e: Exception) {
        "{}"
    }
}

/**
 * Returns a deep copy of the object using JSON serialization
 */
inline fun <reified T> T.deepCopy(): T {
    return try {
        val json = Gson().toJson(this)
        Gson().fromJson(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        this
    }
}

/**
 * Returns the object as a formatted string for debugging
 */
fun Any.debugString(): String {
    return try {
        when (this) {
            is String -> this
            is Number -> toString()
            is Boolean -> toString()
            is Array<*> -> "Array(${size})"
            is Collection<*> -> "${this::class.simpleName}(${size})"
            is Map<*, *> -> "Map(${size})"
            else -> this::class.simpleName ?: "Object"
        }
    } catch (e: Exception) {
        "Unknown"
    }
}

/**
 * Returns the object's hash code as a hex string
 */
fun Any.hashHex(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toString().toByteArray())
        digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        Integer.toHexString(hashCode())
    }
}

/**
 * Returns the object's SHA-256 hash as a hex string
 */
fun Any.sha256(): String {
    return try {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(toString().toByteArray())
        digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}

/**
 * Logs the object if debug mode is enabled
 */
fun Any.logDebug(tag: String = "ExoryDebug") {
    if (BuildConfig.DEBUG) {
        Log.d(tag, toString())
    }
}

/**
 * Logs the object as error
 */
fun Any.logError(tag: String = "ExoryError") {
    Log.e(tag, toString())
}

/**
 * Logs the object with a custom log level
 */
fun Any.log(level: Int = Log.DEBUG, tag: String = "ExoryLog") {
    when (level) {
        Log.VERBOSE -> Log.v(tag, toString())
        Log.DEBUG -> Log.d(tag, toString())
        Log.INFO -> Log.i(tag, toString())
        Log.WARN -> Log.w(tag, toString())
        Log.ERROR -> Log.e(tag, toString())
        else -> Log.d(tag, toString())
    }
}

// ============== String Extensions ==============

/**
 * Checks if the string is null or empty
 */
fun String?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

/**
 * Checks if the string is null or blank
 */
fun String?.isNullOrBlank(): Boolean {
    return this == null || this.isBlank()
}

/**
 * Returns the string if not null, otherwise default
 */
fun String?.orDefault(default: String = ""): String {
    return this ?: default
}

/**
 * Capitalizes the first letter of the string
 */
fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) {
        replaceFirstChar { it.titlecase(Locale.getDefault()) }
    } else {
        this
    }
}

/**
 * Returns the string truncated to maxLength with ellipsis
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (length <= maxLength) {
        this
    } else {
        substring(0, maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Converts the string to a file name safe string
 */
fun String.toSafeFileName(): String {
    return replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .replace(" ", "_")
        .trim()
}

/**
 * Extracts numbers from the string
 */
fun String.extractNumbers(): String {
    return filter { it.isDigit() }
}

/**
 * Checks if the string contains only digits
 */
fun String.isNumeric(): Boolean {
    return all { it.isDigit() }
}

/**
 * Converts the string to a boolean
 */
fun String.toBooleanOrDefault(default: Boolean = false): Boolean {
    return when (lowercase()) {
        "true", "yes", "1", "y" -> true
        "false", "no", "0", "n" -> false
        else -> default
    }
}

/**
 * Converts the string to a URL encoded string
 */
fun String.urlEncode(): String {
    return try {
        java.net.URLEncoder.encode(this, "UTF-8")
    } catch (e: Exception) {
        this
    }
}

/**
 * Converts the string to a URL decoded string
 */
fun String.urlDecode(): String {
    return try {
        java.net.URLDecoder.decode(this, "UTF-8")
    } catch (e: Exception) {
        this
    }
}

/**
 * Converts the string to Base64
 */
fun String.toBase64(): String {
    return try {
        android.util.Base64.encodeToString(toByteArray(), android.util.Base64.DEFAULT)
    } catch (e: Exception) {
        this
    }
}

/**
 * Converts the string from Base64
 */
fun String.fromBase64(): String {
    return try {
        android.util.Base64.decode(this, android.util.Base64.DEFAULT).toString(Charsets.UTF_8)
    } catch (e: Exception) {
        this
    }
}

/**
 * Returns the MD5 hash of the string
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

// ============== Number Extensions ==============

/**
 * Converts number to a file size string
 */
fun Long.toFileSize(context: Context? = null): String {
    return android.text.format.Formatter.formatFileSize(context ?: android.app.Application(), this)
}

/**
 * Converts number to a file size string with custom formatting
 */

/**
 * Converts milliseconds to a time string (HH:MM:SS)
 */
fun Long.toTimeString(): String {
    val seconds = (this / 1000) % 60
    val minutes = (this / (1000 * 60)) % 60
    val hours = (this / (1000 * 60 * 60)) % 24
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Converts milliseconds to a date string
 */
fun Long.toDateString(format: String = "dd/MM/yyyy HH:mm"): String {
    return try {
        SimpleDateFormat(format, Locale.getDefault()).format(Date(this))
    } catch (e: Exception) {
        toString()
    }
}

/**
 * Returns true if the number is between min and max (inclusive)
 */
fun Int.isBetween(min: Int, max: Int): Boolean {
    return this in min..max
}

/**
 * Returns true if the number is between min and max (inclusive)
 */
fun Long.isBetween(min: Long, max: Long): Boolean {
    return this in min..max
}

/**
 * Returns the number clamped between min and max
 */
fun Int.clamp(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Returns the number clamped between min and max
 */
fun Long.clamp(min: Long, max: Long): Long {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Returns the number as a percentage string
 */
fun Int.toPercentage(): String {
    return "${this}%"
}

/**
 * Returns the number as a percentage string with one decimal
 */
fun Float.toPercentage(): String {
    return String.format("%.1f%%", this)
}

// ============== Boolean Extensions ==============

/**
 * Returns "Yes" or "No" based on boolean value
 */
fun Boolean.toYesNo(context: Context): String {
    return if (this) {
        context.getString(android.R.string.yes)
    } else {
        context.getString(android.R.string.no)
    }
}

/**
 * Returns 1 if true, 0 if false
 */
fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

/**
 * Executes the block if true
 */
inline fun Boolean.ifTrue(block: () -> Unit): Boolean {
    if (this) block()
    return this
}

/**
 * Executes the block if false
 */
inline fun Boolean.ifFalse(block: () -> Unit): Boolean {
    if (!this) block()
    return this
}

/**
 * Returns the result of onTrue if true, onFalse if false
 */
inline fun <T> Boolean.ifElse(onTrue: () -> T, onFalse: () -> T): T {
    return if (this) onTrue() else onFalse()
}

// ============== Collection Extensions ==============

/**
 * Checks if the collection is not null and not empty
 */
fun <T> Collection<T>?.isNotNullOrEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

/**
 * Returns the collection if not null and not empty, otherwise null
 */
fun <T> Collection<T>?.nullIfEmpty(): Collection<T>? {
    return if (this.isNullOrEmpty()) null else this
}

/**
 * Groups the collection into chunks of specified size
 */
fun <T> List<T>.chunked(chunkSize: Int): List<List<T>> {
    return (0 until size step chunkSize).map { i ->
        subList(i, (i + chunkSize).coerceAtMost(size))
    }
}

/**
 * Returns distinct elements by a selector
 */
inline fun <T, K> List<T>.distinctBy(selector: (T) -> K): List<T> {
    val set = HashSet<K>()
    val list = ArrayList<T>()
    for (item in this) {
        val key = selector(item)
        if (set.add(key)) {
            list.add(item)
        }
    }
    return list
}

/**
 * Returns the sum of a property
 */
inline fun <T> Collection<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Joins the collection with a separator
 */
fun <T> Collection<T>.joinWith(separator: String = ", "): String {
    return joinToString(separator)
}

/**
 * Returns the median value
 */
fun List<Int>.median(): Double {
    val sorted = sorted()
    return if (size % 2 == 0) {
        (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
    } else {
        sorted[size / 2].toDouble()
    }
}

/**
 * Returns the average value
 */
fun List<Int>.average(): Double {
    return if (isEmpty()) 0.0 else sum().toDouble() / size
}

// ============== Map Extensions ==============

/**
 * Returns the value for the key or default if not found
 */
fun <K, V> Map<K, V>.getOrDefault(key: K, default: V): V {
    return this[key] ?: default
}

/**
 * Returns a new map with keys and values swapped
 */
fun <K, V> Map<K, V>.swap(): Map<V, K> {
    return entries.associate { it.value to it.key }
}

/**
 * Returns a new map with filtered keys
 */
inline fun <K, V> Map<K, V>.filterKeys(predicate: (K) -> Boolean): Map<K, V> {
    val result = LinkedHashMap<K, V>()
    for (entry in this) {
        if (predicate(entry.key)) {
            result[entry.key] = entry.value
        }
    }
    return result
}

/**
 * Returns a new map with filtered values
 */
inline fun <K, V> Map<K, V>.filterValues(predicate: (V) -> Boolean): Map<K, V> {
    val result = LinkedHashMap<K, V>()
    for (entry in this) {
        if (predicate(entry.value)) {
            result[entry.key] = entry.value
        }
    }
    return result
}

// ============== Pair Extensions ==============

/**
 * Swaps the pair
 */
fun <A, B> Pair<A, B>.swap(): Pair<B, A> {
    return Pair(second, first)
}

/**
 * Converts pair to triple with third element
 */
fun <A, B, C> Pair<A, B>.toTriple(third: C): Triple<A, B, C> {
    return Triple(first, second, third)
}

// ============== Triple Extensions ==============

/**
 * Converts triple to pair by dropping first element
 */
fun <A, B, C> Triple<A, B, C>.dropFirst(): Pair<B, C> {
    return Pair(second, third)
}

/**
 * Converts triple to pair by dropping second element
 */
fun <A, B, C> Triple<A, B, C>.dropSecond(): Pair<A, C> {
    return Pair(first, third)
}

/**
 * Converts triple to pair by dropping third element
 */
fun <A, B, C> Triple<A, B, C>.dropThird(): Pair<A, B> {
    return Pair(first, second)
}

// ============== Exception Extensions ==============

/**
 * Returns the stack trace as a string
 */
fun Exception.getStackTraceString(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    printStackTrace(pw)
    return sw.toString()
}

/**
 * Logs the exception
 */
fun Exception.logError(tag: String = "ExoryException") {
    Log.e(tag, message ?: "Unknown error", this)
}

/**
 * Returns the root cause of the exception
 */
fun Exception.rootCause(): Throwable {
    var cause: Throwable = this
    while (cause.cause != null) {
        cause = cause.cause!!
    }
    return cause
}
