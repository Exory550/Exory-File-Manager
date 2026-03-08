package com.exory550.exoryfilemanager.extensions

import android.database.Cursor
import android.database.CursorWrapper
import java.io.Closeable

/**
 * Extension functions for Cursor
 */

/**
 * Safely close cursor
 */
fun Cursor?.safeClose() {
    try {
        this?.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Execute block with cursor and close it automatically
 */
inline fun <T> Cursor?.use(block: (Cursor) -> T): T? {
    return try {
        this?.let { cursor ->
            try {
                block(cursor)
            } finally {
                cursor.safeClose()
            }
        }
    } catch (e: Exception) {
        this?.safeClose()
        null
    }
}

/**
 * Check if cursor is not null and not closed
 */
val Cursor?.isValid: Boolean
    get() = this != null && !this.isClosed

/**
 * Check if cursor has data
 */
val Cursor?.hasData: Boolean
    get() = this.isValid && this.count > 0

/**
 * Get string column value safely
 */
fun Cursor.getStringOrNull(columnName: String): String? {
    return try {
        val index = getColumnIndex(columnName)
        if (index >= 0) getString(index) else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Get string column value with default
 */
fun Cursor.getStringOrDefault(columnName: String, default: String = ""): String {
    return getStringOrNull(columnName) ?: default
}

/**
 * Get int column value safely
 */
fun Cursor.getIntOrNull(columnName: String): Int? {
    return try {
        val index = getColumnIndex(columnName)
        if (index >= 0) getInt(index) else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Get int column value with default
 */
fun Cursor.getIntOrDefault(columnName: String, default: Int = 0): Int {
    return getIntOrNull(columnName) ?: default
}

/**
 * Get long column value safely
 */
fun Cursor.getLongOrNull(columnName: String): Long? {
    return try {
        val index = getColumnIndex(columnName)
        if (index >= 0) getLong(index) else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Get long column value with default
 */
fun Cursor.getLongOrDefault(columnName: String, default: Long = 0L): Long {
    return getLongOrNull(columnName) ?: default
}

/**
 * Get float column value safely
 */
fun Cursor.getFloatOrNull(columnName: String): Float? {
    return try {
        val index = getColumnIndex(columnName)
        if (index >= 0) getFloat(index) else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Get float column value with default
 */
fun Cursor.getFloatOrDefault(columnName: String, default: Float = 0f): Float {
    return getFloatOrNull(columnName) ?: default
}

/**
 * Get double column value safely
 */
fun Cursor.getDoubleOrNull(columnName: String): Double? {
    return try {
        val index = getColumnIndex(columnName)
        if (index >= 0) getDouble(index) else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Get double column value with default
 */
fun Cursor.getDoubleOrDefault(columnName: String, default: Double = 0.0): Double {
    return getDoubleOrNull(columnName) ?: default
}

/**
 * Get boolean column value safely
 */
fun Cursor.getBooleanOrNull(columnName: String): Boolean? {
    return try {
        val index = getColumnIndex(columnName)
        if (index >= 0) getInt(index) == 1 else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Get boolean column value with default
 */
fun Cursor.getBooleanOrDefault(columnName: String, default: Boolean = false): Boolean {
    return getBooleanOrNull(columnName) ?: default
}

/**
 * Get blob column value safely
 */
fun Cursor.getBlobOrNull(columnName: String): ByteArray? {
    return try {
        val index = getColumnIndex(columnName)
        if (index >= 0) getBlob(index) else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Check if column exists
 */
fun Cursor.hasColumn(columnName: String): Boolean {
    return try {
        getColumnIndex(columnName) >= 0
    } catch (e: Exception) {
        false
    }
}

/**
 * Get column names as list
 */
val Cursor.columnNamesList: List<String>
    get() = columnNames?.toList() ?: emptyList()

/**
 * Get current row as map
 */
val Cursor.currentRowAsMap: Map<String, Any?>
    get() {
        val map = mutableMapOf<String, Any?>()
        columnNames?.forEach { columnName ->
            map[columnName] = getValue(columnName)
        }
        return map
    }

/**
 * Get value by column name with appropriate type
 */
fun Cursor.getValue(columnName: String): Any? {
    val index = getColumnIndex(columnName)
    if (index < 0) return null
    
    return when (getType(index)) {
        Cursor.FIELD_TYPE_NULL -> null
        Cursor.FIELD_TYPE_INTEGER -> getLong(index)
        Cursor.FIELD_TYPE_FLOAT -> getDouble(index)
        Cursor.FIELD_TYPE_STRING -> getString(index)
        Cursor.FIELD_TYPE_BLOB -> getBlob(index)
        else -> null
    }
}

/**
 * Move to position and return cursor
 */
fun Cursor.moveToPosition(position: Int): Cursor {
    moveToPosition(position)
    return this
}

/**
 * Move to first and return cursor
 */
fun Cursor.moveToFirst(): Cursor {
    moveToFirst()
    return this
}

/**
 * Move to last and return cursor
 */
fun Cursor.moveToLast(): Cursor {
    moveToLast()
    return this
}

/**
 * Move to next and return cursor
 */
fun Cursor.moveToNext(): Cursor {
    moveToNext()
    return this
}

/**
 * Move to previous and return cursor
 */
fun Cursor.moveToPrevious(): Cursor {
    moveToPrevious()
    return this
}

/**
 * For each row in cursor
 */
inline fun Cursor.forEach(action: (Cursor) -> Unit) {
    try {
        if (moveToFirst()) {
            do {
                action(this)
            } while (moveToNext())
        }
    } finally {
        safeClose()
    }
}

/**
 * Map cursor rows to list
 */
inline fun <T> Cursor.mapToList(transform: (Cursor) -> T): List<T> {
    val list = mutableListOf<T>()
    forEach { cursor ->
        list.add(transform(cursor))
    }
    return list
}

/**
 * Get first row or null
 */
inline fun <T> Cursor.firstOrNull(transform: (Cursor) -> T): T? {
    return try {
        if (moveToFirst()) {
            transform(this)
        } else {
            null
        }
    } finally {
        safeClose()
    }
}

/**
 * Get single row or null
 */
inline fun <T> Cursor.singleOrNull(transform: (Cursor) -> T): T? {
    return try {
        if (count == 1 && moveToFirst()) {
            transform(this)
        } else {
            null
        }
    } finally {
        safeClose()
    }
}

/**
 * Check if cursor has a specific value
 */
fun Cursor.hasValue(columnName: String, value: String): Boolean {
    forEach { cursor ->
        if (cursor.getStringOrDefault(columnName) == value) {
            return true
        }
    }
    return false
}

/**
 * Count rows matching condition
 */
inline fun Cursor.count(condition: (Cursor) -> Boolean): Int {
    var count = 0
    forEach { cursor ->
        if (condition(cursor)) {
            count++
        }
    }
    return count
}

/**
 * Dump cursor content to string
 */
fun Cursor.dump(): String {
    val sb = StringBuilder()
    
    sb.appendLine("Cursor Dump (${count} rows)")
    sb.appendLine("Columns: ${columnNamesList.joinToString(", ")}")
    sb.appendLine("-".repeat(50))
    
    forEach { cursor ->
        columnNamesList.forEach { column ->
            sb.appendLine("$column: ${cursor.getValue(column)}")
        }
        sb.appendLine("-".repeat(30))
    }
    
    return sb.toString()
}

/**
 * Log cursor content
 */
fun Cursor.log(tag: String = "Cursor") {
    android.util.Log.d(tag, dump())
}

/**
 * Cursor wrapper with additional functionality
 */
open class ExoryCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {
    
    fun getString(columnName: String): String {
        return getString(getColumnIndexOrThrow(columnName))
    }
    
    fun getStringOrNull(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }
    
    fun getInt(columnName: String): Int {
        return getInt(getColumnIndexOrThrow(columnName))
    }
    
    fun getIntOrNull(columnName: String): Int? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getInt(index) else null
    }
    
    fun getLong(columnName: String): Long {
        return getLong(getColumnIndexOrThrow(columnName))
    }
    
    fun getLongOrNull(columnName: String): Long? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }
    
    fun getBoolean(columnName: String): Boolean {
        return getInt(columnName) == 1
    }
    
    fun getBooleanOrNull(columnName: String): Boolean? {
        val value = getIntOrNull(columnName)
        return value?.let { it == 1 }
    }
}

/**
 * Extension function to create wrapped cursor
 */
fun Cursor.wrap(): ExoryCursorWrapper {
    return ExoryCursorWrapper(this)
}

/**
 * Batch cursor operations
 */
class BatchCursorOperations(private val cursor: Cursor) : Closeable {
    
    private var position = -1
    
    fun processBatch(batchSize: Int = 100, processor: (Cursor) -> Unit) {
        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    processor(c)
                    position++
                    
                    if (position % batchSize == 0) {
                        // Yield to other operations
                        Thread.yield()
                    }
                } while (c.moveToNext())
            }
        }
    }
    
    override fun close() {
        cursor.safeClose()
    }
}

fun Cursor.batch(): BatchCursorOperations {
    return BatchCursorOperations(this)
}

/**
 * Get column index safely
 */
fun Cursor.getColumnIndexSafely(columnName: String): Int {
    return try {
        getColumnIndex(columnName)
    } catch (e: Exception) {
        -1
    }
}

/**
 * Check if cursor is at valid position
 */
val Cursor.isValidPosition: Boolean
    get() = isValid && position >= 0 && position < count

/**
 * Get current position safely
 */
val Cursor.safePosition: Int
    get() = if (isValid) position else -1

/**
 * Move to absolute position safely
 */
fun Cursor.safeMoveToPosition(position: Int): Boolean {
    return try {
        if (position in 0 until count) {
            moveToPosition(position)
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

/**
 * Get next row or null
 */
fun Cursor.nextOrNull(): Cursor? {
    return if (moveToNext()) this else null
}

/**
 * Get previous row or null
 */
fun Cursor.previousOrNull(): Cursor? {
    return if (moveToPrevious()) this else null
}

/**
 * Get first row or null
 */
fun Cursor.firstOrNull(): Cursor? {
    return if (moveToFirst()) this else null
}

/**
 * Get last row or null
 */
fun Cursor.lastOrNull(): Cursor? {
    return if (moveToLast()) this else null
}
