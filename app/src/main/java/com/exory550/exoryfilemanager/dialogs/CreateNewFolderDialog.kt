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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.exory550.exoryfilemanager.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class CreateNewFolderDialog(
    context: Context,
    private val currentPath: String,
    private val existingFolders: List<String>,
    private val onFolderCreated: (String) -> Unit
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvCurrentPath: TextView
    private lateinit var tilFolderName: TextInputLayout
    private lateinit var etFolderName: TextInputEditText
    private lateinit var tvValidationMessage: TextView
    private lateinit var btnCreate: Button
    private lateinit var btnCancel: Button
    private lateinit var btnClose: ImageView
    
    companion object {
        private const val MAX_FOLDER_NAME_LENGTH = 255
        private val INVALID_CHARS = arrayOf("\\", "/", ":", "*", "?", "\"", "<", ">", "|")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        
        setupViews()
        setupListeners()
        setupInitialValues()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_create_new_folder, null)
        setContentView(binding)
        
        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvCurrentPath = binding.findViewById(R.id.tvCurrentPath)
        tilFolderName = binding.findViewById(R.id.tilFolderName)
        etFolderName = binding.findViewById(R.id.etFolderName)
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
    
    private fun setupListeners() {
        btnCreate.setOnClickListener {
            val folderName = etFolderName.text.toString().trim()
            if (validateFolderName(folderName)) {
                onFolderCreated.invoke(folderName)
                dismiss()
            }
        }
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnClose.setOnClickListener {
            dismiss()
        }
        
        etFolderName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validateFolderName(s.toString())
            }
        })
        
        etFolderName.setOnEditorActionListener { _, _, _ ->
            val folderName = etFolderName.text.toString().trim()
            if (validateFolderName(folderName)) {
                onFolderCreated.invoke(folderName)
                dismiss()
                true
            } else {
                false
            }
        }
    }
    
    private fun setupInitialValues() {
        tvTitle.text = context.getString(R.string.create_new_folder)
        tvCurrentPath.text = currentPath
        
        // Set default folder name
        val defaultName = generateDefaultFolderName()
        etFolderName.setText(defaultName)
        etFolderName.setSelection(defaultName.length)
        
        // Show keyboard
        etFolderName.requestFocus()
    }
    
    private fun generateDefaultFolderName(): String {
        var counter = 1
        var folderName = context.getString(R.string.new_folder)
        
        while (folderExists(folderName)) {
            counter++
            folderName = context.getString(R.string.new_folder_with_number, counter)
        }
        
        return folderName
    }
    
    private fun folderExists(folderName: String): Boolean {
        val folder = File(currentPath, folderName)
        return folder.exists() || existingFolders.contains(folderName)
    }
    
    private fun validateFolderName(name: String): Boolean {
        val trimmedName = name.trim()
        
        // Check if empty
        if (trimmedName.isEmpty()) {
            showValidationError(context.getString(R.string.error_folder_name_empty))
            return false
        }
        
        // Check length
        if (trimmedName.length > MAX_FOLDER_NAME_LENGTH) {
            showValidationError(context.getString(R.string.error_folder_name_too_long, MAX_FOLDER_NAME_LENGTH))
            return false
        }
        
        // Check for invalid characters
        INVALID_CHARS.forEach { invalidChar ->
            if (trimmedName.contains(invalidChar)) {
                showValidationError(context.getString(R.string.error_folder_name_invalid_chars))
                return false
            }
        }
        
        // Check if starts or ends with dot or space
        if (trimmedName.startsWith(".") || trimmedName.startsWith(" ") || 
            trimmedName.endsWith(".") || trimmedName.endsWith(" ")) {
            showValidationError(context.getString(R.string.error_folder_name_invalid_start_end))
            return false
        }
        
        // Check if folder already exists
        if (folderExists(trimmedName)) {
            showValidationError(context.getString(R.string.error_folder_already_exists))
            return false
        }
        
        // Check for reserved names in Windows
        val reservedNames = listOf("CON", "PRN", "AUX", "NUL", 
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9")
        
        if (reservedNames.contains(trimmedName.uppercase())) {
            showValidationError(context.getString(R.string.error_folder_name_reserved))
            return false
        }
        
        // Clear any previous error
        clearValidationError()
        return true
    }
    
    private fun showValidationError(message: String) {
        tvValidationMessage.text = message
        tvValidationMessage.visibility = View.VISIBLE
        tilFolderName.error = message
        btnCreate.isEnabled = false
    }
    
    private fun clearValidationError() {
        tvValidationMessage.visibility = View.GONE
        tilFolderName.error = null
        btnCreate.isEnabled = true
    }
    
    override fun show() {
        super.show()
        // Additional setup after showing
        etFolderName.requestFocus()
    }
    
    companion object {
        /**
         * Show the create folder dialog
         */
        fun show(
            context: Context,
            currentPath: String,
            existingFolders: List<String> = emptyList(),
            onFolderCreated: (String) -> Unit
        ) {
            CreateNewFolderDialog(
                context,
                currentPath,
                existingFolders,
                onFolderCreated
            ).show()
        }
        
        /**
         * Show the create folder dialog with file list
         */
        fun show(
            context: Context,
            currentPath: String,
            existingFiles: List<File>,
            onFolderCreated: (String) -> Unit
        ) {
            val existingNames = existingFiles
                .filter { it.isDirectory }
                .map { it.name }
            
            CreateNewFolderDialog(
                context,
                currentPath,
                existingNames,
                onFolderCreated
            ).show()
        }
    }
}
