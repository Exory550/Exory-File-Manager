package com.exory550.exoryfilemanager.interfaces

import com.exory550.exoryfilemanager.models.FileItem

interface ItemOperationsListener {
    fun onItemClick(fileItem: FileItem)
    fun onItemLongClick(fileItem: FileItem): Boolean
    fun onItemDoubleClick(fileItem: FileItem)
    fun onItemSelected(fileItem: FileItem, selected: Boolean)
    fun onSelectionChanged(selectedCount: Int)
    fun onSelectionCleared()
    fun onSelectAll()
    fun onActionCopy(items: List<FileItem>)
    fun onActionCut(items: List<FileItem>)
    fun onActionDelete(items: List<FileItem>)
    fun onActionRename(item: FileItem)
    fun onActionMove(items: List<FileItem>, destination: String)
    fun onActionShare(items: List<FileItem>)
    fun onActionProperties(item: FileItem)
    fun onActionCompress(items: List<FileItem>)
    fun onActionExtract(item: FileItem)
    fun onActionOpenWith(item: FileItem)
    fun onActionAddToFavorites(item: FileItem)
    fun onActionRemoveFromFavorites(item: FileItem)
}
