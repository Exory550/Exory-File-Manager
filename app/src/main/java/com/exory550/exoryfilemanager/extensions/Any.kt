package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.util.Log
import com.exory550.exoryfilemanager.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.PrintWriter
import java.io.StringWriter
import java.security.MessageDigest

fun Any.toJson(): String {
    return try {
        GsonBuilder().setPrettyPrinting().create().toJson(this)
    } catch (e: Exception) { "{}" }
}

fun Any.toCompactJson(): String {
    return try {
        Gson().toJson(this)
    } catch (e: Exception) { "{}" }
}

inline fun <reified T> T.deepCopy(): T {
    return try {
        val json = Gson().toJson(this)
        Gson().fromJson(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) { this }
}

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
    } catch (e: Exception) { "Unknown" }
}

fun Any.hashHex(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toString().toByteArray())
        digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) { Integer.toHexString(hashCode()) }
}

fun Any.sha256(): String {
    return try {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(toString().toByteArray())
        digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) { "" }
}

fun Any.logDebug(tag: String = "ExoryDebug") {
    if (BuildConfig.DEBUG) Log.d(tag, toString())
}

fun Any.logError(tag: String = "ExoryError") {
    Log.e(tag, toString())
}

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

fun Long.toFileSize(context: Context? = null): String {
    return android.text.format.Formatter.formatFileSize(context ?: android.app.Application(), this)
}

fun Int.toPercentage(): String = "${this}%"
fun Float.toPercentage(): String = String.format("%.1f%%", this)

fun Boolean.toYesNo(context: Context): String =
    if (this) context.getString(android.R.string.yes)
    else context.getString(android.R.string.no)

fun Boolean.toInt(): Int = if (this) 1 else 0

inline fun Boolean.ifTrue(block: () -> Unit): Boolean { if (this) block(); return this }
inline fun Boolean.ifFalse(block: () -> Unit): Boolean { if (!this) block(); return this }
inline fun <T> Boolean.ifElse(onTrue: () -> T, onFalse: () -> T): T = if (this) onTrue() else onFalse()

fun <T> Collection<T>?.isNotNullOrEmpty(): Boolean = this != null && this.isNotEmpty()
fun <T> Collection<T>?.nullIfEmpty(): Collection<T>? = if (this.isNullOrEmpty()) null else this

inline fun <T> List<T>.fastSumBy(selector: (T) -> Int): Int {
    var sum = 0
    for (i in indices) sum += selector(this[i])
    return sum
}

fun <T> Collection<T>.joinWith(separator: String = ", "): String = joinToString(separator)

fun List<Int>.median(): Double {
    val sorted = sorted()
    return if (size % 2 == 0) (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
    else sorted[size / 2].toDouble()
}

fun <K, V> Map<K, V>.swap(): Map<V, K> = entries.associate { it.value to it.key }

fun <A, B> Pair<A, B>.swap(): Pair<B, A> = Pair(second, first)
fun <A, B, C> Pair<A, B>.toTriple(third: C): Triple<A, B, C> = Triple(first, second, third)
fun <A, B, C> Triple<A, B, C>.dropFirst(): Pair<B, C> = Pair(second, third)
fun <A, B, C> Triple<A, B, C>.dropSecond(): Pair<A, C> = Pair(first, third)
fun <A, B, C> Triple<A, B, C>.dropThird(): Pair<A, B> = Pair(first, second)

fun Exception.getStackTraceString(): String {
    val sw = StringWriter()
    printStackTrace(PrintWriter(sw))
    return sw.toString()
}

fun Exception.rootCause(): Throwable {
    var cause: Throwable = this
    while (cause.cause != null) cause = cause.cause!!
    return cause
}
