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
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.utils.FileUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class RenameItemDialog(
    context: Context,
    private val fileItem: FileItem,
    private val onRenamed: (String) -> Unit,
    private val onCancel: (() -> Unit)? = null
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvCurrentName: TextView
    private lateinit var tvExtension: TextView
    private lateinit var tilNewName: TextInputLayout
    private lateinit var etNewName: TextInputEditText
    private lateinit var etExtension: TextInputEditText
    private lateinit var toggleExtension: ImageButton
    private lateinit var cbKeepExtension: CheckBox
    private lateinit var suggestionsLayout: LinearLayout
    private lateinit var suggestionChipGroup: LinearLayout
    private lateinit var tvValidationMessage: TextView
    private lateinit var btnRename: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnClose: ImageButton
    
    
    private var keepExtension = true
    private var nameWithoutExtension = ""
    private var extension = ""
    private var suggestionsVisible = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        
        parseFileName()
        setupViews()
        setupListeners()
        setupSuggestions()
    }
    
    private fun parseFileName() {
        val name = fileItem.name
        val lastDotIndex = name.lastIndexOf('.')
        
        if (lastDotIndex > 0 && !fileItem.isDirectory) {
            nameWithoutExtension = name.substring(0, lastDotIndex)
            extension = name.substring(lastDotIndex)
        } else {
            nameWithoutExtension = name
            extension = ""
        }
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_rename_item, null)
        setContentView(binding)
        
        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvCurrentName = binding.findViewById(R.id.tvCurrentName)
        tvExtension = binding.findViewById(R.id.tvExtension)
        tilNewName = binding.findViewById(R.id.tilNewName)
        etNewName = binding.findViewById(R.id.etNewName)
        etExtension = binding.findViewById(R.id.etExtension)
        toggleExtension = binding.findViewById(R.id.toggleExtension)
        cbKeepExtension = binding.findViewById(R.id.cbKeepExtension)
        suggestionsLayout = binding.findViewById(R.id.suggestionsLayout)
        suggestionChipGroup = binding.findViewById(R.id.suggestionChipGroup)
        tvValidationMessage = binding.findViewById(R.id.tvValidationMessage)
        btnRename = binding.findViewById(R.id.btnRename)
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
        
        // Set icon
        ivIcon.setImageResource(
            if (fileItem.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
        )
        ivIcon.setColorFilter(
            ContextCompat.getColor(context, R.color.primary_color),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        
        // Set current name
        tvTitle.text = if (fileItem.isDirectory) 
            context.getString(R.string.rename_folder) 
        else 
            context.getString(R.string.rename_file)
        
        tvCurrentName.text = fileItem.name
        
        // Set initial values
        etNewName.setText(nameWithoutExtension)
        etNewName.setSelection(nameWithoutExtension.length)
        
        if (extension.isNotEmpty() && !fileItem.isDirectory) {
            tvExtension.text = extension
            etExtension.setText(extension)
            cbKeepExtension.isChecked = true
            cbKeepExtension.visibility = View.VISIBLE
        } else {
            tvExtension.visibility = View.GONE
            etExtension.visibility = View.GONE
            toggleExtension.visibility = View.GONE
            cbKeepExtension.visibility = View.GONE
        }
        
        // Initial validation
        validateName(nameWithoutExtension)
    }
    
    private fun setupListeners() {
        btnRename.setOnClickListener {
            performRename()
        }
        
        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
        
        btnClose.setOnClickListener {
            dismiss()
        }
        
        etNewName.doOnTextChanged { text, _, _, _ ->
            validateName(text.toString())
        }
        
        etExtension.doOnTextChanged { text, _, _, _ ->
            if (keepExtension) {
                validateName(etNewName.text.toString())
            }
        }
        
        cbKeepExtension.setOnCheckedChangeListener { _, isChecked ->
            keepExtension = isChecked
            if (isChecked) {
                etExtension.visibility = View.GONE
                tvExtension.visibility = View.VISIBLE
                tvExtension.text = extension
            } else {
                etExtension.visibility = View.VISIBLE
                tvExtension.visibility = View.GONE
                etExtension.setText(extension)
            }
            validateName(etNewName.text.toString())
        }
        
        toggleExtension.setOnClickListener {
            suggestionsVisible = !suggestionsVisible
            suggestionsLayout.visibility = if (suggestionsVisible) View.VISIBLE else View.GONE
            toggleExtension.setImageResource(
                if (suggestionsVisible) R.drawable.ic_expand_less
                else R.drawable.ic_expand_more
            )
        }
        
        etNewName.setOnEditorActionListener { _, _, _ ->
            performRename()
            true
        }
    }
    
    private fun setupSuggestions() {
        SUGGESTIONS.forEach { suggestion ->
            val chip = LayoutInflater.from(context)
                .inflate(R.layout.item_suggestion_chip, suggestionChipGroup, false) as TextView
            
            chip.text = suggestion
            chip.setOnClickListener {
                val currentName = etNewName.text.toString()
                val newName = if (currentName.isEmpty()) {
                    suggestion
                } else {
                    "$currentName $suggestion"
                }
                etNewName.setText(newName)
                etNewName.setSelection(newName.length)
                suggestionsLayout.visibility = View.GONE
                suggestionsVisible = false
                toggleExtension.setImageResource(R.drawable.ic_expand_more)
            }
            
            suggestionChipGroup.addView(chip)
        }
    }
    
    private fun validateName(name: String): Boolean {
        val trimmedName = name.trim()
        
        // Check if empty
        if (trimmedName.isEmpty()) {
            showValidationError(context.getString(R.string.error_name_empty))
            return false
        }
        
        // Check length
        if (trimmedName.length > 255) {
            showValidationError(context.getString(R.string.error_name_too_long, 255))
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
        
        // Check if same as original
        val newFullName = if (keepExtension && extension.isNotEmpty()) {
            trimmedName + extension
        } else if (!keepExtension && etExtension.text.toString().isNotEmpty()) {
            trimmedName + etExtension.text.toString()
        } else {
            trimmedName
        }
        
        if (newFullName == fileItem.name) {
            showValidationError(context.getString(R.string.error_name_same))
            return false
        }
        
        // Check if file already exists
        val parent = File(fileItem.path).parentFile
        if (parent != null) {
            val newFile = File(parent, newFullName)
            if (newFile.exists()) {
                showValidationError(context.getString(R.string.error_file_already_exists))
                return false
            }
        }
        
        // Clear any previous error
        clearValidationError()
        return true
    }
    
    private fun performRename() {
        val newName = etNewName.text.toString().trim()
        
        if (!validateName(newName)) {
            etNewName.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
            return
        }
        
        val newFullName = if (keepExtension && extension.isNotEmpty()) {
            newName + extension
        } else if (!keepExtension && etExtension.text.toString().isNotEmpty()) {
            newName + etExtension.text.toString()
        } else {
            newName
        }
        
        onRenamed.invoke(newFullName)
        dismiss()
    }
    
    private fun showValidationError(message: String) {
        tvValidationMessage.text = message
        tvValidationMessage.visibility = View.VISIBLE
        tilNewName.error = message
        btnRename.isEnabled = false
    }
    
    private fun clearValidationError() {
        tvValidationMessage.visibility = View.GONE
        tilNewName.error = null
        btnRename.isEnabled = true
    }
    
    override fun show() {
        super.show()
        etNewName.requestFocus()
    }

    companion object {
        private val INVALID_CHARS = arrayOf("\\", "/", ":", "*", "?", "\"", "<", ">", "|")
        private val SUGGESTIONS = listOf(
            "Copy", "Backup", "New", "Old", "Final", "Draft",
            "Version 1", "Version 2", "Updated", "Modified"
        )

        fun show(
            context: Context,
            fileItem: FileItem,
            onRenamed: (String) -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            RenameItemDialog(context, fileItem, onRenamed, onCancel).show()
        }

        fun show(
            context: Context,
            file: File,
            onRenamed: (String) -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            val fileItem = FileItem(
                name = file.name,
                path = file.absolutePath,
                size = file.length(),
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory,
                isHidden = file.isHidden,
                isSelected = false
            )
            RenameItemDialog(context, fileItem, onRenamed, onCancel).show()
        }
    }
}
