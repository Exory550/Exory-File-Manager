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
import com.exory550.exoryfilemanager.utils.FileUtils
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileConflictDialog(
    context: Context,
    private val sourceFile: File,
    private val destFile: File,
    private val isMultiple: Boolean = false,
    private val currentIndex: Int = 0,
    private val totalCount: Int = 1,
    private val onConflictResolved: (ConflictResolution) -> Unit
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    private lateinit var binding: View
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvProgress: TextView
    private lateinit var progressContainer: LinearLayout
    private lateinit var sourceContainer: View
    private lateinit var destContainer: View
    private lateinit var ivSourceIcon: ImageView
    private lateinit var ivDestIcon: ImageView
    private lateinit var tvSourceName: TextView
    private lateinit var tvDestName: TextView
    private lateinit var tvSourcePath: TextView
    private lateinit var tvDestPath: TextView
    private lateinit var tvSourceSize: TextView
    private lateinit var tvDestSize: TextView
    private lateinit var tvSourceModified: TextView
    private lateinit var tvDestModified: TextView
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var rbReplace: MaterialRadioButton
    private lateinit var rbSkip: MaterialRadioButton
    private lateinit var rbRename: MaterialRadioButton
    private lateinit var rbKeepBoth: MaterialRadioButton
    private lateinit var cbApplyToAll: CheckBox
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button
    private lateinit var previewRecyclerView: RecyclerView
    private lateinit var previewContainer: LinearLayout
    private lateinit var renameContainer: LinearLayout
    private lateinit var etNewName: EditText
    
    private lateinit var fileUtils: FileUtils
    private var selectedResolution = ConflictResolution.REPLACE
    
    enum class ConflictResolution {
        REPLACE,
        SKIP,
        RENAME,
        KEEP_BOTH,
        REPLACE_OLDER,
        REPLACE_NEWER,
        REPLACE_SMALLER,
        REPLACE_LARGER
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        
        fileUtils = FileUtils.getInstance()
        
        setupViews()
        setupFileInfo()
        setupListeners()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_file_conflict, null)
        setContentView(binding)
        
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvMessage = binding.findViewById(R.id.tvMessage)
        tvProgress = binding.findViewById(R.id.tvProgress)
        progressContainer = binding.findViewById(R.id.progressContainer)
        sourceContainer = binding.findViewById(R.id.sourceContainer)
        destContainer = binding.findViewById(R.id.destContainer)
        ivSourceIcon = binding.findViewById(R.id.ivSourceIcon)
        ivDestIcon = binding.findViewById(R.id.ivDestIcon)
        tvSourceName = binding.findViewById(R.id.tvSourceName)
        tvDestName = binding.findViewById(R.id.tvDestName)
        tvSourcePath = binding.findViewById(R.id.tvSourcePath)
        tvDestPath = binding.findViewById(R.id.tvDestPath)
        tvSourceSize = binding.findViewById(R.id.tvSourceSize)
        tvDestSize = binding.findViewById(R.id.tvDestSize)
        tvSourceModified = binding.findViewById(R.id.tvSourceModified)
        tvDestModified = binding.findViewById(R.id.tvDestModified)
        toggleGroup = binding.findViewById(R.id.toggleGroup)
        rbReplace = binding.findViewById(R.id.rbReplace)
        rbSkip = binding.findViewById(R.id.rbSkip)
        rbRename = binding.findViewById(R.id.rbRename)
        rbKeepBoth = binding.findViewById(R.id.rbKeepBoth)
        cbApplyToAll = binding.findViewById(R.id.cbApplyToAll)
        btnOk = binding.findViewById(R.id.btnOk)
        btnCancel = binding.findViewById(R.id.btnCancel)
        previewRecyclerView = binding.findViewById(R.id.previewRecyclerView)
        previewContainer = binding.findViewById(R.id.previewContainer)
        renameContainer = binding.findViewById(R.id.renameContainer)
        etNewName = binding.findViewById(R.id.etNewName)
        
        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
        
        // Show/hide progress for multiple files
        if (isMultiple) {
            progressContainer.visibility = View.VISIBLE
            tvProgress.text = context.getString(R.string.file_progress, currentIndex + 1, totalCount)
        } else {
            progressContainer.visibility = View.GONE
        }
        
        // Hide apply to all for single file
        cbApplyToAll.visibility = if (isMultiple) View.VISIBLE else View.GONE
    }
    
    private fun setupFileInfo() {
        // Source file info
        tvSourceName.text = sourceFile.name
        tvSourcePath.text = sourceFile.parent
        tvSourceSize.text = fileUtils.formatFileSize(sourceFile.length())
        
        val sourceDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            .format(Date(sourceFile.lastModified()))
        tvSourceModified.text = sourceDate
        
        // Set source icon
        ivSourceIcon.setImageResource(
            if (sourceFile.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
        )
        
        // Destination file info
        tvDestName.text = destFile.name
        tvDestPath.text = destFile.parent
        tvDestSize.text = fileUtils.formatFileSize(destFile.length())
        
        val destDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            .format(Date(destFile.lastModified()))
        tvDestModified.text = destDate
        
        // Set destination icon
        ivDestIcon.setImageResource(
            if (destFile.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
        )
        
        // Compare files and suggest best action
        suggestBestAction()
    }
    
    private fun setupListeners() {
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedResolution = when (checkedId) {
                    R.id.rbReplace -> ConflictResolution.REPLACE
                    R.id.rbSkip -> ConflictResolution.SKIP
                    R.id.rbRename -> ConflictResolution.RENAME
                    R.id.rbKeepBoth -> ConflictResolution.KEEP_BOTH
                    else -> ConflictResolution.REPLACE
                }
                updateRenameVisibility()
            }
        }
        
        btnOk.setOnClickListener {
            val applyToAll = cbApplyToAll.isChecked
            val resolution = if (selectedResolution == ConflictResolution.RENAME) {
                val newName = etNewName.text.toString()
                if (newName.isNotBlank()) {
                    ConflictResolution.RENAME
                } else {
                    Toast.makeText(context, R.string.enter_new_name, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                selectedResolution
            }
            
            onConflictResolved.invoke(ConflictResolutionResult(resolution, applyToAll))
            dismiss()
        }
        
        btnCancel.setOnClickListener {
            onConflictResolved.invoke(ConflictResolutionResult(ConflictResolution.SKIP, false))
            dismiss()
        }
        
        // Advanced options
        binding.findViewById<Button>(R.id.btnAdvanced)?.setOnClickListener {
            showAdvancedOptions()
        }
    }
    
    private fun suggestBestAction() {
        // Compare file sizes and dates to suggest best action
        val sourceSize = sourceFile.length()
        val destSize = destFile.length()
        val sourceDate = sourceFile.lastModified()
        val destDate = destFile.lastModified()
        
        when {
            sourceSize > destSize && sourceDate > destDate -> {
                // Source is larger and newer
                tvMessage.text = context.getString(R.string.conflict_larger_newer)
            }
            sourceSize > destSize -> {
                // Source is larger but older
                tvMessage.text = context.getString(R.string.conflict_larger_older)
            }
            sourceDate > destDate -> {
                // Source is newer but smaller
                tvMessage.text = context.getString(R.string.conflict_newer_smaller)
            }
            sourceSize == destSize && sourceDate == destDate -> {
                // Files are identical
                tvMessage.text = context.getString(R.string.conflict_identical)
                rbSkip.isChecked = true
            }
            else -> {
                tvMessage.text = context.getString(R.string.conflict_different)
            }
        }
    }
    
    private fun updateRenameVisibility() {
        renameContainer.visibility = if (selectedResolution == ConflictResolution.RENAME) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        if (selectedResolution == ConflictResolution.RENAME) {
            etNewName.setText(generateNewFileName())
        }
    }
    
    private fun generateNewFileName(): String {
        val nameWithoutExt = sourceFile.nameWithoutExtension
        val extension = sourceFile.extension
        var counter = 1
        var newName: String
        
        do {
            newName = if (extension.isNotEmpty()) {
                "${nameWithoutExt}_$counter.$extension"
            } else {
                "${nameWithoutExt}_$counter"
            }
            counter++
        } while (File(destFile.parent, newName).exists())
        
        return newName
    }
    
    private fun showAdvancedOptions() {
        val options = arrayOf(
            context.getString(R.string.replace_older),
            context.getString(R.string.replace_newer),
            context.getString(R.string.replace_smaller),
            context.getString(R.string.replace_larger),
            context.getString(R.string.auto_rename_all),
            context.getString(R.string.compare_content)
        )
        
        AlertDialog.Builder(context)
            .setTitle(R.string.advanced_options)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectedResolution = ConflictResolution.REPLACE_OLDER
                    1 -> selectedResolution = ConflictResolution.REPLACE_NEWER
                    2 -> selectedResolution = ConflictResolution.REPLACE_SMALLER
                    3 -> selectedResolution = ConflictResolution.REPLACE_LARGER
                    4 -> selectedResolution = ConflictResolution.RENAME
                    5 -> compareFileContent()
                }
                updateRenameVisibility()
            }
            .show()
    }
    
    private fun compareFileContent() {
        // Show content comparison (for text files)
        if (fileUtils.isTextFile(sourceFile.name)) {
            showContentComparison()
        } else {
            Toast.makeText(context, R.string.cannot_compare_content, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showContentComparison() {
        // Implement content comparison view
        previewContainer.visibility = View.VISIBLE
        // Load and compare file contents
    }
    
    data class ConflictResolutionResult(
        val resolution: ConflictResolution,
        val applyToAll: Boolean
    )
    
    companion object {
        fun show(
            context: Context,
            sourceFile: File,
            destFile: File,
            onConflictResolved: (ConflictResolutionResult) -> Unit
        ) {
            FileConflictDialog(
                context,
                sourceFile,
                destFile,
                false,
                0,
                1,
                onConflictResolved
            ).show()
        }
        
        fun show(
            context: Context,
            sourceFile: File,
            destFile: File,
            index: Int,
            total: Int,
            onConflictResolved: (ConflictResolutionResult) -> Unit
        ) {
            FileConflictDialog(
                context,
                sourceFile,
                destFile,
                true,
                index,
                total,
                onConflictResolved
            ).show()
        }
    }
}
