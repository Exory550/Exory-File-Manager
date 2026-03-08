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
import com.exory550.exoryfilemanager.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CreateNewItemDialog(
    context: Context,
    private val currentPath: String,
    private val existingItems: List<String>,
    private val onItemCreated: (String, Int) -> Unit
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvCurrentPath: TextView
    private lateinit var tabHost: TabHost
    private lateinit var tilItemName: TextInputLayout
    private lateinit var etItemName: TextInputEditText
    private lateinit var rgItemType: RadioGroup
    private lateinit var rbFile: RadioButton
    private lateinit var rbFolder: RadioButton
    private lateinit var rbShortcut: RadioButton
    private lateinit var spinnerFileType: Spinner
    private lateinit var layoutFileType: LinearLayout
    private lateinit var tvValidationMessage: TextView
    private lateinit var btnCreate: Button
    private lateinit var btnCancel: Button
    private lateinit var btnClose: ImageView
    
    companion object {
        const val TYPE_FOLDER = 0
        const val TYPE_FILE = 1
        const val TYPE_SHORTCUT = 2
        
        private const val MAX_NAME_LENGTH = 255
        private val INVALID_CHARS = arrayOf("\\", "/", ":", "*", "?", "\"", "<", ">", "|")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        
        setupViews()
        setupTabs()
        setupSpinners()
        setupListeners()
        setupInitialValues()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_create_new_item, null)
        setContentView(binding)
        
        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvCurrentPath = binding.findViewById(R.id.tvCurrentPath)
        tabHost = binding.findViewById(R.id.tabHost)
        tilItemName = binding.findViewById(R.id.tilItemName)
        etItemName = binding.findViewById(R.id.etItemName)
        rgItemType = binding.findViewById(R.id.rgItemType)
        rbFile = binding.findViewById(R.id.rbFile)
        rbFolder = binding.findViewById(R.id.rbFolder)
        rbShortcut = binding.findViewById(R.id.rbShortcut)
        spinnerFileType = binding.findViewById(R.id.spinnerFileType)
        layoutFileType = binding.findViewById(R.id.layoutFileType)
        tvValidationMessage = binding.findViewById(R.id.tvValidationMessage)
        btnCreate = binding.findViewById(R.id.btnCreate)
        btnCancel = binding.findViewById(R.id.btnCancel)
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
        
        // Set icon color
        ivIcon.setColorFilter(
            ContextCompat.getColor(context, R.color.primary_color),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
    
    private fun setupTabs() {
        tabHost.setup()
        
        // Basic tab
        var spec = tabHost.newTabSpec("basic")
        spec.setContent(R.id.tabBasic)
        spec.setIndicator(context.getString(R.string.basic))
        tabHost.addTab(spec)
        
        // Advanced tab
        spec = tabHost.newTabSpec("advanced")
        spec.setContent(R.id.tabAdvanced)
        spec.setIndicator(context.getString(R.string.advanced))
        tabHost.addTab(spec)
        
        // Template tab
        spec = tabHost.newTabSpec("template")
        spec.setContent(R.id.tabTemplate)
        spec.setIndicator(context.getString(R.string.template))
        tabHost.addTab(spec)
    }
    
    private fun setupSpinners() {
        // File type spinner
        val fileTypes = arrayOf(
            context.getString(R.string.text_file),
            context.getString(R.string.image_file),
            context.getString(R.string.audio_file),
            context.getString(R.string.video_file),
            context.getString(R.string.document_file),
            context.getString(R.string.html_file),
            context.getString(R.string.markdown_file),
            context.getString(R.string.json_file),
            context.getString(R.string.xml_file),
            context.getString(R.string.yaml_file),
            context.getString(R.string.properties_file),
            context.getString(R.string.batch_file),
            context.getString(R.string.shell_script),
            context.getString(R.string.python_script),
            context.getString(R.string.java_source),
            context.getString(R.string.kotlin_source),
            context.getString(R.string.empty_file)
        )
        
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, fileTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFileType.adapter = adapter
    }
    
    private fun setupListeners() {
        btnCreate.setOnClickListener {
            val itemName = etItemName.text.toString().trim()
            val itemType = when (rgItemType.checkedRadioButtonId) {
                R.id.rbFolder -> TYPE_FOLDER
                R.id.rbShortcut -> TYPE_SHORTCUT
                else -> TYPE_FILE
            }
            
            if (validateItemName(itemName, itemType)) {
                onItemCreated.invoke(itemName, itemType)
                dismiss()
            }
        }
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnClose.setOnClickListener {
            dismiss()
        }
        
        rgItemType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbFolder -> {
                    layoutFileType.visibility = View.GONE
                    tilItemName.hint = context.getString(R.string.folder_name)
                }
                R.id.rbFile -> {
                    layoutFileType.visibility = View.VISIBLE
                    tilItemName.hint = context.getString(R.string.file_name)
                }
                R.id.rbShortcut -> {
                    layoutFileType.visibility = View.GONE
                    tilItemName.hint = context.getString(R.string.shortcut_name)
                }
            }
            validateItemName(etItemName.text.toString(), getSelectedType())
        }
        
        etItemName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validateItemName(s.toString(), getSelectedType())
            }
        })
        
        spinnerFileType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateFileNameExtension(position)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        etItemName.setOnEditorActionListener { _, _, _ ->
            val itemName = etItemName.text.toString().trim()
            val itemType = getSelectedType()
            
            if (validateItemName(itemName, itemType)) {
                onItemCreated.invoke(itemName, itemType)
                dismiss()
                true
            } else {
                false
            }
        }
    }
    
    private fun setupInitialValues() {
        tvTitle.text = context.getString(R.string.create_new)
        tvCurrentPath.text = currentPath
        
        // Set default selection to folder
        rbFolder.isChecked = true
        layoutFileType.visibility = View.GONE
        
        // Set default name
        val defaultName = generateDefaultName(TYPE_FOLDER)
        etItemName.setText(defaultName)
        etItemName.setSelection(defaultName.length)
        
        // Show keyboard
        etItemName.requestFocus()
    }
    
    private fun getSelectedType(): Int {
        return when (rgItemType.checkedRadioButtonId) {
            R.id.rbFolder -> TYPE_FOLDER
            R.id.rbShortcut -> TYPE_SHORTCUT
            else -> TYPE_FILE
        }
    }
    
    private fun updateFileNameExtension(position: Int) {
        val currentName = etItemName.text.toString()
        val nameWithoutExt = currentName.substringBeforeLast(".", currentName)
        
        val extension = when (position) {
            0 -> ".txt"
            1 -> ".jpg"
            2 -> ".mp3"
            3 -> ".mp4"
            4 -> ".doc"
            5 -> ".html"
            6 -> ".md"
            7 -> ".json"
            8 -> ".xml"
            9 -> ".yml"
            10 -> ".properties"
            11 -> ".bat"
            12 -> ".sh"
            13 -> ".py"
            14 -> ".java"
            15 -> ".kt"
            else -> ""
        }
        
        if (extension.isNotEmpty()) {
            etItemName.setText(nameWithoutExt + extension)
            etItemName.setSelection((nameWithoutExt + extension).length)
        }
    }
    
    private fun generateDefaultName(type: Int): String {
        var counter = 1
        val baseName = when (type) {
            TYPE_FOLDER -> context.getString(R.string.new_folder)
            TYPE_FILE -> context.getString(R.string.new_file)
            TYPE_SHORTCUT -> context.getString(R.string.new_shortcut)
            else -> context.getString(R.string.new_item)
        }
        
        var itemName = baseName
        
        while (itemExists(itemName)) {
            counter++
            itemName = when (type) {
                TYPE_FOLDER -> context.getString(R.string.new_folder_with_number, counter)
                TYPE_FILE -> context.getString(R.string.new_file_with_number, counter)
                TYPE_SHORTCUT -> context.getString(R.string.new_shortcut_with_number, counter)
                else -> context.getString(R.string.new_item_with_number, counter)
            }
        }
        
        return itemName
    }
    
    private fun itemExists(itemName: String): Boolean {
        return existingItems.contains(itemName)
    }
    
    private fun validateItemName(name: String, type: Int): Boolean {
        val trimmedName = name.trim()
        
        // Check if empty
        if (trimmedName.isEmpty()) {
            showValidationError(context.getString(R.string.error_name_empty))
            return false
        }
        
        // Check length
        if (trimmedName.length > MAX_NAME_LENGTH) {
            showValidationError(context.getString(R.string.error_name_too_long, MAX_NAME_LENGTH))
            return false
        }
        
        // Check for invalid characters
        INVALID_CHARS.forEach { invalidChar ->
            if (trimmedName.contains(invalidChar)) {
                showValidationError(context.getString(R.string.error_name_invalid_chars))
                return false
            }
        }
        
        // Check if starts or ends with dot or space
        if (trimmedName.startsWith(".") || trimmedName.startsWith(" ") || 
            trimmedName.endsWith(".") || trimmedName.endsWith(" ")) {
            showValidationError(context.getString(R.string.error_name_invalid_start_end))
            return false
        }
        
        // Check if item already exists
        if (itemExists(trimmedName)) {
            showValidationError(context.getString(R.string.error_item_already_exists))
            return false
        }
        
        // Check for reserved names in Windows
        val reservedNames = listOf("CON", "PRN", "AUX", "NUL", 
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9")
        
        if (reservedNames.contains(trimmedName.uppercase())) {
            showValidationError(context.getString(R.string.error_name_reserved))
            return false
        }
        
        // Clear any previous error
        clearValidationError()
        return true
    }
    
    private fun showValidationError(message: String) {
        tvValidationMessage.text = message
        tvValidationMessage.visibility = View.VISIBLE
        tilItemName.error = message
        btnCreate.isEnabled = false
    }
    
    private fun clearValidationError() {
        tvValidationMessage.visibility = View.GONE
        tilItemName.error = null
        btnCreate.isEnabled = true
    }
    
    override fun show() {
        super.show()
        etItemName.requestFocus()
    }
    
    companion object {
        /**
         * Show the create item dialog
         */
        fun show(
            context: Context,
            currentPath: String,
            existingItems: List<String> = emptyList(),
            onItemCreated: (String, Int) -> Unit
        ) {
            CreateNewItemDialog(
                context,
                currentPath,
                existingItems,
                onItemCreated
            ).show()
        }
    }
}
