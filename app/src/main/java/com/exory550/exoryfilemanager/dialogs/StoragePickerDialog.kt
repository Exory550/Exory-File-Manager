package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.models.StorageInfo
import com.exory550.exoryfilemanager.utils.FileUtils
import com.exory550.exoryfilemanager.utils.StorageUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.io.File

class StoragePickerDialog(
    context: Context,
    private val config: Config
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    data class Config(
        val title: String = context.getString(R.string.select_storage),
        val mode: Int = MODE_ALL,
        val showInternal: Boolean = true,
        val showExternal: Boolean = true,
        val showUsb: Boolean = true,
        val showCloud: Boolean = false,
        val showSize: Boolean = true,
        val allowSelectMultiple: Boolean = false,
        val requireWriteAccess: Boolean = false,
        val requirePersistentAccess: Boolean = false,
        val onStorageSelected: (List<StorageInfo>) -> Unit,
        val onCancel: (() -> Unit)? = null
    ) {
        companion object {
            const val MODE_ALL = 0
            const val MODE_READ = 1
            const val MODE_WRITE = 2
            const val MODE_INSTALL = 3
        }
    }

    private lateinit var binding: View
    private lateinit var tvTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StorageAdapter
    private lateinit var btnSelect: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnClose: ImageButton
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var tvEmpty: TextView
    private lateinit var btnGrantAccess: Button
    
    private lateinit var storageUtils: StorageUtils
    private lateinit var fileUtils: FileUtils
    
    private var selectedStorages = mutableListOf<StorageInfo>()
    private var storages = listOf<StorageInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        storageUtils = StorageUtils.getInstance()
        fileUtils = FileUtils.getInstance()

        setupViews()
        loadStorages()
        setupListeners()
    }

    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_storage_picker, null)
        setContentView(binding)

        tvTitle = binding.findViewById(R.id.tvTitle)
        recyclerView = binding.findViewById(R.id.recyclerView)
        btnSelect = binding.findViewById(R.id.btnSelect)
        btnCancel = binding.findViewById(R.id.btnCancel)
        btnClose = binding.findViewById(R.id.btnClose)
        progressBar = binding.findViewById(R.id.progressBar)
        tvEmpty = binding.findViewById(R.id.tvEmpty)
        btnGrantAccess = binding.findViewById(R.id.btnGrantAccess)

        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )

        tvTitle.text = config.title

        // Setup RecyclerView
        adapter = StorageAdapter(
            storages = storages,
            mode = config.mode,
            showSize = config.showSize,
            allowMultiple = config.allowSelectMultiple,
            onStorageClick = { storage ->
                toggleStorageSelection(storage)
            },
            onStorageLongClick = { storage ->
                showStorageDetails(storage)
                true
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Update button states
        updateSelectButton()
        
        // Show grant access button for Android 11+ if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && config.requirePersistentAccess) {
            btnGrantAccess.visibility = View.VISIBLE
        } else {
            btnGrantAccess.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        btnSelect.setOnClickListener {
            if (selectedStorages.isNotEmpty()) {
                config.onStorageSelected(selectedStorages)
                dismiss()
            }
        }

        btnCancel.setOnClickListener {
            config.onCancel?.invoke()
            dismiss()
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        btnGrantAccess.setOnClickListener {
            grantStorageAccess()
        }
    }

    private fun loadStorages() {
        showProgress(true)

        val storageList = mutableListOf<StorageInfo>()

        // Internal storage
        if (config.showInternal) {
            val internalStorage = getInternalStorage()
            if (internalStorage != null) {
                storageList.add(internalStorage)
            }
        }

        // External storages (SD cards, USB)
        if (config.showExternal || config.showUsb) {
            storageList.addAll(getExternalStorages())
        }

        // Cloud storages (if enabled)
        if (config.showCloud) {
            storageList.addAll(getCloudStorages())
        }

        storages = storageList.filter { storage ->
            when (config.mode) {
                Config.MODE_READ -> storage.isReadable
                Config.MODE_WRITE -> storage.isWritable && !storage.isReadOnly
                Config.MODE_INSTALL -> storage.isRemovable || storage.isEmulated
                else -> true
            }
        }

        adapter.updateStorages(storages)
        
        tvEmpty.visibility = if (storages.isEmpty()) View.VISIBLE else View.GONE
        showProgress(false)
    }

    private fun getInternalStorage(): StorageInfo? {
        return try {
            val path = Environment.getExternalStorageDirectory().absolutePath
            val file = File(path)
            
            StorageInfo(
                id = "internal",
                path = path,
                description = context.getString(R.string.internal_storage),
                totalSpace = file.totalSpace,
                freeSpace = file.freeSpace,
                usedSpace = file.totalSpace - file.freeSpace,
                isEmulated = true,
                isRemovable = false,
                isReadOnly = false,
                isUsb = false,
                isSd = false,
                volumeId = null,
                uuid = null,
                state = Environment.MEDIA_MOUNTED,
                rootUri = Uri.fromFile(file)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getExternalStorages(): List<StorageInfo> {
        val storages = mutableListOf<StorageInfo>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumes = storageManager.storageVolumes

            storageVolumes.forEach { volume ->
                try {
                    val path = volume.getDirectory()?.absolutePath
                    if (path != null && !path.contains("emulated") && !path.contains("self/primary")) {
                        val file = File(path)
                        
                        val isSd = volume.isRemovable && !isUsbDevice(path)
                        val isUsb = isUsbDevice(path)
                        
                        storages.add(
                            StorageInfo(
                                id = volume.uuid ?: "ext_${storages.size}",
                                path = path,
                                description = getVolumeDescription(volume),
                                totalSpace = file.totalSpace,
                                freeSpace = file.freeSpace,
                                usedSpace = file.totalSpace - file.freeSpace,
                                isEmulated = false,
                                isRemovable = volume.isRemovable,
                                isReadOnly = Environment.getExternalStorageState(file).contains("read-only"),
                                isUsb = isUsb,
                                isSd = isSd,
                                volumeId = volume.uuid,
                                uuid = volume.uuid,
                                state = Environment.getExternalStorageState(file),
                                rootUri = Uri.fromFile(file)
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // For older Android versions, use getExternalFilesDirs
            val externalDirs = context.getExternalFilesDirs(null)
            externalDirs.forEachIndexed { index, dir ->
                if (dir != null) {
                    val path = dir.absolutePath.substringBefore("/Android")
                    val file = File(path)
                    
                    if (file.exists() && !path.contains("emulated")) {
                        storages.add(
                            StorageInfo(
                                id = "ext_$index",
                                path = path,
                                description = context.getString(R.string.external_storage, index + 1),
                                totalSpace = file.totalSpace,
                                freeSpace = file.freeSpace,
                                usedSpace = file.totalSpace - file.freeSpace,
                                isEmulated = false,
                                isRemovable = true,
                                isReadOnly = false,
                                isUsb = isUsbDevice(path),
                                isSd = !isUsbDevice(path),
                                volumeId = null,
                                uuid = null,
                                state = Environment.MEDIA_MOUNTED,
                                rootUri = Uri.fromFile(file)
                            )
                        )
                    }
                }
            }
        }

        return storages
    }

    private fun getCloudStorages(): List<StorageInfo> {
        // Implement cloud storage providers (Google Drive, Dropbox, etc.)
        return emptyList()
    }

    private fun getVolumeDescription(volume: StorageVolume): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            volume.getDescription(context)
        } else {
            @Suppress("DEPRECATION")
            volume.description
        } ?: context.getString(R.string.external_storage)
    }

    private fun isUsbDevice(path: String): Boolean {
        return path.contains("usb", ignoreCase = true) || 
               path.contains("usbotg", ignoreCase = true) ||
               path.contains("otg", ignoreCase = true)
    }

    private fun toggleStorageSelection(storage: StorageInfo) {
        if (config.allowSelectMultiple) {
            if (selectedStorages.contains(storage)) {
                selectedStorages.remove(storage)
            } else {
                selectedStorages.add(storage)
            }
        } else {
            selectedStorages.clear()
            selectedStorages.add(storage)
        }
        
        adapter.updateSelection(selectedStorages)
        updateSelectButton()
    }

    private fun updateSelectButton() {
        btnSelect.isEnabled = selectedStorages.isNotEmpty()
        btnSelect.text = if (selectedStorages.size > 1) {
            context.getString(R.string.select_count, selectedStorages.size)
        } else {
            context.getString(R.string.select)
        }
    }

    private fun showStorageDetails(storage: StorageInfo) {
        val details = """
            ${context.getString(R.string.path)}: ${storage.path}
            ${context.getString(R.string.total_space)}: ${fileUtils.formatFileSize(storage.totalSpace)}
            ${context.getString(R.string.free_space)}: ${fileUtils.formatFileSize(storage.freeSpace)}
            ${context.getString(R.string.used_space)}: ${fileUtils.formatFileSize(storage.usedSpace)}
            ${context.getString(R.string.type)}: ${getStorageType(storage)}
            ${context.getString(R.string.state)}: ${storage.state}
            ${context.getString(R.string.writable)}: ${if (storage.isWritable) context.getString(R.string.yes) else context.getString(R.string.no)}
        """.trimIndent()

        AlertDialog.Builder(context)
            .setTitle(storage.description)
            .setMessage(details)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun getStorageType(storage: StorageInfo): String {
        return when {
            storage.isUsb -> context.getString(R.string.usb_storage)
            storage.isSd -> context.getString(R.string.sd_card)
            storage.isEmulated -> context.getString(R.string.internal_storage)
            else -> context.getString(R.string.external_storage)
        }
    }

    private fun grantStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            (context as? android.app.Activity)?.startActivityForResult(intent, REQUEST_MANAGE_STORAGE)
        }
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    inner class StorageAdapter(
        private var storages: List<StorageInfo>,
        private val mode: Int,
        private val showSize: Boolean,
        private val allowMultiple: Boolean,
        private val onStorageClick: (StorageInfo) -> Unit,
        private val onStorageLongClick: (StorageInfo) -> Boolean
    ) : RecyclerView.Adapter<StorageAdapter.ViewHolder>() {

        private var selectedStorages = listOf<StorageInfo>()

        fun updateStorages(newStorages: List<StorageInfo>) {
            storages = newStorages
            notifyDataSetChanged()
        }

        fun updateSelection(selected: List<StorageInfo>) {
            selectedStorages = selected
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_storage_picker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(storages[position])
        }

        override fun getItemCount(): Int = storages.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
            private val tvName: TextView = itemView.findViewById(R.id.tvName)
            private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
            private val tvSize: TextView = itemView.findViewById(R.id.tvSize)
            private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
            private val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
            private val ivWarning: ImageView = itemView.findViewById(R.id.ivWarning)
            private val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)

            init {
                itemView.setOnClickListener {
                    val storage = storages[adapterPosition]
                    if (isAccessible(storage)) {
                        onStorageClick(storage)
                    } else {
                        showAccessRequiredDialog(storage)
                    }
                }

                itemView.setOnLongClickListener {
                    val storage = storages[adapterPosition]
                    onStorageLongClick(storage)
                }
            }

            fun bind(storage: StorageInfo) {
                tvName.text = storage.description
                tvDescription.text = storage.path

                // Set icon based on storage type
                ivIcon.setImageResource(
                    when {
                        storage.isUsb -> R.drawable.ic_usb
                        storage.isSd -> R.drawable.ic_sd_card
                        storage.isEmulated -> R.drawable.ic_internal_storage
                        else -> R.drawable.ic_storage
                    }
                )

                // Set icon color based on accessibility
                val iconColor = if (isAccessible(storage)) {
                    R.color.primary_color
                } else {
                    R.color.text_disabled
                }
                ivIcon.setColorFilter(
                    ContextCompat.getColor(context, iconColor),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                // Show storage size
                if (showSize) {
                    val usedPercent = (storage.usedSpace * 100 / storage.totalSpace).toInt()
                    progressBar.progress = usedPercent
                    
                    tvSize.text = context.getString(
                        R.string.storage_usage,
                        fileUtils.formatFileSize(storage.usedSpace),
                        fileUtils.formatFileSize(storage.totalSpace),
                        usedPercent
                    )
                    tvSize.visibility = View.VISIBLE
                } else {
                    tvSize.visibility = View.GONE
                }

                // Show warning for inaccessible storage
                if (!isAccessible(storage)) {
                    ivWarning.visibility = View.VISIBLE
                } else {
                    ivWarning.visibility = View.GONE
                }

                // Show selection indicator
                val isSelected = storage in selectedStorages
                if (allowMultiple) {
                    selectionOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE
                    ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
                    ivSelected.setImageResource(R.drawable.ic_check_circle)
                } else {
                    selectionOverlay.visibility = View.GONE
                    ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
                    ivSelected.setImageResource(R.drawable.ic_radio_checked)
                }

                // Dim the item if not accessible
                itemView.alpha = if (isAccessible(storage)) 1.0f else 0.6f
            }

            private fun isAccessible(storage: StorageInfo): Boolean {
                return when (mode) {
                    Config.MODE_WRITE -> storage.isWritable && !storage.isReadOnly
                    else -> true
                }
            }

            private fun showAccessRequiredDialog(storage: StorageInfo) {
                AlertDialog.Builder(context)
                    .setTitle(R.string.access_required)
                    .setMessage(R.string.storage_access_required_message)
                    .setPositiveButton(R.string.grant_access) { _, _ ->
                        grantStorageAccess()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    class Builder(private val context: Context) {
        private var title: String = context.getString(R.string.select_storage)
        private var mode: Int = Config.MODE_ALL
        private var showInternal: Boolean = true
        private var showExternal: Boolean = true
        private var showUsb: Boolean = true
        private var showCloud: Boolean = false
        private var showSize: Boolean = true
        private var allowSelectMultiple: Boolean = false
        private var requireWriteAccess: Boolean = false
        private var requirePersistentAccess: Boolean = false
        private var onStorageSelected: (List<StorageInfo>) -> Unit = {}
        private var onCancel: (() -> Unit)? = null

        fun setTitle(title: String) = apply { this.title = title }
        fun setMode(mode: Int) = apply { this.mode = mode }
        fun setShowInternal(show: Boolean) = apply { this.showInternal = show }
        fun setShowExternal(show: Boolean) = apply { this.showExternal = show }
        fun setShowUsb(show: Boolean) = apply { this.showUsb = show }
        fun setShowCloud(show: Boolean) = apply { this.showCloud = show }
        fun setShowSize(show: Boolean) = apply { this.showSize = show }
        fun setAllowSelectMultiple(allow: Boolean) = apply { this.allowSelectMultiple = allow }
        fun setRequireWriteAccess(require: Boolean) = apply { this.requireWriteAccess = require }
        fun setRequirePersistentAccess(require: Boolean) = apply { this.requirePersistentAccess = require }
        fun setOnStorageSelected(listener: (List<StorageInfo>) -> Unit) = apply { this.onStorageSelected = listener }
        fun setOnCancel(listener: () -> Unit) = apply { this.onCancel = listener }

        fun build(): Config {
            return Config(
                title = title,
                mode = mode,
                showInternal = showInternal,
                showExternal = showExternal,
                showUsb = showUsb,
                showCloud = showCloud,
                showSize = showSize,
                allowSelectMultiple = allowSelectMultiple,
                requireWriteAccess = requireWriteAccess,
                requirePersistentAccess = requirePersistentAccess,
                onStorageSelected = onStorageSelected,
                onCancel = onCancel
            )
        }

        fun show() {
            StoragePickerDialog(context, build()).show()
        }
    }

    companion object {
        const val REQUEST_MANAGE_STORAGE = 1001

        fun show(context: Context, config: Config.() -> Unit) {
            val builder = Builder(context)
            config.invoke(builder)
            builder.show()
        }

        fun showForWriteAccess(
            context: Context,
            onStorageSelected: (StorageInfo) -> Unit
        ) {
            Builder(context)
                .setTitle(R.string.select_storage_for_write)
                .setMode(Config.MODE_WRITE)
                .setRequireWriteAccess(true)
                .setAllowSelectMultiple(false)
                .setOnStorageSelected { storages ->
                    if (storages.isNotEmpty()) {
                        onStorageSelected(storages[0])
                    }
                }
                .show()
        }
    }
}
