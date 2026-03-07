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
import com.exory550.exoryfilemanager.models.CompressedItem
import com.exory550.exoryfilemanager.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DecompressItemsAdapter(
    private val onItemClick: (CompressedItem) -> Unit,
    private val onItemLongClick: (CompressedItem) -> Boolean
) : ListAdapter<CompressedItem, DecompressItemsAdapter.ViewHolder>(DiffCallback()) {

    private var selectedItems = mutableSetOf<CompressedItem>()
    private var isSelectionMode = false
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_decompress_item, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(item)
                holder.updateSelectionState(item in selectedItems)
            } else {
                onItemClick(item)
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                isSelectionMode = true
            }
            toggleSelection(item)
            holder.updateSelectionState(item in selectedItems)
            onItemLongClick(item)
        }
    }
    
    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        if (!enabled) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }
    
    fun toggleSelection(item: CompressedItem) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        notifyItemChanged(currentList.indexOf(item))
    }
    
    fun getSelectedItems(): List<CompressedItem> = selectedItems.toList()
    
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(currentList)
        notifyDataSetChanged()
    }
    
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPath: TextView = itemView.findViewById(R.id.tvPath)
        private val tvSize: TextView = itemView.findViewById(R.id.tvSize)
        private val tvModified: TextView = itemView.findViewById(R.id.tvModified)
        private val tvCompressionRatio: TextView = itemView.findViewById(R.id.tvCompressionRatio)
        private val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
        private val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)
        
        fun bind(item: CompressedItem) {
            tvName.text = item.name
            tvPath.text = item.path
            
            val context = itemView.context
            
            // Set icon based on type
            when {
                item.isDirectory -> {
                    ivIcon.setImageResource(R.drawable.ic_folder)
                }
                item.name.lowercase(Locale.getDefault()).endsWith(".zip") -> {
                    ivIcon.setImageResource(R.drawable.ic_zip)
                }
                item.name.lowercase(Locale.getDefault()).endsWith(".rar") -> {
                    ivIcon.setImageResource(R.drawable.ic_rar)
                }
                item.name.lowercase(Locale.getDefault()).endsWith(".7z") -> {
                    ivIcon.setImageResource(R.drawable.ic_7z)
                }
                item.name.lowercase(Locale.getDefault()).endsWith(".tar") -> {
                    ivIcon.setImageResource(R.drawable.ic_tar)
                }
                item.name.lowercase(Locale.getDefault()).endsWith(".gz") -> {
                    ivIcon.setImageResource(R.drawable.ic_gz)
                }
                FileUtils.isImageFile(item.name) -> {
                    Glide.with(context)
                        .load(File(item.path))
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_file)
                        .centerCrop()
                        .into(ivIcon)
                }
                else -> {
                    ivIcon.setImageResource(FileUtils.getFileIconResource(item.name))
                }
            }
            
            // Size
            tvSize.text = if (item.isDirectory) {
                context.getString(R.string.folder)
            } else {
                FileUtils.formatFileSize(item.size)
            }
            
            // Modified date
            tvModified.text = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
            ).format(Date(item.lastModified))
            
            // Compression ratio (for files inside compressed archive)
            if (!item.isDirectory && item.compressedSize > 0 && item.size > 0) {
                val ratio = ((item.size - item.compressedSize) * 100 / item.size.toDouble())
                tvCompressionRatio.text = String.format(Locale.getDefault(), "%.1f%%", ratio)
                tvCompressionRatio.visibility = View.VISIBLE
            } else {
                tvCompressionRatio.visibility = View.GONE
            }
            
            // Selection state
            updateSelectionState(item in selectedItems)
        }
        
        fun updateSelectionState(isSelected: Boolean) {
            if (isSelectionMode) {
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
    
    class DiffCallback : DiffUtil.ItemCallback<CompressedItem>() {
        override fun areItemsTheSame(oldItem: CompressedItem, newItem: CompressedItem): Boolean {
            return oldItem.path == newItem.path
        }
        
        override fun areContentsTheSame(oldItem: CompressedItem, newItem: CompressedItem): Boolean {
            return oldItem == newItem
        }
    }
}
