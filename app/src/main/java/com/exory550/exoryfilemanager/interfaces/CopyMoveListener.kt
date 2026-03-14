package com.exory550.exoryfilemanager.interfaces

import com.exory550.exoryfilemanager.models.FileItem

interface CopyMoveListener {
    fun onCopyStarted(totalItems: Int, totalSize: Long)
    fun onCopyProgress(fileName: String, progress: Int, total: Int, bytesCopied: Long, totalBytes: Long)
    fun onCopyItemCompleted(fileItem: FileItem)
    fun onCopyCompleted(successCount: Int, failCount: Int)
    fun onCopyError(error: String)
    fun onCopyCancelled()
}
