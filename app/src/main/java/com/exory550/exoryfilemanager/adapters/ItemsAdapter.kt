package com.exory550.exoryfilemanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ItemsAdapter(
    private var items: List<ExoryFileItem> = emptyList(),
    private val onItemClick: (ExoryFileItem) -> Unit,
    private val onItemLongClick: (ExoryFileItem) -> Boolean
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    private var selectedItems = mutableSetOf<ExoryFileItem>()
    private var isSelectionMode = false
    private var showCheckboxes = false
    private var showSize = true
    private var showDate = true
    private var viewType = VIEW_TYPE_LIST
    
    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GRID = 1
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = when (viewType) {
            VIEW_TYPE_GRID -> R.layout.item_file_grid
            else -> R.layout.item_file_list
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        
        holder.itemView.setOnClickListener {
            if (isSelectionMode || showCheckboxes) {
                toggleSelection(item)
                holder.updateSelectionState(item in selectedItems)
            } else {
                onItemClick(item)
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode && !showCheckboxes) {
                isSelectionMode = true
                toggleSelection(item)
                holder.updateSelectionState(true)
            } else {
                toggleSelection(item)
                holder.updateSelectionState(item in selectedItems)
            }
            onItemLongClick(item)
            true
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    override fun getItemViewType(position: Int): Int = viewType
    
    fun setViewType(type: Int) {
        viewType = type
        notifyDataSetChanged()
    }
    
    fun updateItems(newItems: List<ExoryFileItem>) {
        items = newItems
        notifyDataSetChanged()
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
    
    fun toggleSelection(item: ExoryExoryFileItem) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        notifyItemChanged(items.indexOf(item))
    }
    
    fun getSelectedItems(): List<ExoryFileItem> = selectedItems.toList()
    
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(items)
        notifyDataSetChanged()
    }
    
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
    
    fun setShowSize(show: Boolean) {
        showSize = show
        notifyDataSetChanged()
    }
    
    fun setShowDate(show: Boolean) {
        showDate = show
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        private val tvSize: TextView = itemView.findViewById(R.id.tvSize)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val ivSelected: ImageView? = itemView.findViewById(R.id.ivSelected)
        private val selectionOverlay: View? = itemView.findViewById(R.id.selectionOverlay)
        
        fun bind(item: ExoryExoryFileItem) {
            tvName.text = item.name
            
            val context = itemView.context
            
            // Set icon based on type
            when {
                item.isDirectory -> {
                    ivIcon.setImageResource(R.drawable.ic_folder)
                    tvDetails.text = context.getString(R.string.folder)
                }
                FileUtils.isImageFile(item.name) -> {
                    Glide.with(context)
                        .load(File(item.path))
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_file)
                        .centerCrop()
                        .into(ivIcon)
                    tvDetails.text = context.getString(R.string.image_file)
                }
                FileUtils.isVideoFile(item.name) -> {
                    Glide.with(context)
                        .load(File(item.path))
                        .placeholder(R.drawable.ic_video)
                        .error(R.drawable.ic_file)
                        .centerCrop()
                        .into(ivIcon)
                    tvDetails.text = context.getString(R.string.video_file)
                }
                FileUtils.isAudioFile(item.name) -> {
                    ivIcon.setImageResource(R.drawable.ic_audio)
                    tvDetails.text = context.getString(R.string.audio_file)
                }
                FileUtils.isArchiveFile(item.name) -> {
                    ivIcon.setImageResource(when {
                        item.name.lowercase(Locale.getDefault()).endsWith(".zip") -> R.drawable.ic_zip
                        item.name.lowercase(Locale.getDefault()).endsWith(".rar") -> R.drawable.ic_rar
                        item.name.lowercase(Locale.getDefault()).endsWith(".7z") -> R.drawable.ic_7z
                        else -> R.drawable.ic_archive
                    })
                    tvDetails.text = context.getString(R.string.archive_file)
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
                    tvDetails.text = context.getString(R.string.document_file)
                }
                else -> {
                    ivIcon.setImageResource(R.drawable.ic_file)
                    tvDetails.text = context.getString(R.string.file)
                }
            }
            
            // Size
            if (showSize && !item.isDirectory) {
                tvSize?.text = FileUtils.formatFileSize(item.size)
                tvSize?.visibility = View.VISIBLE
            } else {
                tvSize?.visibility = View.GONE
            }
            
            // Date
            if (showDate) {
                tvDate?.text = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(item.lastModified))
                tvDate?.visibility = View.VISIBLE
            } else {
                tvDate?.visibility = View.GONE
            }
            
            // Selection state
            updateSelectionState(item in selectedItems)
        }
        
        fun updateSelectionState(isSelected: Boolean) {
            if (isSelectionMode || showCheckboxes) {
                selectionOverlay?.visibility = View.VISIBLE
                ivSelected?.visibility = View.VISIBLE
                ivSelected?.setImageResource(
                    if (isSelected) R.drawable.ic_checkbox_checked
                    else R.drawable.ic_checkbox_unchecked
                )
            } else {
                selectionOverlay?.visibility = View.GONE
                ivSelected?.visibility = View.GONE
            }
        }
    }
}
