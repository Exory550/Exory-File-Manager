package com.exory550.exoryfilemanager.interfaces

interface ZipOperationListener {
    fun onProgress(progress: Int)
    fun onSuccess()
    fun onError(error: String)
}
