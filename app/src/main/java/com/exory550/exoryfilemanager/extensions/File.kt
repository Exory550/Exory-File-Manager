package com.exory550.exoryfilemanager.extensions

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.exory550.exoryfilemanager.BuildConfig
import java.io.*
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions for File class
 */

/**
 * Get file name without extension
 */
val File.nameWithoutExtension: String
    get() = name.substringBeforeLast(".", "")

/**
 * Get file extension
 */
val File.extension: String
    get() = name.substringAfterLast(".", "").lowercase(Locale.US)

/**
 * Get file extension with dot
 */
val File.extensionWithDot: String
    get() = if (extension.isNotEmpty()) ".$extension" else ""

/**
 * Get file path without file name
 */
val File.parentPath: String
    get() = parent ?: ""

/**
 * Get file size in readable format
 */
val File.readableSize: String
    get() = length().toFormattedFileSize()

/**
 * Get file last modified date in readable format
 */
val File.readableLastModified: String
    get() = lastModified().toDateString()

/**
 * Check if file is hidden (starts with dot)
 */
val File.isHiddenFile: Boolean
    get() = name.startsWith('.')

/**
 * Check if file is a media file
 */
val File.isMediaFile: Boolean
    get() = isImage || isVideo || isAudio

/**
 * Check if file is an image
 */
val File.isImage: Boolean
    get() {
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "raw", "cr2", "nef", "arw")
        return extension in imageExtensions
    }

/**
 * Check if file is a video
 */
val File.isVideo: Boolean
    get() {
        val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg")
        return extension in videoExtensions
    }

/**
 * Check if file is an audio
 */
val File.isAudio: Boolean
    get() {
        val audioExtensions = setOf("mp3", "wav", "ogg", "m4a", "aac", "flac", "wma", "ape", "ac3", "dts")
        return extension in audioExtensions
    }

/**
 * Check if file is a document
 */
val File.isDocument: Boolean
    get() {
        val documentExtensions = setOf("txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf")
        return extension in documentExtensions
    }

/**
 * Check if file is an archive
 */
val File.isArchive: Boolean
    get() {
        val archiveExtensions = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "z", "arj", "cab")
        return extension in archiveExtensions
    }

/**
 * Check if file is an APK
 */
val File.isApk: Boolean
    get() = extension == "apk"

/**
 * Check if file is a text file
 */
val File.isTextFile: Boolean
    get() {
        val textExtensions = setOf("txt", "text", "md", "markdown", "ini", "cfg", "conf", "log", "csv", "tsv")
        return extension in textExtensions
    }

/**
 * Check if file is a code file
 */
val File.isCodeFile: Boolean
    get() {
        val codeExtensions = setOf(
            "java", "kt", "kts", "xml", "json", "yml", "yaml", "properties", "gradle",
            "c", "cpp", "h", "hpp", "cs", "php", "js", "ts", "py", "rb", "go", "rs", "swift"
        )
        return extension in codeExtensions
    }

/**
 * Get MIME type of file
 */
val File.mimeType: String
    get() {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mimeType ?: when {
            isDirectory -> "resource/folder"
            isApk -> "application/vnd.android.package-archive"
            isTextFile -> "text/plain"
            else -> "*/*"
        }
    }

/**
 * Get file URI using FileProvider
 */
fun File.getUri(context: Context): Uri {
    return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", this)
}

/**
 * Get content URI for MediaStore
 */
fun File.getMediaStoreUri(context: Context): Uri? {
    val collection = when {
        isImage -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        isVideo -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        isAudio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        else -> MediaStore.Files.getContentUri("external")
    }
    
    val projection = arrayOf(MediaStore.MediaColumns._ID)
    val selection = MediaStore.MediaColumns.DATA + " = ?"
    val selectionArgs = arrayOf(absolutePath)
    
    context.contentResolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            return Uri.withAppendedPath(collection, id.toString())
        }
    }
    
    return null
}

/**
 * Create file if not exists
 */
fun File.createIfNotExists(): Boolean {
    return if (exists()) {
        true
    } else {
        try {
            parentFile?.mkdirs()
            createNewFile()
        } catch (e: IOException) {
            false
        }
    }
}

/**
 * Create directory if not exists
 */
fun File.createDirIfNotExists(): Boolean {
    return if (exists()) {
        isDirectory
    } else {
        mkdirs()
    }
}

/**
 * Delete directory recursively
 */
fun File.deleteRecursivelySafely(): Boolean {
    return if (isDirectory) {
        listFiles()?.all { it.deleteRecursivelySafely() } == true && delete()
    } else {
        delete()
    }
}

/**
 * Copy file to destination
 */
fun File.copyTo(dest: File, overwrite: Boolean = false): Boolean {
    return try {
        if (dest.exists() && !overwrite) {
            return false
        }
        
        if (isDirectory) {
            dest.mkdirs()
            listFiles()?.forEach { file ->
                file.copyTo(File(dest, file.name), overwrite)
            }
            true
        } else {
            dest.parentFile?.mkdirs()
            inputStream().use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            dest.setLastModified(lastModified())
            true
        }
    } catch (e: IOException) {
        false
    }
}

/**
 * Move file to destination
 */
fun File.moveTo(dest: File, overwrite: Boolean = false): Boolean {
    return try {
        if (dest.exists() && !overwrite) {
            return false
        }
        
        if (renameTo(dest)) {
            true
        } else {
            // Fallback to copy and delete
            val copied = copyTo(dest, overwrite)
            if (copied) {
                deleteRecursivelySafely()
            }
            copied
        }
    } catch (e: Exception) {
        false
    }
}

/**
 * Get file checksum (MD5)
 */
fun File.md5(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192)
        
        inputStream().use { fis ->
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        
        md.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}

/**
 * Get file checksum (SHA-1)
 */
fun File.sha1(): String {
    return try {
        val md = MessageDigest.getInstance("SHA-1")
        val buffer = ByteArray(8192)
        
        inputStream().use { fis ->
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        
        md.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}

/**
 * Get file checksum (SHA-256)
 */
fun File.sha256(): String {
    return try {
        val md = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        
        inputStream().use { fis ->
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        
        md.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        ""
    }
}

/**
 * Get file size in bytes
 */
val File.totalSize: Long
    get() = if (isDirectory) {
        walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    } else {
        length()
    }

/**
 * Get file count
 */
val File.fileCount: Int
    get() = if (isDirectory) {
        walkTopDown().filter { it.isFile }.count()
    } else {
        1
    }

/**
 * Get directory count
 */
val File.directoryCount: Int
    get() = if (isDirectory) {
        walkTopDown().filter { it.isDirectory }.count() - 1
    } else {
        0
    }

/**
 * Get total item count (files and directories)
 */
val File.totalItemCount: Int
    get() = fileCount + directoryCount + (if (isDirectory) 1 else 0)

/**
 * Get human-readable file info
 */
val File.info: String
    get() = """
        Name: $name
        Path: $absolutePath
        Size: $readableSize
        Modified: $readableLastModified
        Type: ${if (isDirectory) "Directory" else "File"}
        Hidden: $isHiddenFile
        Readable: $canRead()
        Writable: $canWrite()
        Executable: $canExecute()
        MIME: $mimeType
    """.trimIndent()

/**
 * Open file with default app
 */
fun File.open(context: Context): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(getUri(context), mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Share file
 */
fun File.share(context: Context): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, getUri(context))
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share $name"))
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Get list of files with filter
 */
fun File.listFiles(filter: (File) -> Boolean): List<File> {
    return listFiles()?.filter(filter) ?: emptyList()
}

/**
 * Get list of directories
 */
val File.listDirectories: List<File>
    get() = listFiles { it.isDirectory }

/**
 * Get list of files (non-directories)
 */
val File.listFilesOnly: List<File>
    get() = listFiles { it.isFile }

/**
 * Get list of files by extension
 */
fun File.listFilesByExtension(vararg extensions: String): List<File> {
    return listFiles { file ->
        file.isFile && extensions.any { file.extension.equals(it, ignoreCase = true) }
    }
}

/**
 * Find files recursively
 */
fun File.findFilesRecursively(filter: (File) -> Boolean): List<File> {
    val result = mutableListOf<File>()
    
    fun findRecursive(file: File) {
        if (filter(file)) {
            result.add(file)
        }
        if (file.isDirectory) {
            file.listFiles()?.forEach { findRecursive(it) }
        }
    }
    
    findRecursive(this)
    return result
}

/**
 * Get relative path from base
 */
fun File.getRelativePath(base: File): String {
    return toRelativeString(base)
}

/**
 * Check if file is child of parent
 */
fun File.isChildOf(parent: File): Boolean {
    return absolutePath.startsWith(parent.absolutePath + File.separator)
}

/**
 * Get the root storage of this file
 */
val File.storageRoot: File
    get() {
        var current = this
        while (current.parentFile != null) {
            current = current.parentFile!!
        }
        return current
    }

/**
 * Get free space on the file system containing this file
 */
val File.freeSpace: Long
    get() {
        return try {
            StatFs(absolutePath).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    availableBlocksLong * blockSizeLong
                } else {
                    @Suppress("DEPRECATION")
                    availableBlocks.toLong() * blockSize.toLong()
                }
            }
        } catch (e: Exception) {
            0
        }
    }

/**
 * Get total space on the file system containing this file
 */
val File.totalSpace: Long
    get() {
        return try {
            StatFs(absolutePath).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    blockCountLong * blockSizeLong
                } else {
                    @Suppress("DEPRECATION")
                    blockCount.toLong() * blockSize.toLong()
                }
            }
        } catch (e: Exception) {
            0
        }
    }

/**
 * Get used space on the file system containing this file
 */
val File.usedSpace: Long
    get() = totalSpace - freeSpace

/**
 * Get usage percentage
 */
val File.usagePercentage: Int
    get() = if (totalSpace > 0) {
        ((totalSpace - freeSpace) * 100 / totalSpace).toInt()
    } else {
        0
    }

/**
 * Ensure file has the correct extension
 */
fun File.ensureExtension(desiredExtension: String): File {
    if (extension.equals(desiredExtension, ignoreCase = true)) {
        return this
    }
    return File(parent, "$nameWithoutExtension.$desiredExtension")
}

/**
 * Create a unique file name in the same directory
 */
fun File.createUnique(): File {
    if (!exists()) return this
    
    var counter = 1
    var newFile: File
    
    do {
        newFile = File(parent, "$nameWithoutExtension ($counter).$extension")
        counter++
    } while (newFile.exists())
    
    return newFile
}

/**
 * Touch file (update last modified timestamp)
 */
fun File.touch(): Boolean {
    return try {
        setLastModified(System.currentTimeMillis())
    } catch (e: Exception) {
        false
    }
}

/**
 * Read file as text
 */
fun File.readText(): String {
    return readText(Charsets.UTF_8)
}

/**
 * Read file as text with specific charset
 */
fun File.readText(charset: Charset): String {
    return bufferedReader(charset).use { it.readText() }
}

/**
 * Read file as lines
 */
fun File.readLines(): List<String> {
    return readLines(Charsets.UTF_8)
}

/**
 * Read file as lines with specific charset
 */
fun File.readLines(charset: Charset): List<String> {
    return bufferedReader(charset).use { it.readLines() }
}

/**
 * Write text to file
 */
fun File.writeText(text: String, append: Boolean = false) {
    if (append) {
        appendText(text)
    } else {
        bufferedWriter().use { it.write(text) }
    }
}

/**
 * Write lines to file
 */
fun File.writeLines(lines: List<String>, append: Boolean = false) {
    if (append) {
        appendText(lines.joinToString("\n"))
    } else {
        bufferedWriter().use { writer ->
            lines.forEachIndexed { index, line ->
                writer.write(line)
                if (index < lines.size - 1) {
                    writer.newLine()
                }
            }
        }
    }
}

/**
 * Append line to file
 */
fun File.appendLine(line: String) {
    appendText("$line\n")
}

/**
 * Get input stream safely
 */
fun File.inputStreamSafely(): InputStream? {
    return try {
        if (exists() && canRead()) {
            inputStream()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Get output stream safely
 */
fun File.outputStreamSafely(append: Boolean = false): OutputStream? {
    return try {
        if (exists() && !canWrite()) {
            null
        } else {
            parentFile?.mkdirs()
            outputStream()
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Compare two files by content
 */
fun File.contentEquals(other: File): Boolean {
    if (!exists() || !other.exists()) return false
    if (length() != other.length()) return false
    if (absolutePath == other.absolutePath) return true
    
    return try {
        inputStream().use { input1 ->
            other.inputStream().use { input2 ->
                val buffer1 = ByteArray(8192)
                val buffer2 = ByteArray(8192)
                
                while (true) {
                    val read1 = input1.read(buffer1)
                    val read2 = input2.read(buffer2)
                    
                    if (read1 != read2) return false
                    if (read1 == -1) break
                    if (!buffer1.contentEquals(buffer2, read1)) return false
                }
                true
            }
        }
    } catch (e: Exception) {
        false
    }
}

private fun ByteArray.contentEquals(other: ByteArray, length: Int): Boolean {
    for (i in 0 until length) {
        if (this[i] != other[i]) return false
    }
    return true
}
