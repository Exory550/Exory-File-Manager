package com.exory550.exoryfilemanager.extensions

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.exory550.exoryfilemanager.BuildConfig
import java.io.*
import java.nio.channels.FileChannel

/**
 * Storage-related extension functions for Context
 */

/**
 * Get all available storage volumes
 */
val Context.storageVolumes: List<StorageInfo>
    get() {
        val volumes = mutableListOf<StorageInfo>()
        
        // Internal storage
        try {
            val internalPath = Environment.getExternalStorageDirectory().absolutePath
            val internalFile = File(internalPath)
            volumes.add(
                StorageInfo(
                    path = internalPath,
                    description = getString(android.R.string.yes),
                    totalSpace = internalFile.totalSpace,
                    freeSpace = internalFile.freeSpace,
                    isInternal = true,
                    isRemovable = false,
                    isEmulated = true,
                    isPrimary = true,
                    state = Environment.getExternalStorageState(internalFile)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // External storage volumes (SD cards, USB OTG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            volumes.addAll(getStorageVolumesApi24())
        } else {
            volumes.addAll(getStorageVolumesLegacy())
        }
        
        return volumes
    }

@RequiresApi(Build.VERSION_CODES.N)
private fun Context.getStorageVolumesApi24(): List<StorageInfo> {
    val volumes = mutableListOf<StorageInfo>()
    val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
    
    storageManager.storageVolumes.forEach { volume ->
        try {
            val path = volume.getDirectory()?.absolutePath
            if (path != null && !path.contains("/emulated/")) {
                val file = File(path)
                val isPrimary = volume.isPrimary
                val isRemovable = volume.isRemovable
                val isEmulated = volume.isEmulated
                val state = volume.state ?: Environment.MEDIA_UNKNOWN
                val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    volume.getDescription(this)
                } else {
                    @Suppress("DEPRECATION")
                    volume.description
                } ?: getString(android.R.string.unknownName)
                
                volumes.add(
                    StorageInfo(
                        path = path,
                        description = description,
                        totalSpace = file.totalSpace,
                        freeSpace = file.freeSpace,
                        isInternal = isPrimary && !isRemovable,
                        isRemovable = isRemovable,
                        isEmulated = isEmulated,
                        isPrimary = isPrimary,
                        state = state,
                        uuid = volume.uuid,
                        volumeId = volume.toString()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    return volumes
}

private fun Context.getStorageVolumesLegacy(): List<StorageInfo> {
    val volumes = mutableListOf<StorageInfo>()
    
    // Try to get external SD card path
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
        try {
            val storageDir = File("/storage")
            if (storageDir.exists() && storageDir.isDirectory) {
                storageDir.listFiles()?.forEach { file ->
                    if (file.isDirectory && !file.absolutePath.contains("emulated") && 
                        !file.absolutePath.contains("self") && file.canRead()) {
                        
                        volumes.add(
                            StorageInfo(
                                path = file.absolutePath,
                                description = getString(R.string.external_storage),
                                totalSpace = file.totalSpace,
                                freeSpace = file.freeSpace,
                                isInternal = false,
                                isRemovable = true,
                                isEmulated = false,
                                isPrimary = false,
                                state = Environment.getExternalStorageState(file)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Try using System.getenv
    try {
        val secondaryStorage = System.getenv("SECONDARY_STORAGE")
        if (!secondaryStorage.isNullOrBlank()) {
            val paths = secondaryStorage.split(":")
            paths.forEach { path ->
                val file = File(path)
                if (file.exists() && file.isDirectory) {
                    volumes.add(
                        StorageInfo(
                            path = file.absolutePath,
                            description = getString(R.string.external_storage),
                            totalSpace = file.totalSpace,
                            freeSpace = file.freeSpace,
                            isInternal = false,
                            isRemovable = true,
                            isEmulated = false,
                            isPrimary = false,
                            state = Environment.getExternalStorageState(file)
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    return volumes
}

/**
 * Get storage info for a specific path
 */
fun Context.getStorageInfo(path: String): StorageInfo? {
    return storageVolumes.firstOrNull { path.startsWith(it.path) }
}

/**
 * Check if a path is on internal storage
 */
fun Context.isInternalStorage(path: String): Boolean {
    return getStorageInfo(path)?.isInternal == true
}

/**
 * Check if a path is on removable storage (SD card)
 */
fun Context.isRemovableStorage(path: String): Boolean {
    return getStorageInfo(path)?.isRemovable == true
}

/**
 * Get external storage volumes (SD cards)
 */
val Context.externalStorageVolumes: List<StorageInfo>
    get() = storageVolumes.filter { it.isRemovable }

/**
 * Get primary storage (usually internal)
 */
val Context.primaryStorage: StorageInfo?
    get() = storageVolumes.firstOrNull { it.isPrimary }

/**
 * Get DocumentFile from URI with permissions
 */
fun Context.getDocumentFile(uri: Uri): DocumentFile? {
    return DocumentFile.fromSingleUri(this, uri)
}

/**
 * Get DocumentFile from tree URI with permissions
 */
fun Context.getTreeDocumentFile(uri: Uri): DocumentFile? {
    return DocumentFile.fromTreeUri(this, uri)
}

/**
 * Create a file in a document tree
 */
fun Context.createFileInTree(treeUri: Uri, mimeType: String, fileName: String): DocumentFile? {
    val tree = getTreeDocumentFile(treeUri) ?: return null
    return tree.createFile(mimeType, fileName)
}

/**
 * Create a directory in a document tree
 */
fun Context.createDirectoryInTree(treeUri: Uri, dirName: String): DocumentFile? {
    val tree = getTreeDocumentFile(treeUri) ?: return null
    return tree.createDirectory(dirName)
}

/**
 * Get file from MediaStore by URI
 */
fun Context.getFileFromMediaUri(uri: Uri): File? {
    val projection = arrayOf(MediaStore.MediaColumns.DATA)
    return try {
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            if (cursor.moveToFirst()) {
                val path = cursor.getString(columnIndex)
                File(path)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Get file from URI using various methods
 */
fun Context.getFileFromUri(uri: Uri): File? {
    // Try MediaStore first
    getFileFromMediaUri(uri)?.let { return it }
    
    // Try to get from document URI
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        getFileFromDocumentUri(uri)?.let { return it }
    }
    
    // Try to get from file URI
    if (uri.scheme == "file") {
        uri.path?.let { return File(it) }
    }
    
    // Try to copy to cache as last resort
    return copyUriToCache(uri)
}

@RequiresApi(Build.VERSION_CODES.KITKAT)
private fun Context.getFileFromDocumentUri(uri: Uri): File? {
    if (DocumentsContract.isDocumentUri(this, uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        if (uri.authority == "com.android.externalstorage.documents") {
            val split = docId.split(":")
            if (split.size >= 2) {
                val type = split[0]
                val path = split[1]
                return File("/storage/$type/$path")
            }
        }
    }
    return null
}

/**
 * Copy URI content to cache and return file
 */
fun Context.copyUriToCache(uri: Uri, fileName: String? = null): File? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val name = fileName ?: getFileNameFromUri(uri) ?: "temp_${System.currentTimeMillis()}"
        val outputFile = File(cacheDir, name)
        
        FileOutputStream(outputFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        
        outputFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Get file name from URI
 */
fun Context.getFileNameFromUri(uri: Uri): String? {
    var name: String? = null
    
    if (uri.scheme == "content") {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
    }
    
    if (name == null) {
        name = uri.path
        val cut = name?.lastIndexOf('/')
        if (cut != -1) {
            name = name?.substring(cut!! + 1)
        }
    }
    
    return name
}

/**
 * Get file size from URI
 */
fun Context.getFileSizeFromUri(uri: Uri): Long {
    var size: Long = -1
    
    if (uri.scheme == "content") {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && it.moveToFirst()) {
                size = it.getLong(sizeIndex)
            }
        }
    } else if (uri.scheme == "file") {
        uri.path?.let { path ->
            val file = File(path)
            if (file.exists()) {
                size = file.length()
            }
        }
    }
    
    return size
}

/**
 * Get MIME type from URI
 */
fun Context.getMimeTypeFromUri(uri: Uri): String {
    var mimeType = contentResolver.getType(uri)
    
    if (mimeType == null) {
        val extension = getFileNameFromUri(uri)?.substringAfterLast(".", "")
        if (!extension.isNullOrBlank()) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        }
    }
    
    return mimeType ?: "*/*"
}

/**
 * Check if a URI is accessible
 */
fun Context.isUriAccessible(uri: Uri): Boolean {
    return try {
        contentResolver.openInputStream(uri)?.close()
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Take persistable permissions for a URI
 */
fun Context.takeUriPermissions(uri: Uri, flags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or 
                                                      Intent.FLAG_GRANT_WRITE_URI_PERMISSION) {
    try {
        contentResolver.takePersistableUriPermission(uri, flags)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Release persistable permissions for a URI
 */
fun Context.releaseUriPermissions(uri: Uri) {
    try {
        contentResolver.releasePersistableUriPermission(uri, 
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Check if we have persistable permissions for a URI
 */
fun Context.hasUriPermissions(uri: Uri): Boolean {
    return try {
        contentResolver.persistedUriPermissions.any { it.uri == uri }
    } catch (e: Exception) {
        false
    }
}

/**
 * Get all persisted URI permissions
 */
val Context.persistedUriPermissions: List<UriPermission>
    get() = contentResolver.persistedUriPermissions

/**
 * Get storage free space in bytes
 */
fun Context.getStorageFreeSpace(path: String = Environment.getExternalStorageDirectory().path): Long {
    return try {
        val stat = StatFs(path)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.availableBlocksLong * stat.blockSizeLong
        } else {
            @Suppress("DEPRECATION")
            stat.availableBlocks.toLong() * stat.blockSize.toLong()
        }
    } catch (e: Exception) {
        0
    }
}

/**
 * Get storage total space in bytes
 */
fun Context.getStorageTotalSpace(path: String = Environment.getExternalStorageDirectory().path): Long {
    return try {
        val stat = StatFs(path)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.blockCountLong * stat.blockSizeLong
        } else {
            @Suppress("DEPRECATION")
            stat.blockCount.toLong() * stat.blockSize.toLong()
        }
    } catch (e: Exception) {
        0
    }
}

/**
 * Get storage used space in bytes
 */
fun Context.getStorageUsedSpace(path: String = Environment.getExternalStorageDirectory().path): Long {
    return getStorageTotalSpace(path) - getStorageFreeSpace(path)
}

/**
 * Get storage usage percentage
 */
fun Context.getStorageUsagePercent(path: String = Environment.getExternalStorageDirectory().path): Int {
    val total = getStorageTotalSpace(path)
    val free = getStorageFreeSpace(path)
    return if (total > 0) {
        ((total - free) * 100 / total).toInt()
    } else {
        0
    }
}

/**
 * Data class for storage information
 */
data class StorageInfo(
    val path: String,
    val description: String,
    val totalSpace: Long,
    val freeSpace: Long,
    val isInternal: Boolean,
    val isRemovable: Boolean,
    val isEmulated: Boolean,
    val isPrimary: Boolean,
    val state: String,
    val uuid: String? = null,
    val volumeId: String? = null
) {
    val usedSpace: Long
        get() = totalSpace - freeSpace
    
    val usedPercentage: Int
        get() = if (totalSpace > 0) {
            ((totalSpace - freeSpace) * 100 / totalSpace).toInt()
        } else {
            0
        }
    
    val isMounted: Boolean
        get() = state == Environment.MEDIA_MOUNTED
    
    val isMountedReadOnly: Boolean
        get() = state == Environment.MEDIA_MOUNTED_READ_ONLY
    
    val isReadable: Boolean
        get() = isMounted || isMountedReadOnly
    
    val isWritable: Boolean
        get() = isMounted
}

/**
 * Get URI for a file using FileProvider
 */
fun Context.getUriForFile(file: File): Uri {
    return FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
}

/**
 * Save a file to MediaStore
 */
@RequiresApi(Build.VERSION_CODES.Q)
fun Context.saveToMediaStore(
    file: File,
    mimeType: String,
    relativePath: String = Environment.DIRECTORY_DOWNLOADS
): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        put(MediaStore.MediaColumns.SIZE, file.length())
        put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
    }
    
    val collection = when {
        mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        mimeType.startsWith("audio/") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        else -> MediaStore.Files.getContentUri("external")
    }
    
    val uri = contentResolver.insert(collection, contentValues)
    
    uri?.let {
        contentResolver.openOutputStream(it)?.use { outputStream ->
            FileInputStream(file).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
    
    return uri
}
