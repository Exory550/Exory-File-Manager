package com.exory550.exoryfilemanager.extensions

import android.system.ErrnoException
import android.system.Os
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Extension functions for ZipFile and related classes
 */

/**
 * Get all entries as list
 */
fun ZipFile.entriesAsList(): List<ZipEntry> {
    return entries().toList()
}

/**
 * Get entry by name (case insensitive)
 */
fun ZipFile.getEntryIgnoreCase(name: String): ZipEntry? {
    return entriesAsList().find { it.name.equals(name, ignoreCase = true) }
}

/**
 * Check if zip contains entry
 */
fun ZipFile.containsEntry(name: String): Boolean {
    return getEntry(name) != null
}

/**
 * Get entry count
 */
val ZipFile.entryCount: Int
    get() = size()

/**
 * Get total uncompressed size
 */
val ZipFile.totalUncompressedSize: Long
    get() = entriesAsList().sumOf { it.size }

/**
 * Get total compressed size
 */
val ZipFile.totalCompressedSize: Long
    get() = entriesAsList().sumOf { it.compressedSize }

/**
 * Get compression ratio
 */
val ZipFile.compressionRatio: Double
    get() {
        val uncompressed = totalUncompressedSize.toDouble()
        val compressed = totalCompressedSize.toDouble()
        return if (uncompressed > 0) (compressed / uncompressed) * 100 else 0.0
    }

/**
 * Extract all entries to destination directory
 */
fun ZipFile.extractAll(destinationDir: File, overwrite: Boolean = false): List<File> {
    val extracted = mutableListOf<File>()
    
    entriesAsList().forEach { entry ->
        val targetFile = File(destinationDir, entry.name)
        
        if (entry.isDirectory) {
            targetFile.mkdirs()
        } else {
            targetFile.parentFile?.mkdirs()
            
            if (!targetFile.exists() || overwrite) {
                getInputStream(entry).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile.setLastModified(entry.time)
                extracted.add(targetFile)
            }
        }
    }
    
    return extracted
}

/**
 * Extract specific entries
 */
fun ZipFile.extractEntries(entryNames: List<String>, destinationDir: File, overwrite: Boolean = false): List<File> {
    val extracted = mutableListOf<File>()
    
    entryNames.forEach { name ->
        val entry = getEntry(name)
        if (entry != null) {
            val targetFile = File(destinationDir, entry.name)
            
            if (entry.isDirectory) {
                targetFile.mkdirs()
            } else {
                targetFile.parentFile?.mkdirs()
                
                if (!targetFile.exists() || overwrite) {
                    getInputStream(entry).use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    targetFile.setLastModified(entry.time)
                    extracted.add(targetFile)
                }
            }
        }
    }
    
    return extracted
}

/**
 * Get input stream for entry safely
 */
fun ZipFile.getInputStreamSafely(entry: ZipEntry): InputStream? {
    return try {
        getInputStream(entry)
    } catch (e: Exception) {
        null
    }
}

/**
 * Read entry as text
 */
fun ZipFile.readEntryText(entry: ZipEntry, charset: Charset = Charsets.UTF_8): String? {
    return try {
        getInputStream(entry)?.bufferedReader(charset)?.use { it.readText() }
    } catch (e: Exception) {
        null
    }
}

/**
 * Read entry as bytes
 */
fun ZipFile.readEntryBytes(entry: ZipEntry): ByteArray? {
    return try {
        getInputStream(entry)?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    }
}

/**
 * Check if entry is text file
 */
fun ZipEntry.isTextFile(): Boolean {
    val textExtensions = setOf("txt", "text", "md", "xml", "json", "html", "css", "js", "kt", "java", "properties")
    val extension = name.substringAfterLast(".", "").lowercase()
    return extension in textExtensions
}

/**
 * Check if entry is image
 */
fun ZipEntry.isImage(): Boolean {
    val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg")
    val extension = name.substringAfterLast(".", "").lowercase()
    return extension in imageExtensions
}

/**
 * Get entry size in readable format
 */
fun ZipEntry.readableSize(): String {
    return size.toFormattedFileSize()
}

/**
 * Get entry compressed size in readable format
 */
fun ZipEntry.readableCompressedSize(): String {
    return compressedSize.toFormattedFileSize()
}

/**
 * Get entry compression ratio
 */
fun ZipEntry.compressionRatio(): Double {
    return if (size > 0) (compressedSize.toDouble() / size.toDouble()) * 100 else 0.0
}

/**
 * Extension functions for ZipOutputStream
 */

/**
 * Add file to zip
 */
fun ZipOutputStream.addFile(file: File, entryName: String? = null) {
    val name = entryName ?: file.name
    val entry = ZipEntry(name).apply {
        time = file.lastModified()
        size = file.length()
    }
    
    putNextEntry(entry)
    FileInputStream(file).use { input ->
        input.copyTo(this)
    }
    closeEntry()
}

/**
 * Add directory to zip recursively
 */
fun ZipOutputStream.addDirectory(dir: File, basePath: String = "") {
    dir.listFiles()?.forEach { file ->
        val entryName = if (basePath.isEmpty()) {
            file.name
        } else {
            "$basePath/${file.name}"
        }
        
        if (file.isDirectory) {
            // Add directory entry
            val entry = ZipEntry("$entryName/").apply {
                time = file.lastModified()
            }
            putNextEntry(entry)
            closeEntry()
            
            // Add contents recursively
            addDirectory(file, entryName)
        } else {
            addFile(file, entryName)
        }
    }
}

/**
 * Add multiple files to zip
 */
fun ZipOutputStream.addFiles(files: List<File>, basePath: String = "") {
    files.forEach { file ->
        if (file.isDirectory) {
            addDirectory(file, if (basePath.isEmpty()) file.name else "$basePath/${file.name}")
        } else {
            addFile(file, if (basePath.isEmpty()) file.name else "$basePath/${file.name}")
        }
    }
}

/**
 * Add entry with data
 */
fun ZipOutputStream.addEntry(entryName: String, data: ByteArray) {
    val entry = ZipEntry(entryName).apply {
        time = System.currentTimeMillis()
        size = data.size.toLong()
    }
    
    putNextEntry(entry)
    write(data)
    closeEntry()
}

/**
 * Add entry with text
 */
fun ZipOutputStream.addTextEntry(entryName: String, text: String, charset: Charset = Charsets.UTF_8) {
    addEntry(entryName, text.toByteArray(charset))
}

/**
 * Set compression level
 */
fun ZipOutputStream.setCompressionLevel(level: Int) {
    setLevel(level)
}

/**
 * Set comment
 */
fun ZipOutputStream.setComment(comment: String) {
    setComment(comment)
}

/**
 * Extension functions for ZipInputStream
 */

/**
 * Get next entry safely
 */
fun ZipInputStream.nextEntrySafely(): ZipEntry? {
    return try {
        nextEntry
    } catch (e: Exception) {
        null
    }
}

/**
 * Extract all entries to destination
 */
fun ZipInputStream.extractAll(destinationDir: File, overwrite: Boolean = false): List<File> {
    val extracted = mutableListOf<File>()
    var entry = nextEntry
    
    while (entry != null) {
        val targetFile = File(destinationDir, entry.name)
        
        if (entry.isDirectory) {
            targetFile.mkdirs()
        } else {
            targetFile.parentFile?.mkdirs()
            
            if (!targetFile.exists() || overwrite) {
                FileOutputStream(targetFile).use { output ->
                    copyTo(output)
                }
                targetFile.setLastModified(entry.time)
                extracted.add(targetFile)
            }
        }
        
        entry = nextEntry
    }
    
    return extracted
}

/**
 * Extract specific entries
 */
fun ZipInputStream.extractEntries(entryNames: List<String>, destinationDir: File): List<File> {
    val extracted = mutableListOf<File>()
    var entry = nextEntry
    
    while (entry != null) {
        if (entry.name in entryNames) {
            val targetFile = File(destinationDir, entry.name)
            
            if (entry.isDirectory) {
                targetFile.mkdirs()
            } else {
                targetFile.parentFile?.mkdirs()
                FileOutputStream(targetFile).use { output ->
                    copyTo(output)
                }
                targetFile.setLastModified(entry.time)
                extracted.add(targetFile)
            }
        }
        
        entry = nextEntry
    }
    
    return extracted
}

/**
 * Get list of entries
 */
fun ZipInputStream.entriesAsList(): List<ZipEntry> {
    val entries = mutableListOf<ZipEntry>()
    var entry = nextEntry
    
    while (entry != null) {
        entries.add(entry)
        entry = nextEntry
    }
    
    return entries
}

/**
 * Check if zip contains entry
 */
fun ZipInputStream.containsEntry(entryName: String): Boolean {
    var entry = nextEntry
    while (entry != null) {
        if (entry.name == entryName) {
            return true
        }
        entry = nextEntry
    }
    return false
}

/**
 * Extension functions for File to create zip
 */

/**
 * Create zip file from directory
 */
fun File.zipTo(destFile: File, includeBaseDir: Boolean = true): Boolean {
    return try {
        ZipOutputStream(FileOutputStream(destFile)).use { zipOut ->
            if (isDirectory) {
                if (includeBaseDir) {
                    zipOut.addDirectory(this, name)
                } else {
                    zipOut.addDirectory(this)
                }
            } else {
                zipOut.addFile(this)
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Create zip file from multiple files
 */
fun List<File>.zipTo(destFile: File): Boolean {
    return try {
        ZipOutputStream(FileOutputStream(destFile)).use { zipOut ->
            zipOut.addFiles(this)
        }
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Unzip file to directory
 */
fun File.unzipTo(destDir: File, overwrite: Boolean = false): Boolean {
    return try {
        ZipFile(this).use { zipFile ->
            zipFile.extractAll(destDir, overwrite)
        }
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Get zip entries list
 */
fun File.zipEntries(): List<ZipEntry>? {
    return try {
        ZipFile(this).use { zipFile ->
            zipFile.entriesAsList()
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Check if file is valid zip
 */
val File.isValidZip: Boolean
    get() {
        return try {
            ZipFile(this).use { true }
        } catch (e: Exception) {
            false
        }
    }

/**
 * Get zip info
 */
val File.zipInfo: Map<String, Any>
    get() {
        return try {
            ZipFile(this).use { zipFile ->
                mapOf(
                    "entryCount" to zipFile.entryCount,
                    "totalUncompressedSize" to zipFile.totalUncompressedSize,
                    "totalCompressedSize" to zipFile.totalCompressedSize,
                    "compressionRatio" to zipFile.compressionRatio,
                    "comment" to (zipFile.comment ?: "")
                )
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

/**
 * Extension functions for ZipEntry
 */

/**
 * Get extension
 */
val ZipEntry.extension: String
    get() = name.substringAfterLast(".", "").lowercase()

/**
 * Get name without extension
 */
val ZipEntry.nameWithoutExtension: String
    get() = name.substringBeforeLast(".", name)

/**
 * Get parent path
 */
val ZipEntry.parentPath: String
    get() = name.substringBeforeLast("/", "")

/**
 * Get depth (number of path segments)
 */
val ZipEntry.depth: Int
    get() = name.count { it == '/' }

/**
 * Check if entry is in directory
 */
fun ZipEntry.isInDirectory(directory: String): Boolean {
    return name.startsWith(directory)
}

/**
 * Get relative path from base
 */
fun ZipEntry.relativePath(base: String): String {
    return if (name.startsWith(base)) {
        name.substring(base.length).trimStart('/')
    } else {
        name
    }
}

/**
 * Compare entries by name
 */
fun ZipEntry.compareName(other: ZipEntry): Int {
    return name.compareTo(other.name)
}

/**
 * Compare entries by size
 */
fun ZipEntry.compareSize(other: ZipEntry): Int {
    return size.compareTo(other.size)
}

/**
 * Compare entries by compressed size
 */
fun ZipEntry.compareCompressedSize(other: ZipEntry): Int {
    return compressedSize.compareTo(other.compressedSize)
}

/**
 * Compare entries by time
 */
fun ZipEntry.compareTime(other: ZipEntry): Int {
    return time.compareTo(other.time)
}

/**
 * Extension functions for ZipFile to close safely
 */

/**
 * Use zip file safely
 */
inline fun <T> ZipFile.use(block: (ZipFile) -> T): T {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            close()
        } catch (closeException: Exception) {
            e.addSuppressed(closeException)
        }
        throw e
    } finally {
        if (!closed) {
            close()
        }
    }
}
