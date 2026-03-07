package com.exory550.exoryfilemanager.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Abstract base adapter for RecyclerView items with common functionality
 * like selection mode, click handling, and view binding
 */
abstract class ItemAbstractAdapter<T, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffCallback) {

    protected var onItemClickListener: ((T) -> Unit)? = null
    protected var onItemLongClickListener: ((T) -> Boolean)? = null
    protected var onSelectionChangedListener: ((Int) -> Unit)? = null
    
    protected var selectedItems = mutableSetOf<T>()
    protected var isSelectionMode = false
    protected var isMultiSelectEnabled = true
    
    /**
     * Set click listener for items
     */
    fun setOnItemClickListener(listener: (T) -> Unit) {
        onItemClickListener = listener
    }
    
    /**
     * Set long click listener for items
     */
    fun setOnItemLongClickListener(listener: (T) -> Boolean) {
        onItemLongClickListener = listener
    }
    
    /**
     * Set listener for selection changes
     */
    fun setOnSelectionChangedListener(listener: (Int) -> Unit) {
        onSelectionChangedListener = listener
    }
    
    /**
     * Enable or disable selection mode
     */
    fun setSelectionMode(enabled: Boolean) {
        if (isSelectionMode != enabled) {
            isSelectionMode = enabled
            if (!enabled) {
                clearSelection()
            }
            notifyDataSetChanged()
        }
    }
    
    /**
     * Toggle selection of an item
     */
    fun toggleSelection(item: T) {
        if (!isMultiSelectEnabled && !isSelectionMode) {
            isSelectionMode = true
        }
        
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            if (!isMultiSelectEnabled) {
                selectedItems.clear()
            }
            selectedItems.add(item)
        }
        
        onSelectionChangedListener?.invoke(selectedItems.size)
        notifyItemChanged(currentList.indexOf(item))
        
        if (selectedItems.isEmpty() && isMultiSelectEnabled) {
            isSelectionMode = false
            notifyDataSetChanged()
        }
    }
    
    /**
     * Select all items
     */
    fun selectAll() {
        if (!isMultiSelectEnabled) return
        
        selectedItems.clear()
        selectedItems.addAll(currentList)
        isSelectionMode = true
        onSelectionChangedListener?.invoke(selectedItems.size)
        notifyDataSetChanged()
    }
    
    /**
     * Clear all selections
     */
    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        onSelectionChangedListener?.invoke(0)
        notifyDataSetChanged()
    }
    
    /**
     * Get list of selected items
     */
    fun getSelectedItems(): List<T> = selectedItems.toList()
    
    /**
     * Check if an item is selected
     */
    fun isSelected(item: T): Boolean = selectedItems.contains(item)
    
    /**
     * Get number of selected items
     */
    fun getSelectedCount(): Int = selectedItems.size
    
    /**
     * Enable or disable multi-select
     */
    fun setMultiSelectEnabled(enabled: Boolean) {
        isMultiSelectEnabled = enabled
        if (!enabled && selectedItems.size > 1) {
            val firstItem = selectedItems.firstOrNull()
            selectedItems.clear()
            firstItem?.let { selectedItems.add(it) }
            notifyDataSetChanged()
        }
    }
    
    /**
     * Abstract method to create view holder
     */
    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    
    /**
     * Abstract method to bind view holder
     */
    abstract override fun onBindViewHolder(holder: VH, position: Int)
    
    /**
     * Abstract base view holder with common functionality
     */
    abstract class ItemAbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        protected var isSelected = false
        
        /**
         * Bind item data to view
         */
        abstract fun bind(item: Any, position: Int)
        
        /**
         * Update selection state
         */
        open fun setSelected(selected: Boolean) {
            isSelected = selected
            itemView.isSelected = selected
        }
        
        /**
         * Get the view for animations
         */
        open fun getAnimationView(): View = itemView
    }
}

/**
 * Default diff callback implementation for items with ID
 */
abstract class IdDiffCallback<T : Any> : DiffUtil.ItemCallback<T>() {
    
    abstract fun getId(item: T): Long
    
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return getId(oldItem) == getId(newItem)
    }
    
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}

/**
 * Default diff callback implementation for items with stable ID
 */
class DefaultDiffCallback<T : Any> : DiffUtil.ItemCallback<T>() {
    
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem === newItem
    }
    
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}
