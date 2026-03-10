package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.utils.FileUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class RenameItemsDialog(
    context: Context,
    private val files: List<ExoryFileItem>,
    private val onItemsRenamed: (List<RenameOperation>) -> Unit,
    private val onCancel: (() -> Unit)? = null
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog_FullScreen) {

    data class RenameOperation(
        val originalFile: ExoryExoryFileItem,
        val newName: String,
        val newPath: String
    )

    data class RenamePattern(
        var find: String = "",
        var replace: String = "",
        var caseSensitive: Boolean = false,
        var useRegex: Boolean = false
    )

    private lateinit var binding: View
    private lateinit var tabHost: TabHost
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RenamePreviewAdapter
    private lateinit var tvSelectedCount: TextView
    private lateinit var btnApply: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnClose: ImageButton
    private lateinit var progressBar: ProgressBar
    
    // Find/Replace tab
    private lateinit var etFind: TextInputEditText
    private lateinit var etReplace: TextInputEditText
    private lateinit var cbCaseSensitive: CheckBox
    private lateinit var cbUseRegex: CheckBox
    private lateinit var btnPreview: MaterialButton
    
    // Add/Remove tab
    private lateinit var spinnerPosition: Spinner
    private lateinit var etText: TextInputEditText
    private lateinit var rgAddRemove: RadioGroup
    private lateinit var rbPrefix: RadioButton
    private lateinit var rbSuffix: RadioButton
    private lateinit var btnAddRemovePreview: MaterialButton
    
    // Numbering tab
    private lateinit var etStartNumber: EditText
    private lateinit var etStep: EditText
    private lateinit var etDigits: EditText
    private lateinit var etSeparator: EditText
    private lateinit var cbUseParentheses: CheckBox
    private lateinit var spinnerPosition2: Spinner
    private lateinit var btnNumberingPreview: MaterialButton
    
    // Case conversion tab
    private lateinit var rgCase: RadioGroup
    private lateinit var rbLowerCase: RadioButton
    private lateinit var rbUpperCase: RadioButton
    private lateinit var rbTitleCase: RadioButton
    private lateinit var rbSentenceCase: RadioButton
    private lateinit var rbCapitalize: RadioButton
    private lateinit var btnCasePreview: MaterialButton
    
    private var previewResults = mutableListOf<RenameOperation>()
    private var selectedOperations = mutableSetOf<RenameOperation>()
    private var isSelectionMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        setupViews()
        setupTabs()
        setupRecyclerView()
        setupListeners()
        updateSelectedCount()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_rename_items, null)
        setContentView(binding)
        
        tabHost = binding.findViewById(R.id.tabHost)
        recyclerView = binding.findViewById(R.id.recyclerView)
        tvSelectedCount = binding.findViewById(R.id.tvSelectedCount)
        btnApply = binding.findViewById(R.id.btnApply)
        btnCancel = binding.findViewById(R.id.btnCancel)
        btnClose = binding.findViewById(R.id.btnClose)
        progressBar = binding.findViewById(R.id.progressBar)
        
        // Find/Replace tab views
        etFind = binding.findViewById(R.id.etFind)
        etReplace = binding.findViewById(R.id.etReplace)
        cbCaseSensitive = binding.findViewById(R.id.cbCaseSensitive)
        cbUseRegex = binding.findViewById(R.id.cbUseRegex)
        btnPreview = binding.findViewById(R.id.btnPreview)
        
        // Add/Remove tab views
        spinnerPosition = binding.findViewById(R.id.spinnerPosition)
        etText = binding.findViewById(R.id.etText)
        rgAddRemove = binding.findViewById(R.id.rgAddRemove)
        rbPrefix = binding.findViewById(R.id.rbPrefix)
        rbSuffix = binding.findViewById(R.id.rbSuffix)
        btnAddRemovePreview = binding.findViewById(R.id.btnAddRemovePreview)
        
        // Numbering tab views
        etStartNumber = binding.findViewById(R.id.etStartNumber)
        etStep = binding.findViewById(R.id.etStep)
        etDigits = binding.findViewById(R.id.etDigits)
        etSeparator = binding.findViewById(R.id.etSeparator)
        cbUseParentheses = binding.findViewById(R.id.cbUseParentheses)
        spinnerPosition2 = binding.findViewById(R.id.spinnerPosition2)
        btnNumberingPreview = binding.findViewById(R.id.btnNumberingPreview)
        
        // Case conversion tab views
        rgCase = binding.findViewById(R.id.rgCase)
        rbLowerCase = binding.findViewById(R.id.rbLowerCase)
        rbUpperCase = binding.findViewById(R.id.rbUpperCase)
        rbTitleCase = binding.findViewById(R.id.rbTitleCase)
        rbSentenceCase = binding.findViewById(R.id.rbSentenceCase)
        rbCapitalize = binding.findViewById(R.id.rbCapitalize)
        btnCasePreview = binding.findViewById(R.id.btnCasePreview)
        
        // Set dialog window properties
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // Setup spinners
        setupSpinners()
    }
    
    private fun setupSpinners() {
        val positions = arrayOf(
            context.getString(R.string.position_start),
            context.getString(R.string.position_end),
            context.getString(R.string.position_before_extension)
        )
        
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, positions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        spinnerPosition.adapter = adapter
        spinnerPosition2.adapter = adapter
    }
    
    private fun setupTabs() {
        tabHost.setup()
        
        tabHost.addTab(tabHost.newTabSpec("find_replace")
            .setIndicator(context.getString(R.string.find_replace))
            .setContent(R.id.tabFindReplace))
            
        tabHost.addTab(tabHost.newTabSpec("add_remove")
            .setIndicator(context.getString(R.string.add_remove))
            .setContent(R.id.tabAddRemove))
            
        tabHost.addTab(tabHost.newTabSpec("numbering")
            .setIndicator(context.getString(R.string.numbering))
            .setContent(R.id.tabNumbering))
            
        tabHost.addTab(tabHost.newTabSpec("case")
            .setIndicator(context.getString(R.string.case_conversion))
            .setContent(R.id.tabCase))
    }
    
    private fun setupRecyclerView() {
        adapter = RenamePreviewAdapter(
            items = previewResults,
            onItemChecked = { operation, isChecked ->
                if (isChecked) {
                    selectedOperations.add(operation)
                } else {
                    selectedOperations.remove(operation)
                }
                updateSelectedCount()
            },
            onSelectAll = {
                selectedOperations.clear()
                selectedOperations.addAll(previewResults)
                adapter.setAllSelected(true)
                updateSelectedCount()
            },
            onSelectNone = {
                selectedOperations.clear()
                adapter.setAllSelected(false)
                updateSelectedCount()
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun setupListeners() {
        btnApply.setOnClickListener {
            if (selectedOperations.isNotEmpty()) {
                onItemsRenamed.invoke(selectedOperations.toList())
                dismiss()
            }
        }
        
        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
        
        btnClose.setOnClickListener {
            dismiss()
        }
        
        // Find/Replace preview
        btnPreview.setOnClickListener {
            val pattern = RenamePattern(
                find = etFind.text.toString(),
                replace = etReplace.text.toString(),
                caseSensitive = cbCaseSensitive.isChecked,
                useRegex = cbUseRegex.isChecked
            )
            applyFindReplace(pattern)
        }
        
        // Add/Remove preview
        btnAddRemovePreview.setOnClickListener {
            val text = etText.text.toString()
            val position = spinnerPosition.selectedItemPosition
            val isPrefix = rbPrefix.isChecked
            applyAddRemove(text, position, isPrefix)
        }
        
        // Numbering preview
        btnNumberingPreview.setOnClickListener {
            val start = etStartNumber.text.toString().toIntOrNull() ?: 1
            val step = etStep.text.toString().toIntOrNull() ?: 1
            val digits = etDigits.text.toString().toIntOrNull() ?: 0
            val separator = etSeparator.text.toString()
            val useParentheses = cbUseParentheses.isChecked
            val position = spinnerPosition2.selectedItemPosition
            applyNumbering(start, step, digits, separator, useParentheses, position)
        }
        
        // Case conversion preview
        btnCasePreview.setOnClickListener {
            val caseType = when (rgCase.checkedRadioButtonId) {
                R.id.rbLowerCase -> "lower"
                R.id.rbUpperCase -> "upper"
                R.id.rbTitleCase -> "title"
                R.id.rbSentenceCase -> "sentence"
                R.id.rbCapitalize -> "capitalize"
                else -> "lower"
            }
            applyCaseConversion(caseType)
        }
    }
    
    private fun applyFindReplace(pattern: RenamePattern) {
        showProgress(true)
        
        val results = files.mapNotNull { file ->
            val newName = if (pattern.useRegex) {
                applyRegexReplace(file.name, pattern)
            } else {
                applySimpleReplace(file.name, pattern)
            }
            
            if (newName != file.name) {
                RenameOperation(
                    originalFile = file,
                    newName = newName,
                    newPath = File(file.path).parent + File.separator + newName
                )
            } else {
                null
            }
        }
        
        updatePreview(results)
        showProgress(false)
    }
    
    private fun applySimpleReplace(name: String, pattern: RenamePattern): String {
        return if (pattern.caseSensitive) {
            name.replace(pattern.find, pattern.replace)
        } else {
            name.replace(Regex(pattern.find, RegexOption.IGNORE_CASE), pattern.replace)
        }
    }
    
    private fun applyRegexReplace(name: String, pattern: RenamePattern): String {
        return try {
            val regex = if (pattern.caseSensitive) {
                Regex(pattern.find)
            } else {
                Regex(pattern.find, RegexOption.IGNORE_CASE)
            }
            regex.replace(name, pattern.replace)
        } catch (e: Exception) {
            name
        }
    }
    
    private fun applyAddRemove(text: String, position: Int, isPrefix: Boolean) {
        showProgress(true)
        
        val results = files.map { file ->
            val newName = when (position) {
                0 -> if (isPrefix) text + file.name else file.name + text
                1 -> if (isPrefix) file.name + text else text + file.name
                2 -> {
                    val dotIndex = file.name.lastIndexOf('.')
                    if (dotIndex > 0 && !file.isDirectory) {
                        val name = file.name.substring(0, dotIndex)
                        val ext = file.name.substring(dotIndex)
                        if (isPrefix) {
                            text + name + ext
                        } else {
                            name + text + ext
                        }
                    } else {
                        if (isPrefix) text + file.name else file.name + text
                    }
                }
                else -> file.name
            }
            
            RenameOperation(
                originalFile = file,
                newName = newName,
                newPath = File(file.path).parent + File.separator + newName
            )
        }
        
        updatePreview(results)
        showProgress(false)
    }
    
    private fun applyNumbering(start: Int, step: Int, digits: Int, separator: String, 
                               useParentheses: Boolean, position: Int) {
        showProgress(true)
        
        var counter = start
        val results = files.mapIndexed { index, file ->
            val number = counter + (index * step)
            val numberStr = when {
                digits > 0 -> String.format("%0${digits}d", number)
                useParentheses -> "($number)"
                else -> number.toString()
            }
            
            val newName = when (position) {
                0 -> numberStr + separator + file.name
                1 -> file.name + separator + numberStr
                2 -> {
                    val dotIndex = file.name.lastIndexOf('.')
                    if (dotIndex > 0 && !file.isDirectory) {
                        val name = file.name.substring(0, dotIndex)
                        val ext = file.name.substring(dotIndex)
                        name + separator + numberStr + ext
                    } else {
                        file.name + separator + numberStr
                    }
                }
                else -> file.name
            }
            
            RenameOperation(
                originalFile = file,
                newName = newName,
                newPath = File(file.path).parent + File.separator + newName
            )
        }
        
        updatePreview(results)
        showProgress(false)
    }
    
    private fun applyCaseConversion(caseType: String) {
        showProgress(true)
        
        val results = files.map { file ->
            val newName = when (caseType) {
                "lower" -> file.name.lowercase()
                "upper" -> file.name.uppercase()
                "title" -> file.name.split(" ").joinToString(" ") { 
                    it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                "sentence" -> {
                    val dotIndex = file.name.lastIndexOf('.')
                    if (dotIndex > 0 && !file.isDirectory) {
                        val name = file.name.substring(0, dotIndex)
                        val ext = file.name.substring(dotIndex)
                        name.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() } + ext.lowercase()
                    } else {
                        file.name.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
                    }
                }
                "capitalize" -> file.name.split(" ").joinToString(" ") { 
                    it.lowercase().replaceFirstChar { it.uppercase() }
                }
                else -> file.name
            }
            
            RenameOperation(
                originalFile = file,
                newName = newName,
                newPath = File(file.path).parent + File.separator + newName
            )
        }
        
        updatePreview(results)
        showProgress(false)
    }
    
    private fun updatePreview(results: List<RenameOperation>) {
        previewResults.clear()
        previewResults.addAll(results)
        adapter.updateItems(previewResults)
        
        // Auto-select all by default
        selectedOperations.clear()
        selectedOperations.addAll(results)
        adapter.setAllSelected(true)
        updateSelectedCount()
    }
    
    private fun updateSelectedCount() {
        tvSelectedCount.text = context.getString(
            R.string.selected_count, 
            selectedOperations.size
        )
        btnApply.isEnabled = selectedOperations.isNotEmpty()
    }
    
    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnPreview.isEnabled = !show
        btnAddRemovePreview.isEnabled = !show
        btnNumberingPreview.isEnabled = !show
        btnCasePreview.isEnabled = !show
    }
    
    inner class RenamePreviewAdapter(
        private var items: List<RenameOperation>,
        private val onItemChecked: (RenameOperation, Boolean) -> Unit,
        private val onSelectAll: () -> Unit,
        private val onSelectNone: () -> Unit
    ) : RecyclerView.Adapter<RenamePreviewAdapter.ViewHolder>() {
        
        private var allSelected = true
        
        fun updateItems(newItems: List<RenameOperation>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        fun setAllSelected(selected: Boolean) {
            allSelected = selected
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rename_preview, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
            private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
            private val tvOriginalName: TextView = itemView.findViewById(R.id.tvOriginalName)
            private val tvNewName: TextView = itemView.findViewById(R.id.tvNewName)
            private val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)
            
            fun bind(operation: RenameOperation) {
                tvOriginalName.text = operation.originalFile.name
                tvNewName.text = operation.newName
                
                ivIcon.setImageResource(
                    if (operation.originalFile.isDirectory) R.drawable.ic_folder 
                    else R.drawable.ic_file
                )
                
                cbSelect.isChecked = operation in selectedOperations
                
                cbSelect.setOnCheckedChangeListener { _, isChecked ->
                    onItemChecked(operation, isChecked)
                }
                
                itemView.setOnClickListener {
                    cbSelect.isChecked = !cbSelect.isChecked
                }
                
                // Show warning if file already exists
                val newFile = File(operation.newPath)
                if (newFile.exists() && operation.originalFile.path != operation.newPath) {
                    tvNewName.setTextColor(ContextCompat.getColor(context, R.color.error_color))
                } else {
                    tvNewName.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                }
            }
        }
    }
    
    companion object {
        fun show(
            context: Context,
            files: List<ExoryFileItem>,
            onItemsRenamed: (List<RenameOperation>) -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            RenameItemsDialog(context, files, onItemsRenamed, onCancel).show()
        }
    }
}
