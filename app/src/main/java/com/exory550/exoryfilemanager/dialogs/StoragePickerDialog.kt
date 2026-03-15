package com.exory550.exoryfilemanager.dialogs

import android.content.Context
import android.os.Environment
import com.exory550.exoryfilemanager.models.StorageInfo

object StoragePickerDialog {
    fun showForWriteAccess(context: Context, onSelected: (String) -> Unit) {
        onSelected(Environment.getExternalStorageDirectory().absolutePath)
    }
}
