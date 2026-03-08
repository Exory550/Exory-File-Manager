package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.utils.PreferenceManager

class ChangeSortingDialog(
    context: Context,
    private val currentSortMode: Int,
    private val currentSortOrder: Int,
    private val onSortChanged: (sortMode: Int, sortOrder: Int) -> Unit
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    private lateinit var binding: View
    private lateinit var rgSortMode: RadioGroup
    private lateinit var rgSortOrder: RadioGroup
    private lateinit var rbName: RadioButton
    private lateinit var rbSize: RadioButton
    private lateinit var rbDate: RadioButton
    private lateinit var rbType: RadioButton
    private lateinit var rbExtension: RadioButton
    private lateinit var rbAscending: RadioButton
    private lateinit var rbDescending: RadioButton
    private lateinit var btnApply: Button
    private lateinit var btnCancel: Button
    private lateinit var btnReset: Button
    private lateinit var ivClose: ImageView
    private lateinit var tvTitle: TextView
    
    companion object {
        const val SORT_NAME = 0
        const val SORT_SIZE = 1
        const val SORT_DATE = 2
        const val SORT_TYPE = 3
        const val SORT_EXTENSION = 4
        
        const val ORDER_ASCENDING = 0
        const val ORDER_DESCENDING = 1
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        
        setupViews()
        setupClickListeners()
        loadCurrentSettings()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_change_sorting, null)
        setContentView(binding)
        
        rgSortMode = binding.findViewById(R.id.rgSortMode)
        rgSortOrder = binding.findViewById(R.id.rgSortOrder)
        rbName = binding.findViewById(R.id.rbName)
        rbSize = binding.findViewById(R.id.rbSize)
        rbDate = binding.findViewById(R.id.rbDate)
        rbType = binding.findViewById(R.id.rbType)
        rbExtension = binding.findViewById(R.id.rbExtension)
        rbAscending = binding.findViewById(R.id.rbAscending)
        rbDescending = binding.findViewById(R.id.rbDescending)
        btnApply = binding.findViewById(R.id.btnApply)
        btnCancel = binding.findViewById(R.id.btnCancel)
        btnReset = binding.findViewById(R.id.btnReset)
        ivClose = binding.findViewById(R.id.ivClose)
        tvTitle = binding.findViewById(R.id.tvTitle)
        
        // Set dialog width to match parent with margins
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set background with rounded corners
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
    }
    
    private fun setupClickListeners() {
        btnApply.setOnClickListener {
            val selectedMode = getSelectedSortMode()
            val selectedOrder = getSelectedSortOrder()
            onSortChanged.invoke(selectedMode, selectedOrder)
            dismiss()
        }
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnReset.setOnClickListener {
            resetToDefault()
        }
        
        ivClose.setOnClickListener {
            dismiss()
        }
        
        // Radio group listeners
        rgSortMode.setOnCheckedChangeListener { _, checkedId ->
            updatePreview()
        }
        
        rgSortOrder.setOnCheckedChangeListener { _, checkedId ->
            updatePreview()
        }
    }
    
    private fun loadCurrentSettings() {
        // Set sort mode
        when (currentSortMode) {
            SORT_NAME -> rbName.isChecked = true
            SORT_SIZE -> rbSize.isChecked = true
            SORT_DATE -> rbDate.isChecked = true
            SORT_TYPE -> rbType.isChecked = true
            SORT_EXTENSION -> rbExtension.isChecked = true
            else -> rbName.isChecked = true
        }
        
        // Set sort order
        when (currentSortOrder) {
            ORDER_ASCENDING -> rbAscending.isChecked = true
            ORDER_DESCENDING -> rbDescending.isChecked = true
            else -> rbAscending.isChecked = true
        }
        
        updatePreview()
    }
    
    private fun getSelectedSortMode(): Int {
        return when (rgSortMode.checkedRadioButtonId) {
            R.id.rbName -> SORT_NAME
            R.id.rbSize -> SORT_SIZE
            R.id.rbDate -> SORT_DATE
            R.id.rbType -> SORT_TYPE
            R.id.rbExtension -> SORT_EXTENSION
            else -> SORT_NAME
        }
    }
    
    private fun getSelectedSortOrder(): Int {
        return when (rgSortOrder.checkedRadioButtonId) {
            R.id.rbAscending -> ORDER_ASCENDING
            R.id.rbDescending -> ORDER_DESCENDING
            else -> ORDER_ASCENDING
        }
    }
    
    private fun resetToDefault() {
        rbName.isChecked = true
        rbAscending.isChecked = true
        updatePreview()
    }
    
    private fun updatePreview() {
        val mode = getSelectedSortMode()
        val order = getSelectedSortOrder()
        
        val previewText = when (mode) {
            SORT_NAME -> context.getString(R.string.sort_by_name)
            SORT_SIZE -> context.getString(R.string.sort_by_size)
            SORT_DATE -> context.getString(R.string.sort_by_date)
            SORT_TYPE -> context.getString(R.string.sort_by_type)
            SORT_EXTENSION -> context.getString(R.string.sort_by_extension)
            else -> context.getString(R.string.sort_by_name)
        } + " (" + when (order) {
            ORDER_ASCENDING -> context.getString(R.string.ascending)
            ORDER_DESCENDING -> context.getString(R.string.descending)
            else -> context.getString(R.string.ascending)
        } + ")"
        
        // Show preview somewhere or just update title
        tvTitle.text = context.getString(R.string.sort_settings)
    }
    
    /**
     * Show dialog with current settings
     */
    fun show(sortMode: Int, sortOrder: Int) {
        // Update current settings before showing
        when (sortMode) {
            SORT_NAME -> rbName.isChecked = true
            SORT_SIZE -> rbSize.isChecked = true
            SORT_DATE -> rbDate.isChecked = true
            SORT_TYPE -> rbType.isChecked = true
            SORT_EXTENSION -> rbExtension.isChecked = true
        }
        
        when (sortOrder) {
            ORDER_ASCENDING -> rbAscending.isChecked = true
            ORDER_DESCENDING -> rbDescending.isChecked = true
        }
        
        updatePreview()
        super.show()
    }
    
    /**
     * Factory method to create and show dialog
     */
    companion object {
        fun show(
            context: Context,
            currentSortMode: Int,
            currentSortOrder: Int,
            onSortChanged: (sortMode: Int, sortOrder: Int) -> Unit
        ) {
            ChangeSortingDialog(
                context,
                currentSortMode,
                currentSortOrder,
                onSortChanged
            ).show()
        }
    }
}
