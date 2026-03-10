package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.FilePickerItemsAdapter
import com.exory550.exoryfilemanager.extensions.showToast
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.models.StorageInfo
import com.exory550.exoryfilemanager.utils.FileUtils
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.exory550.exoryfilemanager.utils.StorageUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FilePickerDialog(
    context: Context,
    private val config: Config
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog_FullScreen) {

    data class Config(
        val title: String = context.getString(R.string.select_file),
        val initialPath: String = Environment.getExternalStorageDirectory().absolutePath,
        val mode: Int = MODE_FILE_AND_FOLDER,
        val fileTypes: Array<String> = emptyArray(),
        val mimeTypes: Array<String> = emptyArray(),
        val allowMultiple: Boolean = false,
        val maxSelection: Int = Int.MAX_VALUE,
        val showHidden: Boolean = false,
        val canCreateFolder: Boolean = true,
        val canNavigateUp: Boolean = true,
        val showRecent: Boolean = true,
        val showFavorites: Boolean = true,
        val showStorageInfo: Boolean = true,
        val onFileSelected: (List<File>) -> Unit,
        val onCancel: (() -> Unit)? = null
    ) {
        companion object {
            const val MODE_FILE = 0
            const val MODE_FOLDER = 1
            const val MODE_FILE_AND_FOLDER = 2
        }
    }

    private lateinit var binding: View
    private lateinit var searchBar: SearchBar
    private lateinit var searchView: SearchView
    private lateinit var tabLayout: TabLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: View
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var fabConfirm: FloatingActionButton
    private lateinit var fabNavigateUp: FloatingActionButton
    private lateinit var fabNewFolder: FloatingActionButton
    private lateinit var storageInfoView: View
    private lateinit var storageProgress: ProgressBar
    private lateinit var storageUsedText: TextView
    private lateinit var storageFreeText: TextView
    
    private lateinit var fileUtils: FileUtils
    private lateinit var storageUtils: StorageUtils
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var adapter: FilePickerItemsAdapter
    
    private var currentPath: String = config.initialPath
    private var currentFiles: List<ExoryFileItem> = emptyList()
    private var selectedFiles = mutableListOf<File>()
    private var isSearching = false
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadDirectory(currentPath)
        } else {
            showPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        setupViews()
        setupInsets()
        setupToolbar()
        setupTabLayout()
        setupRecyclerView()
        setupFabs()
        setupStorageInfo()
        setupSearch()
        
        checkPermissionsAndLoad()
    }

    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_file_picker, null)
        setContentView(binding)
        
        searchBar = binding.findViewById(R.id.searchBar)
        searchView = binding.findViewById(R.id.searchView)
        tabLayout = binding.findViewById(R.id.tabLayout)
        chipGroup = binding.findViewById(R.id.chipGroup)
        recyclerView = binding.findViewById(R.id.recyclerView)
        emptyView = binding.findViewById(R.id.emptyView)
        progressBar = binding.findViewById(R.id.progressBar)
        fabConfirm = binding.findViewById(R.id.fabConfirm)
        fabNavigateUp = binding.findViewById(R.id.fabNavigateUp)
        fabNewFolder = binding.findViewById(R.id.fabNewFolder)
        storageInfoView = binding.findViewById(R.id.storageInfoView)
        storageProgress = binding.findViewById(R.id.storageProgress)
        storageUsedText = binding.findViewById(R.id.storageUsedText)
        storageFreeText = binding.findViewById(R.id.storageFreeText)
        
        // Set dialog window properties
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // Update FAB visibility
        fabConfirm.hide()
        updateNavigationFab()
        
        // Hide new folder if not allowed
        if (!config.canCreateFolder) {
            fabNewFolder.hide()
        }
        
        // Hide storage info if not needed
        if (!config.showStorageInfo) {
            storageInfoView.visibility = View.GONE
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            view.updatePadding(
                top = statusBars.top,
                bottom = navigationBars.bottom
            )
            
            insets
        }
    }

    private fun setupToolbar() {
        searchBar.setText(config.title)
        searchBar.setNavigationOnClickListener {
            dismiss()
            config.onCancel?.invoke()
        }
        
        searchBar.inflateMenu(R.menu.menu_file_picker)
        searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_select_all -> {
                    selectAll()
                    true
                }
                R.id.action_clear_selection -> {
                    clearSelection()
                    true
                }
                R.id.action_settings -> {
                    showSettings()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.current_directory))
        if (config.showRecent) {
            tabLayout.addTab(tabLayout.newTab().setText(R.string.recent))
        }
        if (config.showFavorites) {
            tabLayout.addTab(tabLayout.newTab().setText(R.string.favorites))
        }
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showCurrentDirectory()
                    1 -> if (config.showRecent) showRecentFiles()
                    2 -> if (config.showFavorites) showFavorites()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = FilePickerItemsAdapter(
            onItemClick = { fileItem ->
                when {
                    fileItem.isDirectory -> {
                        if (config.mode == Config.MODE_FOLDER) {
                            toggleFileSelection(File(fileItem.path))
                        } else {
                            navigateToDirectory(fileItem.path)
                        }
                    }
                    config.mode != Config.MODE_FOLDER -> {
                        toggleFileSelection(File(fileItem.path))
                    }
                }
            },
            onItemLongClick = { fileItem ->
                if (!fileItem.isDirectory || config.mode == Config.MODE_FOLDER) {
                    toggleFileSelection(File(fileItem.path))
                }
                true
            }
        )
        
        adapter.setShowCheckboxes(config.allowMultiple)
        adapter.setSingleClickSelect(config.mode == Config.MODE_FOLDER)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupFabs() {
        fabConfirm.setOnClickListener {
            if (selectedFiles.isNotEmpty()) {
                config.onFileSelected(selectedFiles)
                dismiss()
            }
        }
        
        fabNavigateUp.setOnClickListener {
            navigateUp()
        }
        
        fabNewFolder.setOnClickListener {
            showCreateFolderDialog()
        }
    }

    private fun setupStorageInfo() {
        updateStorageInfo()
    }

    private fun setupSearch() {
        searchView.setupWithSearchBar(searchBar)
        searchView.editText.setOnEditorActionListener { _, _, _ ->
            performSearch(searchView.text.toString())
            true
        }
        
        searchView.addTransitionListener { searchView, previousState, newState ->
            when (newState) {
                SearchView.TransitionState.SHOWN -> isSearching = true
                SearchView.TransitionState.HIDDEN -> {
                    isSearching = false
                    loadDirectory(currentPath)
                }
                else -> {}
            }
        }
    }

    private fun checkPermissionsAndLoad() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (Environment.isExternalStorageManager()) {
                    loadDirectory(currentPath)
                } else {
                    requestManageStoragePermission()
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val permissions = mutableListOf<String>()
                if (ContextCompat.checkSelfPermission(context, 
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (permissions.isNotEmpty()) {
                    requestPermissionLauncher.launch(permissions.toTypedArray())
                } else {
                    loadDirectory(currentPath)
                }
            }
            else -> {
                loadDirectory(currentPath)
            }
        }
    }

    private fun requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            (context as? androidx.fragment.app.FragmentActivity)?.let { activity ->
                activity.resultRegistry.register("manage_storage", 
                    ActivityResultContracts.StartActivityForResult()) {
                    if (Environment.isExternalStorageManager()) {
                        loadDirectory(currentPath)
                    } else {
                        showPermissionDenied()
                    }
                }.launch(intent)
            }
        }
    }

    private fun loadDirectory(path: String) {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
        
        (context as? androidx.lifecycle.LifecycleOwner)?.lifecycleScope?.launch {
            try {
                val files = withContext(Dispatchers.IO) {
                    fileUtils.getFilesInDirectory(path, config.showHidden)
                        .filter { file ->
                            when (config.mode) {
                                Config.MODE_FILE -> !file.isDirectory
                                Config.MODE_FOLDER -> file.isDirectory
                                else -> true
                            }
                        }
                        .filter { file ->
                            config.fileTypes.isEmpty() || 
                            config.fileTypes.any { file.name.endsWith(it, ignoreCase = true) }
                        }
                        .filter { file ->
                            config.mimeTypes.isEmpty() || 
                            file.mimeType?.let { mime ->
                                config.mimeTypes.any { mime.startsWith(it) }
                            } ?: true
                        }
                }
                
                currentPath = path
                currentFiles = files
                
                updatePathChips()
                updateUI()
                updateStorageInfo()
                
            } catch (e: Exception) {
                showError(context.getString(R.string.error_loading_directory))
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI() {
        adapter.submitList(currentFiles)
        
        if (currentFiles.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
        
        updateNavigationFab()
    }

    private fun updatePathChips() {
        chipGroup.removeAllViews()
        
        val parts = currentPath.split(File.separator).filter { it.isNotEmpty() }
        var cumulativePath = ""
        
        // Add root chip
        val rootChip = Chip(context).apply {
            text = context.getString(R.string.root)
            isClickable = true
            setOnClickListener {
                navigateToDirectory(File.separator)
            }
        }
        chipGroup.addView(rootChip)
        
        // Add path parts
        parts.forEach { part ->
            cumulativePath += File.separator + part
            val chip = Chip(context).apply {
                text = part
                isClickable = true
                setOnClickListener {
                    navigateToDirectory(cumulativePath)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun updateNavigationFab() {
        if (config.canNavigateUp && File(currentPath).parentFile != null) {
            fabNavigateUp.show()
        } else {
            fabNavigateUp.hide()
        }
    }

    private fun updateStorageInfo() {
        (context as? androidx.lifecycle.LifecycleOwner)?.lifecycleScope?.launch {
            val info = withContext(Dispatchers.IO) {
                storageUtils.getStorageInfo(currentPath)
            }
            
            storageProgress.progress = info.usedPercentage
            storageUsedText.text = context.getString(R.string.storage_used,
                fileUtils.formatFileSize(info.used),
                fileUtils.formatFileSize(info.total))
            storageFreeText.text = context.getString(R.string.storage_free,
                fileUtils.formatFileSize(info.free))
        }
    }

    private fun navigateToDirectory(path: String) {
        loadDirectory(path)
    }

    private fun navigateUp() {
        val parent = File(currentPath).parentFile
        if (parent != null) {
            loadDirectory(parent.absolutePath)
        }
    }

    private fun toggleFileSelection(file: File) {
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
        } else {
            if (!config.allowMultiple) {
                selectedFiles.clear()
            }
            if (selectedFiles.size < config.maxSelection) {
                selectedFiles.add(file)
            } else {
                context.showToast(context.getString(R.string.max_selection_reached, config.maxSelection))
            }
        }
        
        updateSelectionUI()
    }

    private fun selectAll() {
        selectedFiles.clear()
        currentFiles.forEach { fileItem ->
            if (selectedFiles.size < config.maxSelection) {
                selectedFiles.add(File(fileItem.path))
            }
        }
        updateSelectionUI()
    }

    private fun clearSelection() {
        selectedFiles.clear()
        updateSelectionUI()
    }

    private fun updateSelectionUI() {
        adapter.selectedFiles = selectedFiles.map { it.absolutePath }
        
        if (selectedFiles.isNotEmpty()) {
            fabConfirm.show()
            val count = selectedFiles.size
            fabConfirm.text = context.getString(R.string.select_count, count)
        } else {
            fabConfirm.hide()
        }
    }

    private fun showCurrentDirectory() {
        loadDirectory(currentPath)
    }

    private fun showRecentFiles() {
        // Implement recent files
    }

    private fun showFavorites() {
        // Implement favorites
    }

    private fun performSearch(query: String) {
        if (query.length < 3) return
        
        progressBar.visibility = View.VISIBLE
        
        (context as? androidx.lifecycle.LifecycleOwner)?.lifecycleScope?.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    fileUtils.searchFiles(currentPath, query, config.showHidden)
                }
                adapter.submitList(results)
                
                if (results.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showCreateFolderDialog() {
        CreateNewFolderDialog.show(context, currentPath, currentFiles.map { it.name }) { folderName ->
            createFolder(folderName)
        }
    }

    private fun createFolder(name: String) {
        (context as? androidx.lifecycle.LifecycleOwner)?.lifecycleScope?.launch {
            val success = withContext(Dispatchers.IO) {
                File(currentPath, name).mkdir()
            }
            if (success) {
                loadDirectory(currentPath)
            } else {
                showError(context.getString(R.string.folder_create_error))
            }
        }
    }

    private fun showSettings() {
        // Show settings dialog
    }

    private fun showPermissionDenied() {
        Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_LONG).show()
        dismiss()
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    class Builder(private val context: Context) {
        private var title: String = context.getString(R.string.select_file)
        private var initialPath: String = Environment.getExternalStorageDirectory().absolutePath
        private var mode: Int = Config.MODE_FILE_AND_FOLDER
        private var fileTypes: Array<String> = emptyArray()
        private var mimeTypes: Array<String> = emptyArray()
        private var allowMultiple: Boolean = false
        private var maxSelection: Int = Int.MAX_VALUE
        private var showHidden: Boolean = false
        private var canCreateFolder: Boolean = true
        private var canNavigateUp: Boolean = true
        private var showRecent: Boolean = true
        private var showFavorites: Boolean = true
        private var showStorageInfo: Boolean = true
        private var onFileSelected: (List<File>) -> Unit = {}
        private var onCancel: (() -> Unit)? = null

        fun setTitle(title: String) = apply { this.title = title }
        fun setInitialPath(path: String) = apply { this.initialPath = path }
        fun setMode(mode: Int) = apply { this.mode = mode }
        fun setFileTypes(vararg types: String) = apply { this.fileTypes = types }
        fun setMimeTypes(vararg types: String) = apply { this.mimeTypes = types }
        fun setAllowMultiple(allow: Boolean) = apply { this.allowMultiple = allow }
        fun setMaxSelection(max: Int) = apply { this.maxSelection = max }
        fun setShowHidden(show: Boolean) = apply { this.showHidden = show }
        fun setCanCreateFolder(can: Boolean) = apply { this.canCreateFolder = can }
        fun setCanNavigateUp(can: Boolean) = apply { this.canNavigateUp = can }
        fun setShowRecent(show: Boolean) = apply { this.showRecent = show }
        fun setShowFavorites(show: Boolean) = apply { this.showFavorites = show }
        fun setShowStorageInfo(show: Boolean) = apply { this.showStorageInfo = show }
        fun setOnFileSelected(listener: (List<File>) -> Unit) = apply { this.onFileSelected = listener }
        fun setOnCancel(listener: () -> Unit) = apply { this.onCancel = listener }

        fun build(): Config {
            return Config(
                title = title,
                initialPath = initialPath,
                mode = mode,
                fileTypes = fileTypes,
                mimeTypes = mimeTypes,
                allowMultiple = allowMultiple,
                maxSelection = maxSelection,
                showHidden = showHidden,
                canCreateFolder = canCreateFolder,
                canNavigateUp = canNavigateUp,
                showRecent = showRecent,
                showFavorites = showFavorites,
                showStorageInfo = showStorageInfo,
                onFileSelected = onFileSelected,
                onCancel = onCancel
            )
        }

        fun show() {
            FilePickerDialog(context, build()).show()
        }
    }

    companion object {
        fun show(context: Context, config: Config.() -> Unit) {
            val builder = Builder(context)
            config.invoke(builder)
            builder.show()
        }
    }
}
