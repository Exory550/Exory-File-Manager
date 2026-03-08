package com.exory550.exoryfilemanager.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.exory550.exoryfilemanager.BuildConfig
import java.io.File
import java.util.*

/**
 * Extension functions for List and Collection types
 */

/**
 * Check if collection is not null and not empty
 */
fun <T> Collection<T>?.isNotNullOrEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

/**
 * Return collection if not null and not empty, else null
 */
fun <T> Collection<T>?.nullIfEmpty(): Collection<T>? {
    return if (this.isNullOrEmpty()) null else this
}

/**
 * Return collection if not null and not empty, else default
 */
fun <T> Collection<T>?.orDefault(default: Collection<T>): Collection<T> {
    return if (this.isNullOrEmpty()) default else this
}

/**
 * Split list into chunks of specified size
 */
fun <T> List<T>.chunked(chunkSize: Int): List<List<T>> {
    return (0 until size step chunkSize).map { i ->
        subList(i, (i + chunkSize).coerceAtMost(size))
    }
}

/**
 * Get all combinations of list elements
 */
fun <T> List<T>.combinations(size: Int): List<List<T>> {
    if (size > this.size) return emptyList()
    if (size == 0) return listOf(emptyList())
    if (size == this.size) return listOf(this)
    
    val result = mutableListOf<List<T>>()
    val combination = mutableListOf<T>()
    
    fun combine(start: Int) {
        if (combination.size == size) {
            result.add(combination.toList())
            return
        }
        
        for (i in start until this.size) {
            combination.add(this[i])
            combine(i + 1)
            combination.removeAt(combination.size - 1)
        }
    }
    
    combine(0)
    return result
}

/**
 * Get all permutations of list
 */
fun <T> List<T>.permutations(): List<List<T>> {
    if (size <= 1) return listOf(this)
    
    val result = mutableListOf<List<T>>()
    
    for (i in indices) {
        val element = this[i]
        val remaining = this.filterIndexed { index, _ -> index != i }
        
        remaining.permutations().forEach { perm ->
            result.add(listOf(element) + perm)
        }
    }
    
    return result
}

/**
 * Get random element from list
 */
fun <T> List<T>.randomOrNull(): T? {
    return if (isEmpty()) null else this[Random().nextInt(size)]
}

/**
 * Get random element with fallback
 */
fun <T> List<T>.randomOrDefault(default: T): T {
    return randomOrNull() ?: default
}

/**
 * Get multiple random elements
 */
fun <T> List<T>.random(count: Int, allowDuplicates: Boolean = false): List<T> {
    if (isEmpty()) return emptyList()
    if (count >= size && !allowDuplicates) return this.shuffled()
    
    val random = Random()
    return if (allowDuplicates) {
        (0 until count).map { this[random.nextInt(size)] }
    } else {
        this.shuffled().take(count)
    }
}

/**
 * Shuffle list and return new list
 */
fun <T> List<T>.shuffled(): List<T> {
    return toMutableList().apply { shuffle() }
}

/**
 * Move element from one position to another
 */
fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    val element = removeAt(fromIndex)
    add(toIndex.coerceIn(0, size), element)
}

/**
 * Swap two elements
 */
fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val temp = this[index1]
    this[index1] = this[index2]
    this[index2] = temp
}

/**
 * Replace element at index
 */
fun <T> MutableList<T>.replace(index: Int, element: T): T {
    val old = this[index]
    this[index] = element
    return old
}

/**
 * Add element if not already present
 */
fun <T> MutableList<T>.addIfNotPresent(element: T): Boolean {
    return if (!contains(element)) {
        add(element)
        true
    } else {
        false
    }
}

/**
 * Add all elements if not already present
 */
fun <T> MutableList<T>.addAllIfNotPresent(elements: Collection<T>): Boolean {
    var modified = false
    elements.forEach { element ->
        if (addIfNotPresent(element)) {
            modified = true
        }
    }
    return modified
}

/**
 * Remove duplicates preserving order
 */
fun <T> List<T>.distinctOrdered(): List<T> {
    val set = LinkedHashSet<T>()
    set.addAll(this)
    return set.toList()
}

/**
 * Get duplicates in list
 */
fun <T> List<T>.duplicates(): List<T> {
    val seen = mutableSetOf<T>()
    val duplicates = mutableListOf<T>()
    
    forEach { element ->
        if (!seen.add(element)) {
            duplicates.add(element)
        }
    }
    
    return duplicates.distinct()
}

/**
 * Get frequency of each element
 */
fun <T> List<T>.frequency(): Map<T, Int> {
    val map = mutableMapOf<T, Int>()
    forEach { element ->
        map[element] = map.getOrDefault(element, 0) + 1
    }
    return map
}

/**
 * Get most frequent element
 */
fun <T> List<T>.mostFrequent(): T? {
    return frequency().maxByOrNull { it.value }?.key
}

/**
 * Get least frequent element
 */
fun <T> List<T>.leastFrequent(): T? {
    return frequency().minByOrNull { it.value }?.key
}

/**
 * Group by multiple keys
 */
inline fun <T, K> List<T>.groupByMultiple(vararg selectors: (T) -> K): Map<List<K>, List<T>> {
    return groupBy { element ->
        selectors.map { selector -> selector(element) }
    }
}

/**
 * Partition list into multiple lists based on predicate
 */
inline fun <T> List<T>.partitionMultiple(vararg predicates: (T) -> Boolean): List<List<T>> {
    val results = Array(predicates.size) { mutableListOf<T>() }
    
    forEach { element ->
        predicates.forEachIndexed { index, predicate ->
            if (predicate(element)) {
                results[index].add(element)
            }
        }
    }
    
    return results.map { it.toList() }
}

/**
 * Get all indices where predicate is true
 */
inline fun <T> List<T>.indicesWhere(predicate: (T) -> Boolean): List<Int> {
    return indices.filter { predicate(this[it]) }
}

/**
 * Get random sublist
 */
fun <T> List<T>.randomSubList(size: Int): List<T> {
    if (size >= this.size) return this
    val indices = (0 until this.size).toList().shuffled().take(size)
    return indices.map { this[it] }
}

/**
 * Check if list contains any of the elements
 */
fun <T> List<T>.containsAny(elements: Collection<T>): Boolean {
    return elements.any { contains(it) }
}

/**
 * Check if list contains all of the elements
 */
fun <T> List<T>.containsAll(elements: Collection<T>): Boolean {
    return elements.all { contains(it) }
}

/**
 * Intersection of two lists
 */
infix fun <T> List<T>.intersect(other: List<T>): List<T> {
    return filter { it in other }
}

/**
 * Union of two lists
 */
infix fun <T> List<T>.union(other: List<T>): List<T> {
    return (this + other).distinct()
}

/**
 * Difference of two lists
 */
infix fun <T> List<T>.minus(other: List<T>): List<T> {
    return filter { it !in other }
}

/**
 * Symmetric difference of two lists
 */
infix fun <T> List<T>.xor(other: List<T>): List<T> {
    return (this - other) + (other - this)
}

/**
 * Zip with next element
 */
fun <T> List<T>.zipWithNext(): List<Pair<T, T>> {
    if (size < 2) return emptyList()
    return (0 until size - 1).map { Pair(this[it], this[it + 1]) }
}

/**
 * Zip with previous element
 */
fun <T> List<T>.zipWithPrevious(): List<Pair<T, T>> {
    if (size < 2) return emptyList()
    return (1 until size).map { Pair(this[it - 1], this[it]) }
}

/**
 * Get element at index or null
 */
fun <T> List<T>.getOrNull(index: Int): T? {
    return if (index in indices) this[index] else null
}

/**
 * Get element at index or default
 */
fun <T> List<T>.getOrDefault(index: Int, default: T): T {
    return getOrNull(index) ?: default
}

/**
 * Find element or return null
 */
inline fun <T> List<T>.findOrNull(predicate: (T) -> Boolean): T? {
    return firstOrNull(predicate)
}

/**
 * Find last element or return null
 */
inline fun <T> List<T>.findLastOrNull(predicate: (T) -> Boolean): T? {
    return lastOrNull(predicate)
}

/**
 * Sum of property
 */
inline fun <T> List<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    forEach { sum += selector(it) }
    return sum
}

/**
 * Average of property
 */
inline fun <T> List<T>.averageByDouble(selector: (T) -> Double): Double {
    if (isEmpty()) return 0.0
    var sum = 0.0
    forEach { sum += selector(it) }
    return sum / size
}

/**
 * Min of property
 */
inline fun <T> List<T>.minByDouble(selector: (T) -> Double): Double {
    if (isEmpty()) return 0.0
    var min = Double.MAX_VALUE
    forEach { min = minOf(min, selector(it)) }
    return min
}

/**
 * Max of property
 */
inline fun <T> List<T>.maxByDouble(selector: (T) -> Double): Double {
    if (isEmpty()) return 0.0
    var max = Double.MIN_VALUE
    forEach { max = maxOf(max, selector(it)) }
    return max
}

/**
 * Join elements with separator
 */
fun <T> List<T>.joinWith(separator: String = ", "): String {
    return joinToString(separator)
}

/**
 * Join elements with natural language (e.g., "a, b and c")
 */
fun <T> List<T>.joinNaturalLanguage(): String {
    return when (size) {
        0 -> ""
        1 -> "${this[0]}"
        2 -> "${this[0]} and ${this[1]}"
        else -> {
            val allButLast = take(size - 1).joinToString(", ")
            "$allButLast, and ${last()}"
        }
    }
}

/**
 * Create a deep copy of list
 */
fun <T> List<T>.deepCopy(): List<T> where T : Any {
    return map { it }
}

/**
 * Convert list to map with index as key
 */
fun <T> List<T>.indexed(): Map<Int, T> {
    return withIndex().associate { it.index to it.value }
}

/**
 * Batch process list
 */
inline fun <T, R> List<T>.batchProcess(batchSize: Int, processor: (List<T>) -> R): List<R> {
    return chunked(batchSize).map { processor(it) }
}

/**
 * Parallel batch process
 */
inline fun <T, R> List<T>.parallelBatchProcess(batchSize: Int, crossinline processor: (List<T>) -> R): List<R> {
    return chunked(batchSize)
        .map { batch ->
            kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.async { processor(batch) }
            }
        }
        .map { it.await() }
}

/**
 * Extension functions for List of Files
 */

/**
 * Share multiple files
 */
fun List<File>.share(context: Context): Boolean {
    if (isEmpty()) return false
    
    return try {
        val uris = map { file ->
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        }
        
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share files"))
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Delete all files
 */
fun List<File>.deleteAll(): Boolean {
    return all { it.deleteRecursivelySafely() }
}

/**
 * Get total size of all files
 */
val List<File>.totalSize: Long
    get() = sumOf { it.totalSize }

/**
 * Get total file count
 */
val List<File>.totalFileCount: Int
    get() = sumOf { it.fileCount }

/**
 * Get total directory count
 */
val List<File>.totalDirectoryCount: Int
    get() = sumOf { it.directoryCount }

/**
 * Filter files by type
 */
fun List<File>.filterByExtension(vararg extensions: String): List<File> {
    return filter { file ->
        extensions.any { file.extension.equals(it, ignoreCase = true) }
    }
}

/**
 * Sort files by name
 */
fun List<File>.sortByName(ascending: Boolean = true): List<File> {
    return if (ascending) {
        sortedBy { it.name.lowercase() }
    } else {
        sortedByDescending { it.name.lowercase() }
    }
}

/**
 * Sort files by date
 */
fun List<File>.sortByDate(ascending: Boolean = true): List<File> {
    return if (ascending) {
        sortedBy { it.lastModified() }
    } else {
        sortedByDescending { it.lastModified() }
    }
}

/**
 * Sort files by size
 */
fun List<File>.sortBySize(ascending: Boolean = true): List<File> {
    return if (ascending) {
        sortedBy { it.length() }
    } else {
        sortedByDescending { it.length() }
    }
}

/**
 * Get directories only
 */
val List<File>.directories: List<File>
    get() = filter { it.isDirectory }

/**
 * Get files only
 */
val List<File>.filesOnly: List<File>
    get() = filter { it.isFile }

/**
 * Get hidden files
 */
val List<File>.hidden: List<File>
    get() = filter { it.isHiddenFile }
