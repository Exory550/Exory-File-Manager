package com.exory550.exoryfilemanager.tasks

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FileOperationTask @Inject constructor(
    @ApplicationContext private val context: Context
)
