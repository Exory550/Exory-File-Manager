package com.exory550.exoryfilemanager.models

data class FileItem(
    val id: Long = 0,
    val name: String = "",
    val path: String = "",
    val size: Long = 0,
    val lastModified: Long = 0,
    val isDirectory: Boolean = false,
    val isHidden: Boolean = false,
    val isSelected: Boolean = false,
    val mimeType: String = ""
)
