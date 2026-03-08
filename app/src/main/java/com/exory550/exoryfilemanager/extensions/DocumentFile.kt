package com.exory550.exoryfilemanager.extensions

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import java.io.*

/**
 * Extension functions for DocumentFile
 */

/**
 * Get file name from DocumentFile
 */
val DocumentFile.fileName: String
    get() = name ?: "Unknown"

/**
 * Get file extension from DocumentFile
 */
val DocumentFile.fileExtension: String
    get() {
        val name = name ?: return ""
        return name.substringAfterLast(".", "")
    }

/**
 * Get MIME type from DocumentFile
 */
val DocumentFile.mimeTypeSafe: String
    get() = type ?: "*/*"

/**
 * Check if DocumentFile is an image file
 */
val DocumentFile.isImage: Boolean
    get() {
        val mime = type ?: return false
        return mime.startsWith("image/")
    }

/**
 * Check if DocumentFile is a video file
 */
val DocumentFile.isVideo: Boolean
    get() {
        val mime = type ?: return false
        return mime.startsWith("video/")
    }

/**
 * Check if DocumentFile is an audio file
 */
val DocumentFile.isAudio: Boolean
    get() {
        val mime = type ?: return false
        return mime.startsWith("audio/")
    }

/**
 * Check if DocumentFile is a document file
 */
val DocumentFile.isDocument: Boolean
    get() {
        val mime = type ?: return false
        return mime.startsWith("text/") || 
               mime == "application/pdf" ||
               mime.contains("document") ||
               mime.contains("spreadsheet") ||
               mime.contains("presentation")
    }

/**
 * Check if DocumentFile is an archive file
 */
val DocumentFile.isArchive: Boolean
    get() {
        val mime = type ?: return false
        return mime == "application/zip" ||
               mime == "application/x-rar-compressed" ||
               mime == "application/x-7z-compressed" ||
               mime == "application/x-tar" ||
               mime == "application/gzip"
    }

/**
 * Check if DocumentFile is readable
 */
val DocumentFile.isReadable: Boolean
    get() = canRead()

/**
 * Check if DocumentFile is writable
 */
val DocumentFile.isWritable: Boolean
    get() = canWrite()

/**
 * Get parent DocumentFile
 */
val DocumentFile.parentFile: DocumentFile?
    get() = parentFile

/**
 * List files as DocumentFile array
 */
val DocumentFile.listFilesSafe: Array<DocumentFile>
    get() = listFiles() ?: emptyArray()

/**
 * Get file size
 */
fun DocumentFile.getSize(context: Context): Long {
    return try {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            pfd.statSize
        } ?: 0
    } catch (e: Exception) {
        0
    }
}

/**
 * Get last modified time
 */
fun DocumentFile.getLastModified(context: Context): Long {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    cursor.getLong(index)
                } else {
                    0
                }
            } else {
                0
            }
        } ?: 0
    } catch (e: Exception) {
        0
    }
}

/**
 * Copy DocumentFile to another location
 */
fun DocumentFile.copyTo(context: Context, destinationDir: DocumentFile, newName: String? = null): DocumentFile? {
    val name = newName ?: fileName
    val mime = type
    
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val destFile = destinationDir.createFile(mime ?: "*/*", name)
            destFile?.let {
                context.contentResolver.openOutputStream(it.uri)?.use { output ->
                    input.copyTo(output)
                }
            }
            destFile
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Move DocumentFile to another location
 */
fun DocumentFile.moveTo(context: Context, destinationDir: DocumentFile, newName: String? = null): DocumentFile? {
    // Try to rename if in same directory
    if (parentFile?.uri == destinationDir.uri && newName != null) {
        if (renameTo(newName)) {
            return destinationDir.findFile(newName)
        }
    }
    
    // Otherwise copy and delete
    val copied = copyTo(context, destinationDir, newName)
    if (copied != null) {
        delete()
    }
    return copied
}

/**
 * Rename DocumentFile
 */
fun DocumentFile.renameToSafely(newName: String): Boolean {
    return try {
        renameTo(newName)
    } catch (e: Exception) {
        false
    }
}

/**
 * Delete DocumentFile safely
 */
fun DocumentFile.deleteSafely(): Boolean {
    return try {
        delete()
    } catch (e: Exception) {
        false
    }
}

/**
 * Check if DocumentFile exists
 */
fun DocumentFile.exists(): Boolean {
    return try {
        exists()
    } catch (e: Exception) {
        false
    }
}

/**
 * Get URI for sharing
 */
fun DocumentFile.getShareUri(context: Context, authority: String): Uri {
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        authority,
        File(uri.path ?: "")
    )
}

/**
 * Get input stream
 */
fun DocumentFile.getInputStream(context: Context): InputStream? {
    return try {
        context.contentResolver.openInputStream(uri)
    } catch (e: Exception) {
        null
    }
}

/**
 * Get output stream
 */
fun DocumentFile.getOutputStream(context: Context): OutputStream? {
    return try {
        context.contentResolver.openOutputStream(uri)
    } catch (e: Exception) {
        null
    }
}

/**
 * Write string to DocumentFile
 */
fun DocumentFile.writeString(context: Context, content: String): Boolean {
    return try {
        getOutputStream(context)?.use { output ->
            output.write(content.toByteArray())
            true
        } ?: false
    } catch (e: Exception) {
        false
    }
}

/**
 * Read string from DocumentFile
 */
fun DocumentFile.readString(context: Context): String? {
    return try {
        getInputStream(context)?.use { input ->
            input.bufferedReader().readText()
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Write bytes to DocumentFile
 */
fun DocumentFile.writeBytes(context: Context, data: ByteArray): Boolean {
    return try {
        getOutputStream(context)?.use { output ->
            output.write(data)
            true
        } ?: false
    } catch (e: Exception) {
        false
    }
}

/**
 * Read bytes from DocumentFile
 */
fun DocumentFile.readBytes(context: Context): ByteArray? {
    return try {
        getInputStream(context)?.use { input ->
            input.readBytes()
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Get file info as map
 */
fun DocumentFile.getInfo(context: Context): Map<String, Any> {
    val info = mutableMapOf<String, Any>()
    
    info["name"] = fileName
    info["uri"] = uri.toString()
    info["mimeType"] = mimeTypeSafe
    info["isDirectory"] = isDirectory
    info["isFile"] = isFile
    info["isVirtual"] = isVirtual
    info["canRead"] = canRead()
    info["canWrite"] = canWrite()
    info["size"] = getSize(context)
    info["lastModified"] = getLastModified(context)
    
    return info
}

/**
 * Create directory hierarchy
 */
fun DocumentFile.mkdirs(context: Context, path: String): DocumentFile? {
    var current = this
    val parts = path.split("/").filter { it.isNotEmpty() }
    
    for (part in parts) {
        val next = current.findFile(part) ?: current.createDirectory(part)
        if (next == null || !next.exists()) {
            return null
        }
        current = next
    }
    
    return current
}

/**
 * Find file by path
 */
fun DocumentFile.findFileByPath(context: Context, path: String): DocumentFile? {
    var current = this
    val parts = path.split("/").filter { it.isNotEmpty() }
    
    for (part in parts) {
        current = current.findFile(part) ?: return null
        if (!current.exists()) return null
    }
    
    return current
}

/**
 * Get all files recursively
 */
fun DocumentFile.listFilesRecursive(): List<DocumentFile> {
    val result = mutableListOf<DocumentFile>()
    
    fun listRecursive(file: DocumentFile) {
        result.add(file)
        if (file.isDirectory) {
            file.listFilesSafe.forEach { listRecursive(it) }
        }
    }
    
    listRecursive(this)
    return result
}

/**
 * Get total size recursively
 */
fun DocumentFile.getTotalSize(context: Context): Long {
    var total = 0L
    
    if (isDirectory) {
        listFilesSafe.forEach { file ->
            total += file.getTotalSize(context)
        }
    } else {
        total += getSize(context)
    }
    
    return total
}

/**
 * Get file count recursively
 */
val DocumentFile.fileCount: Int
    get() {
        var count = 0
        
        if (isDirectory) {
            count += listFilesSafe.size
            listFilesSafe.forEach { file ->
                if (file.isDirectory) {
                    count += file.fileCount
                }
            }
        } else {
            count = 1
        }
        
        return count
    }

/**
 * Get directory count
 */
val DocumentFile.directoryCount: Int
    get() {
        var count = 0
        
        if (isDirectory) {
            listFilesSafe.forEach { file ->
                if (file.isDirectory) {
                    count++
                    count += file.directoryCount
                }
            }
        }
        
        return count
    }

/**
 * Extension functions for Uri
 */

/**
 * Convert Uri to DocumentFile
 */
fun Uri.toDocumentFile(context: Context): DocumentFile? {
    return DocumentFile.fromSingleUri(context, this)
}

/**
 * Convert tree Uri to DocumentFile
 */
fun Uri.toTreeDocumentFile(context: Context): DocumentFile? {
    return DocumentFile.fromTreeUri(context, this)
}

/**
 * Get DocumentFile for path
 */
fun Context.getDocumentFileForPath(path: String): DocumentFile? {
    return try {
        val file = File(path)
        val uri = Uri.fromFile(file)
        DocumentFile.fromFile(file)
    } catch (e: Exception) {
        null
    }
}

/**
 * Get DocumentFile for Uri with permissions
 */
fun Context.getPersistedDocumentFile(uri: Uri): DocumentFile? {
    return try {
        takeUriPermissions(uri)
        DocumentFile.fromSingleUri(this, uri)
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension functions for File to DocumentFile
 */

/**
 * Convert File to DocumentFile
 */
fun File.toDocumentFile(): DocumentFile {
    return DocumentFile.fromFile(this)
}

/**
 * Get DocumentFile from File with context
 */
fun File.toDocumentFile(context: Context): DocumentFile? {
    return try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            this
        )
        DocumentFile.fromSingleUri(context, uri)
    } catch (e: Exception) {
        DocumentFile.fromFile(this)
    }
}

/**
 * Extension functions for MediaStore integration
 */

@RequiresApi(Build.VERSION_CODES.Q)
fun DocumentFile.saveToMediaStore(context: Context, relativePath: String = Environment.DIRECTORY_DOWNLOADS): Uri? {
    if (!isFile) return null
    
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeTypeSafe)
        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        put(MediaStore.MediaColumns.SIZE, getSize(context))
        put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
    }
    
    val collection = when {
        isImage -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        isVideo -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        isAudio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        else -> MediaStore.Files.getContentUri("external")
    }
    
    val uri = context.contentResolver.insert(collection, contentValues)
    
    uri?.let {
        getInputStream(context)?.use { input ->
            context.contentResolver.openOutputStream(it)?.use { output ->
                input.copyTo(output)
            }
        }
    }
    
    return uri
}

/**
 * Create a new file with unique name if exists
 */
fun DocumentFile.createUniqueFile(context: Context, mimeType: String, fileName: String): DocumentFile? {
    var counter = 1
    var newName = fileName
    
    while (findFile(newName)?.exists() == true) {
        val nameWithoutExt = fileName.substringBeforeLast(".")
        val extension = fileName.substringAfterLast(".", "")
        
        newName = if (extension.isNotEmpty()) {
            "${nameWithoutExt}_${counter}.${extension}"
        } else {
            "${fileName}_${counter}"
        }
        counter++
    }
    
    return createFile(mimeType, newName)
}

/**
 * Create a new directory with unique name if exists
 */
fun DocumentFile.createUniqueDirectory(dirName: String): DocumentFile? {
    var counter = 1
    var newName = dirName
    
    while (findFile(newName)?.exists() == true) {
        newName = "${dirName}_${counter}"
        counter++
    }
    
    return createDirectory(newName)
}

/**
 * Sort files by name
 */
fun List<DocumentFile>.sortByName(ascending: Boolean = true): List<DocumentFile> {
    return if (ascending) {
        sortedBy { it.fileName.lowercase() }
    } else {
        sortedByDescending { it.fileName.lowercase() }
    }
}

/**
 * Sort files by date
 */
fun List<DocumentFile>.sortByDate(context: Context, ascending: Boolean = true): List<DocumentFile> {
    return if (ascending) {
        sortedBy { it.getLastModified(context) }
    } else {
        sortedByDescending { it.getLastModified(context) }
    }
}

/**
 * Sort files by size
 */
fun List<DocumentFile>.sortBySize(context: Context, ascending: Boolean = true): List<DocumentFile> {
    return if (ascending) {
        sortedBy { it.getSize(context) }
    } else {
        sortedByDescending { it.getSize(context) }
    }
}

/**
 * Filter files by type
 */
fun List<DocumentFile>.filterByType(mimeType: String): List<DocumentFile> {
    return filter { it.type?.startsWith(mimeType) == true }
}

/**
 * Filter directories
 */
val List<DocumentFile>.directories: List<DocumentFile>
    get() = filter { it.isDirectory }

/**
 * Filter files (non-directories)
 */
val List<DocumentFile>.files: List<DocumentFile>
    get() = filter { it.isFile }
