package com.exory550.exoryfilemanager.interfaces

interface ZipOperationListener {
    fun onStart(totalItems: Int, totalSize: Long)
    fun onProgress(fileName: String, progress: Int, total: Int, bytesProcessed: Long, totalBytes: Long)
    fun onComplete(successCount: Int, failCount: Int)
    fun onError(error: String)
}
