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
import com.exory550.exoryfilemanager.models.GenericItem
import com.exory550.exoryfilemanager.utils.ViewBindingUtils
import java.io.File

/**
 * Generic RecyclerView adapter that can handle different item types
 * with view binding and click listeners
 */
class RecyclerViewAdapter<T : Any>(
    private val layoutRes: Int,
    private val bindHolder: (View, T, Int) -> Unit,
    private val onItemClick: ((T) -> Unit)? = null,
    private val onItemLongClick: ((T) -> Boolean)? = null
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder<T>>() {

    private var items: List<T> = emptyList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view, bindHolder, onItemClick, onItemLongClick)
    }
    
    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.bind(items[position], position)
    }
    
    override fun getItemCount(): Int = items.size
    
    fun submitList(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    class ViewHolder<T>(
        itemView: View,
        private val bindHolder: (View, T, Int) -> Unit,
        private val onItemClick: ((T) -> Unit)?,
        private val onItemLongClick: ((T) -> Boolean)?
    ) : RecyclerView.ViewHolder(itemView) {
        
        private var currentItem: T? = null
        
        init {
            itemView.setOnClickListener {
                currentItem?.let { item ->
                    onItemClick?.invoke(item)
                }
            }
            
            itemView.setOnLongClickListener {
                currentItem?.let { item ->
                    onItemLongClick?.invoke(item) ?: false
                } ?: false
            }
        }
        
        fun bind(item: T, position: Int) {
            currentItem = item
            bindHolder(itemView, item, position)
        }
    }
}

/**
 * Adapter with multiple view types support
 */
class MultiTypeAdapter : RecyclerView.Adapter<MultiTypeAdapter.ViewHolder>() {
    
    private val items = mutableListOf<Any>()
    private val viewTypeMap = mutableMapOf<Int, ViewTypeConfig<Any>>()
    
    fun registerViewType(type: Int, config: ViewTypeConfig<Any>) {
        viewTypeMap[type] = config
    }
    
    fun submitList(newItems: List<Any>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    
    override fun getItemViewType(position: Int): Int {
        return items[position].hashCode() // Simplified - implement proper type resolution
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val config = viewTypeMap[viewType]
            ?: throw IllegalArgumentException("No config registered for view type $viewType")
        
        val view = LayoutInflater.from(parent.context).inflate(config.layoutRes, parent, false)
        return ViewHolder(view, config)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    class ViewHolder(
        itemView: View,
        private val config: ViewTypeConfig<Any>
    ) : RecyclerView.ViewHolder(itemView) {
        
        fun bind(item: Any) {
            config.bindHolder(itemView, item)
            
            itemView.setOnClickListener {
                config.onItemClick?.invoke(item)
            }
            
            itemView.setOnLongClickListener {
                config.onItemLongClick?.invoke(item) ?: false
            }
        }
    }
    
    data class ViewTypeConfig<T>(
        val layoutRes: Int,
        val bindHolder: (View, T) -> Unit,
        val onItemClick: ((T) -> Unit)? = null,
        val onItemLongClick: ((T) -> Boolean)? = null
    )
}

/**
 * Paging adapter with loading states
 */
class PagingAdapter<T : Any>(
    private val layoutRes: Int,
    private val loadingLayoutRes: Int,
    private val bindHolder: (View, T, Int) -> Unit,
    private val bindLoadingHolder: (View) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private companion object {
        const val TYPE_ITEM = 0
        const val TYPE_LOADING = 1
    }
    
    private var items: List<T> = emptyList()
    private var isLoading = false
    
    override fun getItemViewType(position: Int): Int {
        return if (position == items.size && isLoading) TYPE_LOADING else TYPE_ITEM
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(loadingLayoutRes, parent, false)
                LoadingViewHolder(view, bindLoadingHolder)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(layoutRes, parent, false)
                ItemViewHolder(view, bindHolder)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = items[position]
                holder.bind(item, position)
            }
            is LoadingViewHolder -> {
                holder.bind()
            }
        }
    }
    
    override fun getItemCount(): Int = items.size + if (isLoading) 1 else 0
    
    fun submitList(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            if (loading) {
                notifyItemInserted(items.size)
            } else {
                notifyItemRemoved(items.size)
            }
        }
    }
    
    class ItemViewHolder<T>(
        itemView: View,
        private val bindHolder: (View, T, Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        fun bind(item: T, position: Int) {
            bindHolder(itemView, item, position)
        }
    }
    
    class LoadingViewHolder(
        itemView: View,
        private val bindHolder: (View) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        fun bind() {
            bindHolder(itemView)
        }
    }
}

/**
 * Sectioned adapter with headers
 */
class SectionedAdapter(
    private val headerLayoutRes: Int,
    private val itemLayoutRes: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }
    
    private val sections = mutableListOf<Section>()
    
    fun setSections(newSections: List<Section>) {
        sections.clear()
        sections.addAll(newSections)
        notifyDataSetChanged()
    }
    
    override fun getItemViewType(position: Int): Int {
        var currentPos = position
        sections.forEach { section ->
            if (currentPos == 0) return TYPE_HEADER
            currentPos--
            
            if (currentPos < section.items.size) return TYPE_ITEM
            currentPos -= section.items.size
        }
        return TYPE_ITEM
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(headerLayoutRes, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(itemLayoutRes, parent, false)
                ItemViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = findHeaderForPosition(position)
                holder.bind(header)
            }
            is ItemViewHolder -> {
                val item = findItemForPosition(position)
                holder.bind(item)
            }
        }
    }
    
    override fun getItemCount(): Int {
        return sections.sumOf { 1 + it.items.size }
    }
    
    private fun findHeaderForPosition(position: Int): SectionHeader {
        var currentPos = position
        sections.forEach { section ->
            if (currentPos == 0) {
                return SectionHeader(section.title, section.iconRes)
            }
            currentPos -= (1 + section.items.size)
        }
        throw IllegalArgumentException("No header found at position $position")
    }
    
    private fun findItemForPosition(position: Int): Any {
        var currentPos = position
        sections.forEach { section ->
            currentPos-- // Skip header
            
            if (currentPos < section.items.size) {
                return section.items[currentPos]
            }
            currentPos -= section.items.size
        }
        throw IllegalArgumentException("No item found at position $position")
    }
    
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView? = itemView.findViewById(R.id.tvSectionTitle)
        private val ivIcon: ImageView? = itemView.findViewById(R.id.ivSectionIcon)
        
        fun bind(header: SectionHeader) {
            tvTitle?.text = header.title
            header.iconRes?.let { ivIcon?.setImageResource(it) }
        }
    }
    
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView? = itemView.findViewById(R.id.tvItemTitle)
        private val tvSubtitle: TextView? = itemView.findViewById(R.id.tvItemSubtitle)
        private val ivIcon: ImageView? = itemView.findViewById(R.id.ivItemIcon)
        
        fun bind(item: Any) {
            when (item) {
                is GenericItem -> {
                    tvTitle?.text = item.title
                    tvSubtitle?.text = item.subtitle
                    item.iconRes?.let { ivIcon?.setImageResource(it) }
                    item.imageUrl?.let { url ->
                        Glide.with(itemView.context)
                            .load(url)
                            .placeholder(item.iconRes ?: R.drawable.ic_file)
                            .error(R.drawable.ic_file)
                            .into(ivIcon!!)
                    }
                }
                is File -> {
                    tvTitle?.text = item.name
                    tvSubtitle?.text = item.path
                }
            }
        }
    }
    
    data class Section(
        val title: String,
        val items: List<Any>,
        val iconRes: Int? = null
    )
    
    data class SectionHeader(
        val title: String,
        val iconRes: Int? = null
    )
}
