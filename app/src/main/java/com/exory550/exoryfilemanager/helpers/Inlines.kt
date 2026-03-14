package com.exory550.exoryfilemanager.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.exory550.exoryfilemanager.R

inline fun <reified T : Activity> Context.startActivity(
    block: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    block(intent)
    startActivity(intent)
}

inline fun <reified T : Activity> Fragment.startActivity(
    block: Intent.() -> Unit = {}
) {
    val intent = Intent(requireContext(), T::class.java)
    block(intent)
    startActivity(intent)
}

inline fun <reified T : Activity> Context.startActivityForResult(
    requestCode: Int,
    block: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    block(intent)
    (this as? Activity)?.startActivityForResult(intent, requestCode)
}

inline fun <reified T : Any> Intent.getParcelable(key: String): T? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra<android.os.Parcelable>(key) as? T
    }
}

inline fun <reified T : Any> Intent.getParcelableArrayList(key: String): ArrayList<T>? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayListExtra(key)
    }
}

inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    for (i in indices) {
        action(this[i])
    }
}

inline fun <T> List<T>.fastForEachIndexed(action: (index: Int, T) -> Unit) {
    for (i in indices) {
        action(i, this[i])
    }
}

inline fun <T, R> List<T>.fastMap(transform: (T) -> R): List<R> {
    val result = ArrayList<R>(size)
    for (i in indices) {
        result.add(transform(this[i]))
    }
    return result
}

inline fun <T> List<T>.fastFilter(predicate: (T) -> Boolean): List<T> {
    val result = ArrayList<T>()
    for (i in indices) {
        val item = this[i]
        if (predicate(item)) {
            result.add(item)
        }
    }
    return result
}

inline fun <T, K> List<T>.fastGroupBy(keySelector: (T) -> K): Map<K, List<T>> {
    val map = LinkedHashMap<K, MutableList<T>>()
    for (i in indices) {
        val item = this[i]
        val key = keySelector(item)
        map.getOrPut(key) { ArrayList() }.add(item)
    }
    return map
}

inline fun <T> List<T>.fastSumBy(selector: (T) -> Int): Int {
    var sum = 0
    for (i in indices) {
        sum += selector(this[i])
    }
    return sum
}

inline fun <T> List<T>.fastSumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (i in indices) {
        sum += selector(this[i])
    }
    return sum
}

inline fun <T> Array<T>.fastForEach(action: (T) -> Unit) {
    for (i in indices) {
        action(this[i])
    }
}

inline fun Boolean.ifTrue(block: () -> Unit): Boolean {
    if (this) block()
    return this
}

inline fun Boolean.ifFalse(block: () -> Unit): Boolean {
    if (!this) block()
    return this
}

inline fun <T> T.alsoIf(condition: Boolean, block: (T) -> Unit): T {
    if (condition) block(this)
    return this
}

inline fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
    if (condition) block()
    return this
}

inline fun <T : Any> T?.orElse(block: () -> T): T {
    return this ?: block()
}

inline fun <reified T : Enum<T>> String.toEnumOr(default: T): T {
    return try {
        enumValueOf<T>(this)
    } catch (e: Exception) {
        default
    }
}

inline fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

inline fun Context.toast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageRes, duration).show()
}

inline fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(message, duration)
}

inline fun Fragment.toast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().toast(messageRes, duration)
}

fun <T> lazyFast(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <T> measureTimeMillis(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val time = System.currentTimeMillis() - start
    return result to time
}

inline fun <T> measureNanoTime(block: () -> T): Pair<T, Long> {
    val start = System.nanoTime()
    val result = block()
    val time = System.nanoTime() - start
    return result to time
}

inline fun <R> runCatchingLog(block: () -> R): R? {
    return try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun <T, R> Iterable<T>.parallelMap(transform: (T) -> R): List<R> {
    return map { transform(it) }
}

inline fun repeatTimes(times: Int, action: (Int) -> Unit) {
    for (i in 0 until times) {
        action(i)
    }
}
