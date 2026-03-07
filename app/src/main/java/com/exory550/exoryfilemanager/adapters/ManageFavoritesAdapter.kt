package com.exory550.exoryfilemanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.models.FavoriteItem
import com.exory550.exoryfilemanager.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManageFavoritesAdapter(
    private val onItemClick: (FavoriteItem) -> Unit,
    private val onItemLongClick: (FavoriteItem) -> Boolean,
    private val onEditClick: (FavoriteItem) -> Unit,
    private val onDeleteClick: (FavoriteItem) -> Unit
) : ListAdapter<FavoriteItem, ManageFavoritesAdapter.ViewHolder>(DiffCallback()) {

    private var selectedItems = mutableSetOf<FavoriteItem>()
    private var isSelectionMode = false
    private var showDragHandle = false
    private var showQuickActions = true
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_favorite, parent, false)
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
        
        holder.btnEdit.setOnClickListener {
            onEditClick(item)
        }
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
        
        holder.dragHandle?.visibility = if (showDragHandle) View.VISIBLE else View.GONE
    }
    
    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        if (!enabled) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }
    
    fun toggleSelection(item: FavoriteItem) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        notifyItemChanged(currentList.indexOf(item))
    }
    
    fun getSelectedItems(): List<FavoriteItem> = selectedItems.toList()
    
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(currentList)
        notifyDataSetChanged()
    }
    
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
    
    fun setShowDragHandle(show: Boolean) {
        showDragHandle = show
        notifyDataSetChanged()
    }
    
    fun setShowQuickActions(show: Boolean) {
        showQuickActions = show
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPath: TextView = itemView.findViewById(R.id.tvPath)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvSize: TextView = itemView.findViewById(R.id.tvSize)
        private val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        private val dragHandle: ImageView? = itemView.findViewById(R.id.dragHandle)
        private val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)
        private val quickActionsContainer: View = itemView.findViewById(R.id.quickActionsContainer)
        
        fun bind(item: FavoriteItem) {
            tvName.text = item.name
            tvPath.text = item.path
            
            val context = itemView.context
            
            // Set icon based on type
            ivIcon.setImageResource(when (item.type) {
                "folder" -> R.drawable.ic_folder
                "file" -> R.drawable.ic_file
                "compressed" -> R.drawable.ic_zip
                "image" -> R.drawable.ic_image
                "video" -> R.drawable.ic_video
                "audio" -> R.drawable.ic_audio
                "document" -> R.drawable.ic_document
                else -> R.drawable.ic_bookmark
            })
            
            // Date
            tvDate.text = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date(item.dateAdded))
            
            // Type
            tvType.text = when (item.type) {
                "folder" -> context.getString(R.string.folder)
                "file" -> context.getString(R.string.file)
                "compressed" -> context.getString(R.string.compressed)
                "image" -> context.getString(R.string.image)
                "video" -> context.getString(R.string.video)
                "audio" -> context.getString(R.string.audio)
                "document" -> context.getString(R.string.document)
                else -> context.getString(R.string.favorite)
            }
            
            // Size (if available)
            if (item.size > 0) {
                tvSize.text = FileUtils.formatFileSize(item.size)
                tvSize.visibility = View.VISIBLE
            } else {
                tvSize.visibility = View.GONE
            }
            
            // Quick actions
            quickActionsContainer.visibility = if (showQuickActions && !isSelectionMode) 
                View.VISIBLE else View.GONE
            
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
                quickActionsContainer.visibility = View.GONE
            } else {
                selectionOverlay.visibility = View.GONE
                ivSelected.visibility = View.GONE
                if (showQuickActions) {
                    quickActionsContainer.visibility = View.VISIBLE
                }
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<FavoriteItem>() {
        override fun areItemsTheSame(oldItem: FavoriteItem, newItem: FavoriteItem): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: FavoriteItem, newItem: FavoriteItem): Boolean {
            return oldItem == newItem
        }
    }
}
