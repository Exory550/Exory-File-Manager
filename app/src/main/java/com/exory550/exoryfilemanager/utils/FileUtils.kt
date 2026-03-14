package com.exory550.exoryfilemanager.utils

import android.content.Context
import com.exory550.exoryfilemanager.models.FileItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getFilesInDirectory(path: String, showHidden: Boolean = false): List<FileItem> {
        return try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) return emptyList()
            dir.listFiles()
                ?.filter { if (showHidden) true else !it.isHidden }
                ?.map { file ->
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        size = if (file.isFile) file.length() else 0,
                        lastModified = file.lastModified(),
                        isDirectory = file.isDirectory,
                        isHidden = file.isHidden,
                        mimeType = if (file.isFile) getMimeType(file.extension) else ""
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            "apk" -> "application/vnd.android.package-archive"
            else -> "application/octet-stream"
        }
    }
}
