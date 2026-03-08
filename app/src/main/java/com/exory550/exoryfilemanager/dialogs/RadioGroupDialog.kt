package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textview.MaterialTextView

class RadioGroupDialog(
    context: Context,
    private val config: Config
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    data class Config(
        val title: String? = null,
        val titleRes: Int? = null,
        val message: String? = null,
        val messageRes: Int? = null,
        val items: List<RadioItem>,
        val selectedItemId: Long? = null,
        val showSearch: Boolean = false,
        val showIcons: Boolean = true,
        val showDescriptions: Boolean = true,
        val singleChoice: Boolean = true,
        val positiveText: String = context.getString(R.string.ok),
        val positiveTextRes: Int? = null,
        val negativeText: String? = context.getString(R.string.cancel),
        val negativeTextRes: Int? = null,
        val neutralText: String? = null,
        val neutralTextRes: Int? = null,
        val onItemSelected: (RadioItem) -> Unit,
        val onPositive: (() -> Unit)? = null,
        val onNegative: (() -> Unit)? = null,
        val onNeutral: (() -> Unit)? = null
    )

    data class RadioItem(
        val id: Long,
        val title: String,
        val description: String? = null,
        val iconRes: Int? = null,
        val iconDrawable: android.graphics.drawable.Drawable? = null,
        val isEnabled: Boolean = true,
        val tag: Any? = null
    )

    private lateinit var binding: View
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPositive: MaterialButton
    private lateinit var btnNegative: MaterialButton
    private lateinit var btnNeutral: MaterialButton
    private lateinit var btnClose: ImageButton
    
    private var adapter: RadioAdapter? = null
    private var selectedItem: RadioItem? = null
    private var filteredItems: List<RadioItem> = config.items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        setupViews()
        setupRecyclerView()
        setupSearch()
        setupListeners()
        updateContent()
    }

    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_radio_group, null)
        setContentView(binding)

        tvTitle = binding.findViewById(R.id.tvTitle)
        tvMessage = binding.findViewById(R.id.tvMessage)
        searchView = binding.findViewById(R.id.searchView)
        recyclerView = binding.findViewById(R.id.recyclerView)
        btnPositive = binding.findViewById(R.id.btnPositive)
        btnNegative = binding.findViewById(R.id.btnNegative)
        btnNeutral = binding.findViewById(R.id.btnNeutral)
        btnClose = binding.findViewById(R.id.btnClose)

        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )

        // Configure search visibility
        searchView.visibility = if (config.showSearch) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = RadioAdapter(
            items = filteredItems,
            selectedItemId = config.selectedItemId,
            showIcons = config.showIcons,
            showDescriptions = config.showDescriptions,
            onItemClick = { item ->
                if (config.singleChoice) {
                    selectedItem = item
                    adapter?.setSelectedItem(item.id)
                }
                config.onItemSelected(item)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterItems(newText ?: "")
                return true
            }
        })
    }

    private fun setupListeners() {
        btnPositive.setOnClickListener {
            if (config.singleChoice && selectedItem != null) {
                config.onItemSelected(selectedItem!!)
            }
            config.onPositive?.invoke()
            dismiss()
        }

        btnNegative.setOnClickListener {
            config.onNegative?.invoke()
            dismiss()
        }

        btnNeutral.setOnClickListener {
            config.onNeutral?.invoke()
            dismiss()
        }

        btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun updateContent() {
        // Set title
        when {
            config.titleRes != null -> tvTitle.setText(config.titleRes)
            !config.title.isNullOrBlank() -> tvTitle.text = config.title
            else -> tvTitle.visibility = View.GONE
        }

        // Set message
        when {
            config.messageRes != null -> {
                tvMessage.setText(config.messageRes)
                tvMessage.visibility = View.VISIBLE
            }
            !config.message.isNullOrBlank() -> {
                tvMessage.text = config.message
                tvMessage.visibility = View.VISIBLE
            }
            else -> tvMessage.visibility = View.GONE
        }

        // Set buttons
        when {
            config.positiveTextRes != null -> btnPositive.setText(config.positiveTextRes)
            else -> btnPositive.text = config.positiveText
        }

        when {
            config.negativeTextRes != null -> btnNegative.setText(config.negativeTextRes)
            config.negativeText != null -> btnNegative.text = config.negativeText
            else -> btnNegative.visibility = View.GONE
        }

        when {
            config.neutralTextRes != null -> btnNeutral.setText(config.neutralTextRes)
            config.neutralText != null -> btnNeutral.text = config.neutralText
            else -> btnNeutral.visibility = View.GONE
        }
    }

    private fun filterItems(query: String) {
        filteredItems = if (query.isEmpty()) {
            config.items
        } else {
            config.items.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description?.contains(query, ignoreCase = true) == true
            }
        }
        adapter?.updateItems(filteredItems)
    }

    class RadioAdapter(
        private var items: List<RadioItem>,
        private var selectedItemId: Long?,
        private val showIcons: Boolean,
        private val showDescriptions: Boolean,
        private val onItemClick: (RadioItem) -> Unit
    ) : RecyclerView.Adapter<RadioAdapter.ViewHolder>() {

        fun updateItems(newItems: List<RadioItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        fun setSelectedItem(id: Long) {
            val oldSelected = selectedItemId
            selectedItemId = id
            
            oldSelected?.let {
                val oldPosition = items.indexOfFirst { item -> item.id == it }
                if (oldPosition != -1) {
                    notifyItemChanged(oldPosition)
                }
            }
            
            val newPosition = items.indexOfFirst { item -> item.id == id }
            if (newPosition != -1) {
                notifyItemChanged(newPosition)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_radio_group, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val radioButton: MaterialRadioButton = itemView.findViewById(R.id.radioButton)
            private val iconView: ImageView = itemView.findViewById(R.id.iconView)
            private val titleText: MaterialTextView = itemView.findViewById(R.id.titleText)
            private val descriptionText: MaterialTextView = itemView.findViewById(R.id.descriptionText)
            private val divider: View = itemView.findViewById(R.id.divider)

            init {
                itemView.setOnClickListener {
                    val item = items[adapterPosition]
                    if (item.isEnabled) {
                        radioButton.isChecked = true
                        onItemClick(item)
                    }
                }
            }

            fun bind(item: RadioItem) {
                titleText.text = item.title
                
                // Description
                if (showDescriptions && !item.description.isNullOrBlank()) {
                    descriptionText.text = item.description
                    descriptionText.visibility = View.VISIBLE
                } else {
                    descriptionText.visibility = View.GONE
                }

                // Icon
                if (showIcons) {
                    when {
                        item.iconRes != null -> {
                            iconView.setImageResource(item.iconRes)
                            iconView.visibility = View.VISIBLE
                        }
                        item.iconDrawable != null -> {
                            iconView.setImageDrawable(item.iconDrawable)
                            iconView.visibility = View.VISIBLE
                        }
                        else -> iconView.visibility = View.GONE
                    }
                } else {
                    iconView.visibility = View.GONE
                }

                // Selection state
                radioButton.isChecked = item.id == selectedItemId
                radioButton.isEnabled = item.isEnabled
                itemView.isEnabled = item.isEnabled

                // Alpha for disabled items
                itemView.alpha = if (item.isEnabled) 1.0f else 0.5f

                // Hide divider for last item
                divider.visibility = if (adapterPosition == items.size - 1) View.GONE else View.VISIBLE
            }
        }
    }

    class Builder(private val context: Context) {
        private var title: String? = null
        private var titleRes: Int? = null
        private var message: String? = null
        private var messageRes: Int? = null
        private val items = mutableListOf<RadioItem>()
        private var selectedItemId: Long? = null
        private var showSearch: Boolean = false
        private var showIcons: Boolean = true
        private var showDescriptions: Boolean = true
        private var singleChoice: Boolean = true
        private var positiveText: String = context.getString(R.string.ok)
        private var positiveTextRes: Int? = null
        private var negativeText: String? = context.getString(R.string.cancel)
        private var negativeTextRes: Int? = null
        private var neutralText: String? = null
        private var neutralTextRes: Int? = null
        private var onItemSelected: (RadioItem) -> Unit = {}
        private var onPositive: (() -> Unit)? = null
        private var onNegative: (() -> Unit)? = null
        private var onNeutral: (() -> Unit)? = null

        fun setTitle(title: String) = apply { this.title = title }
        fun setTitleRes(titleRes: Int) = apply { this.titleRes = titleRes }
        fun setMessage(message: String) = apply { this.message = message }
        fun setMessageRes(messageRes: Int) = apply { this.messageRes = messageRes }
        fun addItem(item: RadioItem) = apply { items.add(item) }
        fun addItems(items: List<RadioItem>) = apply { this.items.addAll(items) }
        fun setSelectedItemId(id: Long?) = apply { this.selectedItemId = id }
        fun setShowSearch(show: Boolean) = apply { this.showSearch = show }
        fun setShowIcons(show: Boolean) = apply { this.showIcons = show }
        fun setShowDescriptions(show: Boolean) = apply { this.showDescriptions = show }
        fun setSingleChoice(single: Boolean) = apply { this.singleChoice = single }
        fun setPositiveText(text: String) = apply { this.positiveText = text }
        fun setPositiveTextRes(res: Int) = apply { this.positiveTextRes = res }
        fun setNegativeText(text: String?) = apply { this.negativeText = text }
        fun setNegativeTextRes(res: Int?) = apply { this.negativeTextRes = res }
        fun setNeutralText(text: String?) = apply { this.neutralText = text }
        fun setNeutralTextRes(res: Int?) = apply { this.neutralTextRes = res }
        fun setOnItemSelected(listener: (RadioItem) -> Unit) = apply { this.onItemSelected = listener }
        fun setOnPositive(listener: () -> Unit) = apply { this.onPositive = listener }
        fun setOnNegative(listener: () -> Unit) = apply { this.onNegative = listener }
        fun setOnNeutral(listener: () -> Unit) = apply { this.onNeutral = listener }

        fun build(): Config {
            return Config(
                title = title,
                titleRes = titleRes,
                message = message,
                messageRes = messageRes,
                items = items,
                selectedItemId = selectedItemId,
                showSearch = showSearch,
                showIcons = showIcons,
                showDescriptions = showDescriptions,
                singleChoice = singleChoice,
                positiveText = positiveText,
                positiveTextRes = positiveTextRes,
                negativeText = negativeText,
                negativeTextRes = negativeTextRes,
                neutralText = neutralText,
                neutralTextRes = neutralTextRes,
                onItemSelected = onItemSelected,
                onPositive = onPositive,
                onNegative = onNegative,
                onNeutral = onNeutral
            )
        }

        fun show() {
            RadioGroupDialog(context, build()).show()
        }
    }

    companion object {
        fun show(context: Context, config: Config.() -> Unit) {
            val builder = Builder(context)
            config.invoke(builder)
            builder.show()
        }

        fun showSimple(
            context: Context,
            title: String,
            items: List<String>,
            selectedIndex: Int = -1,
            onItemSelected: (Int, String) -> Unit
        ) {
            val radioItems = items.mapIndexed { index, item ->
                RadioItem(
                    id = index.toLong(),
                    title = item
                )
            }
            
            Builder(context)
                .setTitle(title)
                .addItems(radioItems)
                .setSelectedItemId(if (selectedIndex >= 0) selectedIndex.toLong() else null)
                .setOnItemSelected { item ->
                    onItemSelected(item.id.toInt(), item.title)
                }
                .show()
        }

        fun showWithIcons(
            context: Context,
            title: String,
            items: List<Pair<String, Int>>,
            selectedIndex: Int = -1,
            onItemSelected: (Int, String) -> Unit
        ) {
            val radioItems = items.mapIndexed { index, (title, icon) ->
                RadioItem(
                    id = index.toLong(),
                    title = title,
                    iconRes = icon
                )
            }
            
            Builder(context)
                .setTitle(title)
                .addItems(radioItems)
                .setSelectedItemId(if (selectedIndex >= 0) selectedIndex.toLong() else null)
                .setShowIcons(true)
                .setOnItemSelected { item ->
                    onItemSelected(item.id.toInt(), item.title)
                }
                .show()
        }
    }
}
