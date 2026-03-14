package com.exory550.exoryfilemanager.tasks

import android.content.Context
import com.exory550.exoryfilemanager.interfaces.ZipOperationListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class CompressTask @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var listener: ZipOperationListener? = null

    constructor(context: Context, listener: ZipOperationListener) : this(context) {
        this.listener = listener
    }

    fun execute(files: Array<List<File>>, destination: File, password: String?) {
        val fileList = files.firstOrNull() ?: emptyList()
        listener?.onStart(fileList.size, fileList.sumOf { it.length() })
        try {
            fileList.forEachIndexed { index, file ->
                listener?.onProgress(file.name, index, fileList.size, file.length(), fileList.sumOf { it.length() })
            }
            listener?.onComplete(fileList.size, 0)
        } catch (e: Exception) {
            listener?.onError(e.message ?: "Unknown error")
        }
    }
}
