package com.exory550.exoryfilemanager.interfaces

import com.exory550.exoryfilemanager.models.FileItem

interface ItemOperationsListener {
    fun onItemClick(fileItem: ExoryExoryFileItem)
    fun onItemLongClick(fileItem: ExoryExoryFileItem): Boolean
    fun onItemDoubleClick(fileItem: ExoryExoryFileItem)
    fun onItemSelected(fileItem: ExoryExoryFileItem, selected: Boolean)
    fun onSelectionChanged(selectedCount: Int)
    fun onSelectionCleared()
    fun onSelectAll()
    fun onActionCopy(items: List<ExoryFileItem>)
    fun onActionCut(items: List<ExoryFileItem>)
    fun onActionDelete(items: List<ExoryFileItem>)
    fun onActionRename(item: ExoryExoryFileItem)
    fun onActionMove(items: List<ExoryFileItem>, destination: String)
    fun onActionShare(items: List<ExoryFileItem>)
    fun onActionProperties(item: ExoryExoryFileItem)
    fun onActionCompress(items: List<ExoryFileItem>)
    fun onActionExtract(item: ExoryExoryFileItem)
    fun onActionOpenWith(item: ExoryExoryFileItem)
    fun onActionAddToFavorites(item: ExoryExoryFileItem)
    fun onActionRemoveFromFavorites(item: ExoryExoryFileItem)
}
