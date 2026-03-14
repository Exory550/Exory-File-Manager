package com.exory550.exoryfilemanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilePickerItemsAdapter(
    private val onItemClick: (FileItem) -> Unit,
    private val onItemLongClick: (FileItem) -> Boolean
) : ListAdapter<FileItem, FilePickerItemsAdapter.ViewHolder>(DiffCallback()) {

    private var selectedItems = mutableSetOf<FileItem>()
    private var isSelectionMode = false
    private var showCheckboxes = false
    private var showFileSize = true
    private var showDate = true
    private var singleClickSelect = false
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_picker, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        
        holder.itemView.setOnClickListener {
            if (isSelectionMode || showCheckboxes) {
                toggleSelection(item)
                holder.updateSelectionState(item in selectedItems)
            } else if (singleClickSelect) {
                onItemClick(item)
            } else {
                onItemClick(item)
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode && !showCheckboxes) {
                isSelectionMode = true
                toggleSelection(item)
                holder.updateSelectionState(true)
                onItemLongClick(item)
            } else {
                toggleSelection(item)
                holder.updateSelectionState(item in selectedItems)
                onItemLongClick(item)
            }
            true
        }
    }
    
    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        if (!enabled) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }
    
    fun setShowCheckboxes(show: Boolean) {
        showCheckboxes = show
        if (!show) {
            isSelectionMode = false
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }
    
    fun setSingleClickSelect(singleClick: Boolean) {
        singleClickSelect = singleClick
    }
    
    fun toggleSelection(item: FileItem) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        notifyItemChanged(currentList.indexOf(item))
    }
    
    fun getSelectedItems(): List<FileItem> = selectedItems.toList()
    
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(currentList)
        notifyDataSetChanged()
    }
    
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
    
    fun setShowFileSize(show: Boolean) {
        showFileSize = show
        notifyDataSetChanged()
    }
    
    fun setShowDate(show: Boolean) {
        showDate = show
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPath: TextView = itemView.findViewById(R.id.tvPath)
        private val tvSize: TextView = itemView.findViewById(R.id.tvSize)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
        private val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)
        
        fun bind(item: FileItem) {
            tvName.text = item.name
            tvPath.text = File(item.path).parent ?: "/"
            
            val context = itemView.context
            
            // Set icon based on type
            when {
                item.isDirectory -> {
                    ivIcon.setImageResource(R.drawable.ic_folder)
                }
                FileUtils.isImageFile(item.name) -> {
                    Glide.with(context)
                        .load(File(item.path))
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_file)
                        .centerCrop()
                        .into(ivIcon)
                }
                FileUtils.isVideoFile(item.name) -> {
                    Glide.with(context)
                        .load(File(item.path))
                        .placeholder(R.drawable.ic_video)
                        .error(R.drawable.ic_file)
                        .centerCrop()
                        .into(ivIcon)
                }
                FileUtils.isAudioFile(item.name) -> {
                    ivIcon.setImageResource(R.drawable.ic_audio)
                }
                FileUtils.isArchiveFile(item.name) -> {
                    ivIcon.setImageResource(when {
                        item.name.lowercase(Locale.getDefault()).endsWith(".zip") -> R.drawable.ic_zip
                        item.name.lowercase(Locale.getDefault()).endsWith(".rar") -> R.drawable.ic_rar
                        item.name.lowercase(Locale.getDefault()).endsWith(".7z") -> R.drawable.ic_7z
                        item.name.lowercase(Locale.getDefault()).endsWith(".tar") -> R.drawable.ic_tar
                        item.name.lowercase(Locale.getDefault()).endsWith(".gz") -> R.drawable.ic_gz
                        else -> R.drawable.ic_archive
                    })
                }
                FileUtils.isDocumentFile(item.name) -> {
                    ivIcon.setImageResource(when {
                        item.name.lowercase(Locale.getDefault()).endsWith(".pdf") -> R.drawable.ic_pdf
                        item.name.lowercase(Locale.getDefault()).endsWith(".doc") || 
                        item.name.lowercase(Locale.getDefault()).endsWith(".docx") -> R.drawable.ic_word
                        item.name.lowercase(Locale.getDefault()).endsWith(".xls") ||
                        item.name.lowercase(Locale.getDefault()).endsWith(".xlsx") -> R.drawable.ic_excel
                        item.name.lowercase(Locale.getDefault()).endsWith(".ppt") ||
                        item.name.lowercase(Locale.getDefault()).endsWith(".pptx") -> R.drawable.ic_powerpoint
                        item.name.lowercase(Locale.getDefault()).endsWith(".txt") -> R.drawable.ic_text
                        else -> R.drawable.ic_document
                    })
                }
                else -> {
                    ivIcon.setImageResource(R.drawable.ic_file)
                }
            }
            
            // Size
            if (showFileSize && !item.isDirectory) {
                tvSize.text = FileUtils.formatFileSize(item.size)
                tvSize.visibility = View.VISIBLE
            } else {
                tvSize.visibility = View.GONE
            }
            
            // Date
            if (showDate) {
                tvDate.text = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(item.lastModified))
                tvDate.visibility = View.VISIBLE
            } else {
                tvDate.visibility = View.GONE
            }
            
            // Selection state
            updateSelectionState(item in selectedItems)
        }
        
        fun updateSelectionState(isSelected: Boolean) {
            if (isSelectionMode || showCheckboxes) {
                selectionOverlay.visibility = View.VISIBLE
                ivSelected.visibility = View.VISIBLE
                ivSelected.setImageResource(
                    if (isSelected) R.drawable.ic_checkbox_checked
                    else R.drawable.ic_checkbox_unchecked
                )
            } else {
                selectionOverlay.visibility = View.GONE
                ivSelected.visibility = View.GONE
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem.path == newItem.path
        }
        
        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem == newItem
        }
    }
}
