package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.utils.FileUtils
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class DeleteAppDataDialog(
    context: Context,
    private val packageName: String,
    private val appName: String,
    private val onDataDeleted: (Boolean) -> Unit
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    private lateinit var binding: View
    private lateinit var ivAppIcon: ImageView
    private lateinit var tvAppName: TextView
    private lateinit var tvPackageName: TextView
    private lateinit var tvTotalSize: TextView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var recyclerView: RecyclerView
    private lateinit var cbSelectAll: MaterialCheckBox
    private lateinit var btnDelete: Button
    private lateinit var btnCancel: Button
    private lateinit var btnClose: ImageView
    private lateinit var tvNoData: TextView
    
    private lateinit var packageInfo: PackageInfo
    private var appDataDirectories = mutableListOf<AppDataDir>()
    private var adapter: AppDataAdapter? = null
    private val executor = Executors.newSingleThreadExecutor()
    
    data class AppDataDir(
        val path: String,
        val name: String,
        val type: String,
        val size: Long,
        val fileCount: Int,
        val lastModified: Long,
        var isSelected: Boolean = true
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        
        setupViews()
        loadPackageInfo()
        scanAppData()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_delete_app_data, null)
        setContentView(binding)
        
        ivAppIcon = binding.findViewById(R.id.ivAppIcon)
        tvAppName = binding.findViewById(R.id.tvAppName)
        tvPackageName = binding.findViewById(R.id.tvPackageName)
        tvTotalSize = binding.findViewById(R.id.tvTotalSize)
        progressBar = binding.findViewById(R.id.progressBar)
        recyclerView = binding.findViewById(R.id.recyclerView)
        cbSelectAll = binding.findViewById(R.id.cbSelectAll)
        btnDelete = binding.findViewById(R.id.btnDelete)
        btnCancel = binding.findViewById(R.id.btnCancel)
        btnClose = binding.findViewById(R.id.btnClose)
        tvNoData = binding.findViewById(R.id.tvNoData)
        
        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )
        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AppDataAdapter(
            onItemChecked = { updateTotalSize() }
        )
        recyclerView.adapter = adapter
        
        // Setup listeners
        btnDelete.setOnClickListener {
            deleteSelectedData()
        }
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnClose.setOnClickListener {
            dismiss()
        }
        
        cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            adapter?.selectAll(isChecked)
            updateTotalSize()
        }
    }
    
    private fun loadPackageInfo() {
        try {
            val pm = context.packageManager
            packageInfo = pm.getPackageInfo(packageName, 0)
            
            // Load app icon
            val icon = pm.getApplicationIcon(packageName)
            ivAppIcon.setImageDrawable(icon)
            
            tvAppName.text = appName
            tvPackageName.text = packageName
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun scanAppData() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoData.visibility = View.GONE
        
        executor.execute {
            appDataDirectories.clear()
            
            // Internal data directory
            val internalDataDir = File("/data/data/$packageName")
            if (internalDataDir.exists()) {
                val size = calculateFolderSize(internalDataDir)
                val fileCount = countFiles(internalDataDir)
                appDataDirectories.add(
                    AppDataDir(
                        path = internalDataDir.absolutePath,
                        name = context.getString(R.string.internal_data),
                        type = "internal",
                        size = size,
                        fileCount = fileCount,
                        lastModified = internalDataDir.lastModified()
                    )
                )
            }
            
            // External data directory
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val externalDataDirs = context.getExternalFilesDirs(null)
                externalDataDirs.forEach { dir ->
                    if (dir != null && dir.exists() && dir.absolutePath.contains(packageName)) {
                        val size = calculateFolderSize(dir)
                        val fileCount = countFiles(dir)
                        appDataDirectories.add(
                            AppDataDir(
                                path = dir.absolutePath,
                                name = context.getString(R.string.external_data),
                                type = "external",
                                size = size,
                                fileCount = fileCount,
                                lastModified = dir.lastModified()
                            )
                        )
                    }
                }
            }
            
            // Cache directory
            val cacheDir = File("/data/data/$packageName/cache")
            if (cacheDir.exists()) {
                val size = calculateFolderSize(cacheDir)
                val fileCount = countFiles(cacheDir)
                appDataDirectories.add(
                    AppDataDir(
                        path = cacheDir.absolutePath,
                        name = context.getString(R.string.cache_data),
                        type = "cache",
                        size = size,
                        fileCount = fileCount,
                        lastModified = cacheDir.lastModified()
                    )
                )
            }
            
            // Code cache directory
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val codeCacheDir = File("/data/data/$packageName/code_cache")
                if (codeCacheDir.exists()) {
                    val size = calculateFolderSize(codeCacheDir)
                    val fileCount = countFiles(codeCacheDir)
                    appDataDirectories.add(
                        AppDataDir(
                            path = codeCacheDir.absolutePath,
                            name = context.getString(R.string.code_cache),
                            type = "code_cache",
                            size = size,
                            fileCount = fileCount,
                            lastModified = codeCacheDir.lastModified()
                        )
                    )
                }
            }
            
            // No data found
            if (appDataDirectories.isEmpty()) {
                appDataDirectories.add(
                    AppDataDir(
                        path = "",
                        name = context.getString(R.string.no_app_data),
                        type = "none",
                        size = 0,
                        fileCount = 0,
                        lastModified = 0
                    )
                )
            }
            
            // Update UI on main thread
            (context as? android.app.Activity)?.runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (appDataDirectories.isEmpty() || 
                    (appDataDirectories.size == 1 && appDataDirectories[0].type == "none")) {
                    tvNoData.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    btnDelete.isEnabled = false
                    cbSelectAll.isEnabled = false
                } else {
                    recyclerView.visibility = View.VISIBLE
                    tvNoData.visibility = View.GONE
                    adapter?.submitList(appDataDirectories)
                    updateTotalSize()
                    cbSelectAll.isChecked = true
                }
            }
        }
    }
    
    private fun calculateFolderSize(file: File): Long {
        if (!file.exists()) return 0
        return if (file.isFile) {
            file.length()
        } else {
            file.listFiles()?.sumOf { calculateFolderSize(it) } ?: 0
        }
    }
    
    private fun countFiles(file: File): Int {
        if (!file.exists()) return 0
        return if (file.isFile) {
            1
        } else {
            file.listFiles()?.sumOf { countFiles(it) } ?: 0
        }
    }
    
    private fun updateTotalSize() {
        val selectedItems = adapter?.getSelectedItems() ?: emptyList()
        val totalSize = selectedItems.sumOf { it.size }
        val fileCount = selectedItems.sumOf { it.fileCount }
        
        tvTotalSize.text = context.getString(
            R.string.total_size_with_files,
            FileUtils.formatFileSize(totalSize),
            fileCount
        )
        
        btnDelete.isEnabled = selectedItems.isNotEmpty()
    }
    
    private fun deleteSelectedData() {
        val selectedItems = adapter?.getSelectedItems() ?: emptyList()
        
        if (selectedItems.isEmpty()) return
        
        ConfirmationDialog.show(
            context,
            R.string.delete_app_data,
            R.string.delete_app_data_message,
            R.string.delete,
            R.string.cancel,
            onPositive = {
                performDelete(selectedItems)
            }
        )
    }
    
    private fun performDelete(items: List<AppDataDir>) {
        progressBar.visibility = View.VISIBLE
        btnDelete.isEnabled = false
        btnCancel.isEnabled = false
        
        executor.execute {
            var success = true
            
            items.forEach { item ->
                try {
                    val file = File(item.path)
                    if (file.exists()) {
                        success = success && file.deleteRecursively()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    success = false
                }
            }
            
            (context as? android.app.Activity)?.runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (success) {
                    Toast.makeText(
                        context,
                        R.string.app_data_deleted,
                        Toast.LENGTH_SHORT
                    ).show()
                    onDataDeleted.invoke(true)
                    dismiss()
                } else {
                    Toast.makeText(
                        context,
                        R.string.delete_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    onDataDeleted.invoke(false)
                }
            }
        }
    }
    
    inner class AppDataAdapter(
        private val onItemChecked: () -> Unit
    ) : RecyclerView.Adapter<AppDataAdapter.ViewHolder>() {
        
        private var items = listOf<AppDataDir>()
        
        fun submitList(newItems: List<AppDataDir>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        fun getSelectedItems(): List<AppDataDir> {
            return items.filter { it.isSelected }
        }
        
        fun selectAll(selected: Boolean) {
            items = items.map { it.copy(isSelected = selected) }
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_data, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cbSelect: MaterialCheckBox = itemView.findViewById(R.id.cbSelect)
            private val tvName: TextView = itemView.findViewById(R.id.tvName)
            private val tvPath: TextView = itemView.findViewById(R.id.tvPath)
            private val tvSize: TextView = itemView.findViewById(R.id.tvSize)
            private val tvFiles: TextView = itemView.findViewById(R.id.tvFiles)
            private val tvModified: TextView = itemView.findViewById(R.id.tvModified)
            private val ivTypeIcon: ImageView = itemView.findViewById(R.id.ivTypeIcon)
            
            fun bind(item: AppDataDir) {
                cbSelect.isChecked = item.isSelected
                tvName.text = item.name
                tvPath.text = item.path
                tvSize.text = FileUtils.formatFileSize(item.size)
                tvFiles.text = itemView.context.getString(R.string.files_count, item.fileCount)
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                tvModified.text = dateFormat.format(Date(item.lastModified))
                
                // Set icon based on type
                ivTypeIcon.setImageResource(
                    when (item.type) {
                        "internal" -> R.drawable.ic_internal_storage
                        "external" -> R.drawable.ic_sd_card
                        "cache" -> R.drawable.ic_cache
                        "code_cache" -> R.drawable.ic_code
                        else -> R.drawable.ic_folder
                    }
                )
                
                cbSelect.setOnCheckedChangeListener { _, isChecked ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        items[position] = item.copy(isSelected = isChecked)
                        onItemChecked.invoke()
                    }
                }
                
                itemView.setOnClickListener {
                    cbSelect.isChecked = !cbSelect.isChecked
                }
            }
        }
    }
    
    companion object {
        fun show(
            context: Context,
            packageName: String,
            appName: String,
            onDataDeleted: (Boolean) -> Unit
        ) {
            DeleteAppDataDialog(context, packageName, appName, onDataDeleted).show()
        }
    }
}
