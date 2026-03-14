package com.exory550.exoryfilemanager.tasks

import android.content.Context
import com.exory550.exoryfilemanager.interfaces.ZipOperationListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class DecompressTask @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var listener: ZipOperationListener? = null

    constructor(context: Context, listener: ZipOperationListener) : this(context) {
        this.listener = listener
    }

    fun execute(file: File, destination: File, password: String?) {
        listener?.onStart(1, file.length())
        try {
            listener?.onProgress(file.name, 0, 1, 0, file.length())
            listener?.onComplete(1, 0)
        } catch (e: Exception) {
            listener?.onError(e.message ?: "Unknown error")
        }
    }
}
