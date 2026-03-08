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
import com.exory550.exoryfilemanager.utils.FileUtils
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class CompressAsDialog(
    context: Context,
    private val files: List<File>,
    private val defaultPath: String,
    private val onCompress: (CompressOptions) -> Unit
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    private lateinit var binding: View
    private lateinit var etArchiveName: TextInputEditText
    private lateinit var tilArchiveName: TextInputLayout
    private lateinit var spinnerFormat: Spinner
    private lateinit var spinnerCompressionLevel: Spinner
    private lateinit var sliderCompressionLevel: Slider
    private lateinit var switchSplitArchive: SwitchMaterial
    private lateinit var layoutSplitSize: LinearLayout
    private lateinit var spinnerSplitSize: Spinner
    private lateinit var switchEncrypt: SwitchMaterial
    private lateinit var layoutPassword: LinearLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var switchEncryptFilenames: SwitchMaterial
    private lateinit var chkDeleteAfterCompress: CheckBox
    private lateinit var chkTestArchive: CheckBox
    private lateinit var chkCreateSeparate: CheckBox
    private lateinit var tvEstimatedSize: TextView
    private lateinit var tvFilesCount: TextView
    private lateinit var tvTotalSize: TextView
    private lateinit var btnCompress: Button
    private lateinit var btnCancel: Button
    private lateinit var btnAdvanced: Button
    private lateinit var advancedOptionsLayout: LinearLayout
    
    private val fileUtils = FileUtils.getInstance()
    private var isAdvancedVisible = false
    private var totalSize: Long = 0
    
    data class CompressOptions(
        val archiveName: String,
        val format: String,
        val compressionLevel: Int,
        val splitSize: Long?,
        val password: String?,
        val encryptFilenames: Boolean,
        val deleteAfterCompress: Boolean,
        val testArchive: Boolean,
        val createSeparate: Boolean
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        
        calculateTotalSize()
        setupViews()
        setupSpinners()
        setupListeners()
        setupInitialValues()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_compress_as, null)
        setContentView(binding)
        
        etArchiveName = binding.findViewById(R.id.etArchiveName)
        tilArchiveName = binding.findViewById(R.id.tilArchiveName)
        spinnerFormat = binding.findViewById(R.id.spinnerFormat)
        spinnerCompressionLevel = binding.findViewById(R.id.spinnerCompressionLevel)
        sliderCompressionLevel = binding.findViewById(R.id.sliderCompressionLevel)
        switchSplitArchive = binding.findViewById(R.id.switchSplitArchive)
        layoutSplitSize = binding.findViewById(R.id.layoutSplitSize)
        spinnerSplitSize = binding.findViewById(R.id.spinnerSplitSize)
        switchEncrypt = binding.findViewById(R.id.switchEncrypt)
        layoutPassword = binding.findViewById(R.id.layoutPassword)
        etPassword = binding.findViewById(R.id.etPassword)
        etConfirmPassword = binding.findViewById(R.id.etConfirmPassword)
        switchEncryptFilenames = binding.findViewById(R.id.switchEncryptFilenames)
        chkDeleteAfterCompress = binding.findViewById(R.id.chkDeleteAfterCompress)
        chkTestArchive = binding.findViewById(R.id.chkTestArchive)
        chkCreateSeparate = binding.findViewById(R.id.chkCreateSeparate)
        tvEstimatedSize = binding.findViewById(R.id.tvEstimatedSize)
        tvFilesCount = binding.findViewById(R.id.tvFilesCount)
        tvTotalSize = binding.findViewById(R.id.tvTotalSize)
        btnCompress = binding.findViewById(R.id.btnCompress)
        btnCancel = binding.findViewById(R.id.btnCancel)
        btnAdvanced = binding.findViewById(R.id.btnAdvanced)
        advancedOptionsLayout = binding.findViewById(R.id.advancedOptionsLayout)
        
        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
        
        // Update file info
        tvFilesCount.text = context.getString(R.string.files_selected, files.size)
        tvTotalSize.text = fileUtils.formatFileSize(totalSize)
    }
    
    private fun setupSpinners() {
        // Format spinner
        val formats = arrayOf("ZIP", "7Z", "TAR", "TAR.GZ", "TAR.BZ2")
        val formatAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, formats)
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFormat.adapter = formatAdapter
        
        // Compression level spinner (for 7Z)
        val levels = arrayOf(
            context.getString(R.string.compression_level_store),
            context.getString(R.string.compression_level_fastest),
            context.getString(R.string.compression_level_fast),
            context.getString(R.string.compression_level_normal),
            context.getString(R.string.compression_level_maximum),
            context.getString(R.string.compression_level_ultra)
        )
        val levelAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, levels)
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCompressionLevel.adapter = levelAdapter
        
        // Split sizes
        val splitSizes = arrayOf(
            context.getString(R.string.split_size_100mb),
            context.getString(R.string.split_size_650mb),
            context.getString(R.string.split_size_700mb),
            context.getString(R.string.split_size_1gb),
            context.getString(R.string.split_size_2gb),
            context.getString(R.string.split_size_4gb),
            context.getString(R.string.split_size_custom)
        )
        val splitAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, splitSizes)
        splitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSplitSize.adapter = splitAdapter
    }
    
    private fun setupListeners() {
        btnCompress.setOnClickListener {
            if (validateInputs()) {
                onCompress.invoke(collectOptions())
                dismiss()
            }
        }
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnAdvanced.setOnClickListener {
            isAdvancedVisible = !isAdvancedVisible
            advancedOptionsLayout.visibility = if (isAdvancedVisible) View.VISIBLE else View.GONE
            btnAdvanced.text = if (isAdvancedVisible) 
                context.getString(R.string.hide_advanced) 
            else 
                context.getString(R.string.show_advanced)
        }
        
        spinnerFormat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateUIPerFormat(position)
                updateEstimatedSize()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        sliderCompressionLevel.addOnChangeListener { _, value, _ ->
            // Update compression level display
            updateEstimatedSize()
        }
        
        switchSplitArchive.setOnCheckedChangeListener { _, isChecked ->
            layoutSplitSize.visibility = if (isChecked) View.VISIBLE else View.GONE
            updateEstimatedSize()
        }
        
        switchEncrypt.setOnCheckedChangeListener { _, isChecked ->
            layoutPassword.visibility = if (isChecked) View.VISIBLE else View.GONE
            switchEncryptFilenames.isEnabled = isChecked && spinnerFormat.selectedItemPosition == 1 // 7Z only
        }
        
        etArchiveName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateArchiveName()
            }
        })
        
        chkCreateSeparate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tilArchiveName.hint = context.getString(R.string.archive_name_prefix)
            } else {
                tilArchiveName.hint = context.getString(R.string.archive_name)
            }
        }
    }
    
    private fun setupInitialValues() {
        // Set default archive name based on first file
        val baseName = if (files.size == 1) {
            files[0].nameWithoutExtension
        } else {
            "archive"
        }
        etArchiveName.setText(baseName)
        
        // Default to ZIP format
        spinnerFormat.setSelection(0)
        
        // Default compression level to Normal
        spinnerCompressionLevel.setSelection(3)
        sliderCompressionLevel.value = 5f
        
        // Default settings
        switchSplitArchive.isChecked = false
        switchEncrypt.isChecked = false
        chkDeleteAfterCompress.isChecked = false
        chkTestArchive.isChecked = false
        chkCreateSeparate.isChecked = false
        
        updateUIPerFormat(0)
    }
    
    private fun updateUIPerFormat(formatPosition: Int) {
        // Show/hide compression level based on format
        val showCompression = formatPosition != 0 && formatPosition != 2 // Not for TAR
        
        spinnerCompressionLevel.visibility = if (showCompression) View.VISIBLE else View.GONE
        sliderCompressionLevel.visibility = if (showCompression) View.VISIBLE else View.GONE
        
        // Update encryption availability
        switchEncrypt.isEnabled = formatPosition == 0 || formatPosition == 1 // ZIP or 7Z
        
        // Update filename encryption availability (7Z only)
        if (switchEncrypt.isChecked) {
            switchEncryptFilenames.isEnabled = formatPosition == 1 // 7Z only
        }
        
        // Update split archive availability
        switchSplitArchive.isEnabled = formatPosition != 3 && formatPosition != 4 // Not for TAR.GZ/TAR.BZ2
    }
    
    private fun updateEstimatedSize() {
        // Calculate estimated compressed size based on format and compression level
        val estimatedRatio = when (spinnerFormat.selectedItemPosition) {
            0 -> 0.7 // ZIP
            1 -> 0.5 // 7Z
            2 -> 1.0 // TAR (no compression)
            3 -> 0.6 // TAR.GZ
            4 -> 0.5 // TAR.BZ2
            else -> 0.7
        }
        
        // Adjust based on compression level
        val levelMultiplier = when (sliderCompressionLevel.value.toInt()) {
            0 -> 1.0 // Store
            1, 2 -> 0.9 // Fastest/Fast
            3, 4 -> 0.7 // Normal/Maximum
            5 -> 0.5 // Ultra
            else -> 0.7
        }
        
        val estimatedSize = (totalSize * estimatedRatio * levelMultiplier).toLong()
        tvEstimatedSize.text = context.getString(
            R.string.estimated_size,
            fileUtils.formatFileSize(estimatedSize)
        )
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate archive name
        if (!validateArchiveName()) {
            isValid = false
        }
        
        // Validate passwords if encryption enabled
        if (switchEncrypt.isChecked) {
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            
            if (password.isEmpty()) {
                etPassword.error = context.getString(R.string.error_password_required)
                isValid = false
            } else if (password.length < 4) {
                etPassword.error = context.getString(R.string.error_password_too_short)
                isValid = false
            } else if (password != confirmPassword) {
                etConfirmPassword.error = context.getString(R.string.error_passwords_not_match)
                isValid = false
            }
        }
        
        return isValid
    }
    
    private fun validateArchiveName(): Boolean {
        val name = etArchiveName.text.toString()
        return when {
            name.isEmpty() -> {
                tilArchiveName.error = context.getString(R.string.error_name_required)
                false
            }
            !isValidFileName(name) -> {
                tilArchiveName.error = context.getString(R.string.error_invalid_filename)
                false
            }
            else -> {
                tilArchiveName.error = null
                true
            }
        }
    }
    
    private fun isValidFileName(name: String): Boolean {
        val invalidChars = "[\\\\/:*?\"<>|]".toRegex()
        return !invalidChars.containsMatchIn(name)
    }
    
    private fun collectOptions(): CompressOptions {
        val splitSize = if (switchSplitArchive.isChecked) {
            when (spinnerSplitSize.selectedItemPosition) {
                0 -> 100L * 1024 * 1024 // 100 MB
                1 -> 650L * 1024 * 1024 // 650 MB
                2 -> 700L * 1024 * 1024 // 700 MB
                3 -> 1024L * 1024 * 1024 // 1 GB
                4 -> 2L * 1024 * 1024 * 1024 // 2 GB
                5 -> 4L * 1024 * 1024 * 1024 // 4 GB
                else -> null
            }
        } else {
            null
        }
        
        val format = when (spinnerFormat.selectedItemPosition) {
            0 -> "zip"
            1 -> "7z"
            2 -> "tar"
            3 -> "tar.gz"
            4 -> "tar.bz2"
            else -> "zip"
        }
        
        return CompressOptions(
            archiveName = etArchiveName.text.toString(),
            format = format,
            compressionLevel = sliderCompressionLevel.value.toInt(),
            splitSize = splitSize,
            password = if (switchEncrypt.isChecked) etPassword.text.toString() else null,
            encryptFilenames = switchEncrypt.isChecked && switchEncryptFilenames.isChecked,
            deleteAfterCompress = chkDeleteAfterCompress.isChecked,
            testArchive = chkTestArchive.isChecked,
            createSeparate = chkCreateSeparate.isChecked
        )
    }
    
    private fun calculateTotalSize() {
        totalSize = files.sumOf { it.length() }
    }
    
    companion object {
        fun show(
            context: Context,
            files: List<File>,
            defaultPath: String,
            onCompress: (CompressOptions) -> Unit
        ) {
            CompressAsDialog(context, files, defaultPath, onCompress).show()
        }
    }
}
