package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.FileAdapter
import com.exory550.exoryfilemanager.adapters.NavigationDrawerAdapter
import com.exory550.exoryfilemanager.adapters.StorageAdapter
import com.exory550.exoryfilemanager.databinding.ActivityMainBinding
import com.exory550.exoryfilemanager.dialogs.CreateFolderDialog
import com.exory550.exoryfilemanager.dialogs.FilePropertiesDialog
import com.exory550.exoryfilemanager.dialogs.RenameDialog
import com.exory550.exoryfilemanager.extensions.*
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.models.NavDrawerItem
import com.exory550.exoryfilemanager.models.StorageInfo
import com.exory550.exoryfilemanager.tasks.DeleteTask
import com.exory550.exoryfilemanager.tasks.FileOperationTask
import com.exory550.exoryfilemanager.utils.Constants
import com.exory550.exoryfilemanager.utils.FileUtils
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.exory550.exoryfilemanager.utils.StorageUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class MainActivity : BaseAbstractActivity(), 
    FileAdapter.OnFileClickListener,
    FileAdapter.OnFileLongClickListener,
    NavigationDrawerAdapter.OnNavigationItemClickListener,
    StorageAdapter.OnStorageItemClickListener {

    override val layoutRes: Int = R.layout.activity_main
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var fileAdapter: FileAdapter
    private lateinit var navAdapter: NavigationDrawerAdapter
    private lateinit var storageAdapter: StorageAdapter
    
    @Inject
    lateinit var fileUtils: FileUtils
    
    @Inject
    lateinit var storageUtils: StorageUtils
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var currentPath: String = Environment.getExternalStorageDirectory().absolutePath
    private var currentFiles: List<FileItem> = emptyList()
    private var selectedFiles: MutableSet<FileItem> = mutableSetOf()
    private var isSelectionMode = false
    private var isSearchMode = false
    private var currentViewMode = VIEW_MODE_LIST
    private var currentSortMode = SORT_BY_NAME
    private var sortAscending = true
    private var clipboard: MutableList<FileItem> = mutableListOf()
    private var clipboardOperation = Constants.COPY_OPERATION
    
    private val searchResults = MutableStateFlow<List<FileItem>>(emptyList())
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadDirectory(currentPath)
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadDirectory(currentPath)
            } else {
                showPermissionDeniedDialog()
            }
        }
    }
    
    override fun initializeViews() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
        setupFAB()
        setupSearch()
        
        checkPermissionsAndLoad()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = ""
        }
        
        binding.toolbar.setNavigationOnClickListener {
            if (isSelectionMode) {
                exitSelectionMode()
            } else if (isSearchMode) {
                exitSearchMode()
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        
        binding.toolbar.setTitle(R.string.app_name)
    }
    
    private fun setupNavigationDrawer() {
        navAdapter = NavigationDrawerAdapter(this)
        
        binding.navView.apply {
            setAdapter(navAdapter)
            setOnItemClickListener(this@MainActivity)
        }
        
        updateNavigationDrawer()
    }
    
    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            onItemClick = this,
            onItemLongClick = this,
            onSelectionChanged = { selectedCount ->
                updateSelectionModeTitle(selectedCount)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = if (currentViewMode == VIEW_MODE_LIST) {
                LinearLayoutManager(this@MainActivity)
            } else {
                GridLayoutManager(this@MainActivity, 2)
            }
            adapter = fileAdapter
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshDirectory()
        }
    }
    
    private fun setupFAB() {
        binding.fabAdd.setOnClickListener {
            showCreateOptions()
        }
    }
    
    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : 
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.length >= 3) {
                        performSearch(it)
                    } else if (it.isEmpty()) {
                        exitSearchMode()
                    }
                }
                return true
            }
        })
        
        binding.searchView.setOnCloseListener {
            exitSearchMode()
            true
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
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            intent.data = Uri.parse("package:$packageName")
            manageStorageLauncher.launch(intent)
        }
    }
    
    private fun loadDirectory(path: String) {
        lifecycleScope.launch {
            showProgress(R.string.loading)
            
            try {
                val files = withContext(Dispatchers.IO) {
                    fileUtils.getFilesInDirectory(path)
                }
                
                currentPath = path
                currentFiles = files
                
                applySorting()
                updateUI()
                
                binding.toolbar.title = File(path).name ?: getString(R.string.root)
                
            } catch (e: Exception) {
                showErrorSnackbar(R.string.error_loading_directory)
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun updateUI() {
        fileAdapter.submitList(currentFiles)
        
        binding.apply {
            if (currentFiles.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.root.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.root.visibility = View.GONE
            }
            
            pathTextView.text = currentPath
            fileCountTextView.text = getString(R.string.files_count, currentFiles.size)
        }
        
        binding.swipeRefreshLayout.isRefreshing = false
        updateStorageInfo()
        updateNavigationDrawer()
    }
    
    private fun updateStorageInfo() {
        lifecycleScope.launch {
            val storageInfo = withContext(Dispatchers.IO) {
                storageUtils.getStorageInfo(currentPath)
            }
            
            binding.storageInfo.apply {
                storageBar.progress = storageInfo.usedPercentage
                storageUsedText.text = getString(R.string.storage_used,
                    fileUtils.formatFileSize(storageInfo.used),
                    fileUtils.formatFileSize(storageInfo.total))
                storageFreeText.text = getString(R.string.storage_free,
                    fileUtils.formatFileSize(storageInfo.free))
            }
        }
    }
    
    private fun updateNavigationDrawer() {
        lifecycleScope.launch {
            val navItems = withContext(Dispatchers.IO) {
                storageUtils.getNavigationItems()
            }
            navAdapter.submitList(navItems)
        }
    }
    
    private fun refreshDirectory() {
        loadDirectory(currentPath)
    }
    
    private fun navigateToParent() {
        val parentFile = File(currentPath).parentFile
        if (parentFile != null) {
            loadDirectory(parentFile.absolutePath)
        }
    }
    
    private fun navigateToPath(path: String) {
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            loadDirectory(path)
        } else {
            showErrorSnackbar(R.string.path_not_found)
        }
    }
    
    override fun onFileClick(file: FileItem) {
        when {
            isSelectionMode -> toggleSelection(file)
            file.isDirectory -> loadDirectory(file.path)
            file.isCompressed -> openCompressedFile(file)
            else -> openFile(file)
        }
    }
    
    override fun onFileLongClick(file: FileItem): Boolean {
        if (!isSelectionMode && !isSearchMode) {
            enterSelectionMode()
            toggleSelection(file)
        }
        return true
    }
    
    private fun openFile(file: FileItem) {
        val intent = Intent(this, FileViewerActivity::class.java).apply {
            putExtra(Constants.EXTRA_FILE_PATH, file.path)
            putExtra(Constants.EXTRA_FILE_NAME, file.name)
            putExtra(Constants.EXTRA_FILE_SIZE, file.size)
            putExtra(Constants.EXTRA_MIME_TYPE, file.mimeType)
        }
        startActivityWithAnimation(intent)
    }
    
    private fun openCompressedFile(file: FileItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.open_compressed_file)
            .setMessage(R.string.open_compressed_file_message)
            .setPositiveButton(R.string.open) { _, _ ->
                val intent = DecompressActivity.getIntent(this, file.path)
                startActivityWithAnimation(intent)
            }
            .setNegativeButton(R.string.extract) { _, _ ->
                extractFile(file)
            }
            .show()
    }
    
    private fun extractFile(file: FileItem) {
        lifecycleScope.launch {
            showProgress(R.string.extracting)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    fileUtils.extractFile(file, currentPath)
                }
                
                if (success) {
                    showSuccessSnackbar(R.string.extract_success)
                    refreshDirectory()
                } else {
                    showErrorSnackbar(R.string.extract_failed)
                }
            } catch (e: Exception) {
                showErrorSnackbar(R.string.extract_failed)
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun enterSelectionMode() {
        isSelectionMode = true
        selectedFiles.clear()
        fileAdapter.setSelectionMode(true)
        supportActionBar?.setDisplayHomeAsUpIndicator(R.drawable.ic_close)
        invalidateOptionsMenu()
        binding.fabAdd.hide()
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedFiles.clear()
        fileAdapter.setSelectionMode(false)
        supportActionBar?.setDisplayHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.title = getString(R.string.app_name)
        invalidateOptionsMenu()
        binding.fabAdd.show()
    }
    
    private fun enterSearchMode() {
        isSearchMode = true
        binding.toolbar.visibility = View.GONE
        binding.searchContainer.visibility = View.VISIBLE
        binding.searchView.setQuery("", false)
        binding.searchView.requestFocus()
        invalidateOptionsMenu()
    }
    
    private fun exitSearchMode() {
        isSearchMode = false
        binding.toolbar.visibility = View.VISIBLE
        binding.searchContainer.visibility = View.GONE
        binding.searchView.setQuery("", false)
        hideKeyboard()
        loadDirectory(currentPath)
        invalidateOptionsMenu()
    }
    
    private fun toggleSelection(file: FileItem) {
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
        } else {
            selectedFiles.add(file)
        }
        fileAdapter.notifyItemChanged(currentFiles.indexOf(file))
        updateSelectionModeTitle(selectedFiles.size)
    }
    
    private fun selectAll() {
        selectedFiles.clear()
        selectedFiles.addAll(currentFiles)
        fileAdapter.notifyDataSetChanged()
        updateSelectionModeTitle(selectedFiles.size)
    }
    
    private fun clearSelection() {
        selectedFiles.clear()
        fileAdapter.notifyDataSetChanged()
        updateSelectionModeTitle(0)
    }
    
    private fun updateSelectionModeTitle(count: Int) {
        if (isSelectionMode) {
            supportActionBar?.title = getString(R.string.selected_count, count)
        }
    }
    
    private fun showCreateOptions() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.create_new)
            .setItems(R.array.create_options) { _, which ->
                when (which) {
                    0 -> showCreateFolderDialog()
                    1 -> showCreateFileDialog()
                    2 -> showCreateShortcutDialog()
                }
            }
            .show()
    }
    
    private fun showCreateFolderDialog() {
        CreateFolderDialog(this) { folderName ->
            createFolder(folderName)
        }.show()
    }
    
    private fun createFolder(name: String) {
        lifecycleScope.launch {
            showProgress(R.string.creating)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    fileUtils.createFolder(currentPath, name)
                }
                
                if (success) {
                    showSuccessSnackbar(R.string.folder_created)
                    refreshDirectory()
                } else {
                    showErrorSnackbar(R.string.folder_create_error)
                }
            } catch (e: Exception) {
                showErrorSnackbar(R.string.folder_create_error)
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun showCreateFileDialog() {
        // Implement file creation dialog
        showWarningSnackbar(R.string.coming_soon)
    }
    
    private fun showCreateShortcutDialog() {
        // Implement shortcut creation
        showWarningSnackbar(R.string.coming_soon)
    }
    
    private fun performSearch(query: String) {
        lifecycleScope.launch {
            showProgress(R.string.searching)
            
            try {
                val results = withContext(Dispatchers.IO) {
                    fileUtils.searchFiles(currentPath, query)
                }
                
                searchResults.value = results
                fileAdapter.submitList(results)
                
                binding.apply {
                    if (results.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyView.root.visibility = View.VISIBLE
                        emptyView.emptyText.text = getString(R.string.no_search_results, query)
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.root.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                showErrorSnackbar(R.string.search_error)
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun showDeleteDialog() {
        if (selectedFiles.isEmpty()) return
        
        val message = if (selectedFiles.size == 1) {
            getString(R.string.delete_single_message, selectedFiles.first().name)
        } else {
            getString(R.string.delete_multiple_message, selectedFiles.size)
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete)
            .setMessage(message)
            .setPositiveButton(R.string.delete) { _, _ ->
                performDelete()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun performDelete() {
        lifecycleScope.launch {
            showProgress(R.string.deleting)
            
            val task = DeleteTask(this@MainActivity)
            val results = task.execute(selectedFiles.toList())
            
            dismissProgress()
            
            val successCount = results.count { it }
            if (successCount == selectedFiles.size) {
                showSuccessSnackbar(R.string.delete_success)
            } else {
                showErrorSnackbar(R.string.delete_error)
            }
            
            exitSelectionMode()
            refreshDirectory()
        }
    }
    
    private fun showRenameDialog() {
        if (selectedFiles.size != 1) return
        
        val file = selectedFiles.first()
        
        RenameDialog(this, file.name) { newName ->
            renameFile(file, newName)
        }.show()
    }
    
    private fun renameFile(file: FileItem, newName: String) {
        lifecycleScope.launch {
            showProgress(R.string.renaming)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    fileUtils.renameFile(file, newName)
                }
                
                if (success) {
                    showSuccessSnackbar(R.string.rename_success)
                    exitSelectionMode()
                    refreshDirectory()
                } else {
                    showErrorSnackbar(R.string.rename_error)
                }
            } catch (e: Exception) {
                showErrorSnackbar(R.string.rename_error)
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun showCopyDialog() {
        if (selectedFiles.isEmpty()) return
        
        clipboard.clear()
        clipboard.addAll(selectedFiles)
        clipboardOperation = Constants.COPY_OPERATION
        
        showSnackbar(
            getString(R.string.copied_to_clipboard, selectedFiles.size),
            Snackbar.LENGTH_LONG,
            getString(R.string.paste)
        ) {
            showPasteOptions()
        }
        
        exitSelectionMode()
    }
    
    private fun showCutDialog() {
        if (selectedFiles.isEmpty()) return
        
        clipboard.clear()
        clipboard.addAll(selectedFiles)
        clipboardOperation = Constants.MOVE_OPERATION
        
        showSnackbar(
            getString(R.string.cut_to_clipboard, selectedFiles.size),
            Snackbar.LENGTH_LONG,
            getString(R.string.paste)
        ) {
            showPasteOptions()
        }
        
        exitSelectionMode()
    }
    
    private fun showPasteOptions() {
        if (clipboard.isEmpty()) {
            showWarningSnackbar(R.string.clipboard_empty)
            return
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.paste)
            .setMessage(getString(R.string.paste_message, clipboard.size))
            .setPositiveButton(R.string.paste) { _, _ ->
                performPaste()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun performPaste() {
        lifecycleScope.launch {
            showProgress(R.string.pasting)
            
            val task = FileOperationTask(this@MainActivity)
            val results = task.execute(clipboard.toList(), currentPath, clipboardOperation)
            
            dismissProgress()
            
            val successCount = results.count { it }
            if (successCount == clipboard.size) {
                showSuccessSnackbar(R.string.paste_success)
                clipboard.clear()
                refreshDirectory()
            } else {
                showErrorSnackbar(R.string.paste_error)
            }
        }
    }
    
    private fun showPropertiesDialog() {
        if (selectedFiles.size != 1) {
            showPropertiesForMultiple()
            return
        }
        
        val file = selectedFiles.first()
        FilePropertiesDialog(this, file).show()
    }
    
    private fun showPropertiesForMultiple() {
        lifecycleScope.launch {
            val totalSize = withContext(Dispatchers.IO) {
                selectedFiles.sumOf { it.size }
            }
            
            val message = buildString {
                appendLine(getString(R.string.items_count, selectedFiles.size))
                appendLine(getString(R.string.total_size, fileUtils.formatFileSize(totalSize)))
                appendLine()
                selectedFiles.take(10).forEachIndexed { index, file ->
                    appendLine("${index + 1}. ${file.name}")
                }
                if (selectedFiles.size > 10) {
                    appendLine(getString(R.string.and_more, selectedFiles.size - 10))
                }
            }
            
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle(R.string.properties)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show()
        }
    }
    
    private fun showShareDialog() {
        if (selectedFiles.isEmpty()) return
        
        val uris = selectedFiles.map { file ->
            FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                File(file.path)
            )
        }.toTypedArray()
        
        val intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris.toList()))
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }
    
    private fun compressSelected() {
        if (selectedFiles.isEmpty()) return
        
        // Implement compression
        showWarningSnackbar(R.string.coming_soon)
    }
    
    private fun addToFavorites() {
        if (selectedFiles.isEmpty()) return
        
        lifecycleScope.launch {
            val results = selectedFiles.map { file ->
                fileUtils.addToFavorites(file)
            }
            
            val successCount = results.count { it }
            if (successCount == selectedFiles.size) {
                showSuccessSnackbar(R.string.added_to_favorites)
            } else {
                showErrorSnackbar(R.string.favorite_add_error)
            }
            
            exitSelectionMode()
        }
    }
    
    private fun applySorting() {
        currentFiles = when (currentSortMode) {
            SORT_BY_NAME -> currentFiles.sortedWith(
                compareBy(if (sortAscending) { it.name.lowercase() } else { { it.name.lowercase().reversed() } })
            )
            SORT_BY_SIZE -> currentFiles.sortedWith(
                compareBy(if (sortAscending) { it.size } else { { -it.size } })
            )
            SORT_BY_DATE -> currentFiles.sortedWith(
                compareBy(if (sortAscending) { it.lastModified } else { { -it.lastModified } })
            )
            SORT_BY_TYPE -> currentFiles.sortedWith(
                compareBy(if (sortAscending) { it.extension } else { { it.extension.reversed() } })
            )
            else -> currentFiles
        }
        
        // Folders first
        val (folders, files) = currentFiles.partition { it.isDirectory }
        currentFiles = folders + files
    }
    
    private fun showSortDialog() {
        val sortOptions = arrayOf(
            getString(R.string.sort_by_name),
            getString(R.string.sort_by_size),
            getString(R.string.sort_by_date),
            getString(R.string.sort_by_type)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort_by)
            .setSingleChoiceItems(sortOptions, currentSortMode) { _, which ->
                currentSortMode = which
                applySorting()
                fileAdapter.notifyDataSetChanged()
            }
            .setNeutralButton(R.string.sort_order) { _, _ ->
                sortAscending = !sortAscending
                applySorting()
                fileAdapter.notifyDataSetChanged()
            }
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    private fun showViewModeDialog() {
        val viewModes = arrayOf(
            getString(R.string.list_view),
            getString(R.string.grid_view)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.view_mode)
            .setSingleChoiceItems(viewModes, currentViewMode) { _, which ->
                currentViewMode = which
                binding.recyclerView.layoutManager = if (currentViewMode == VIEW_MODE_LIST) {
                    LinearLayoutManager(this)
                } else {
                    GridLayoutManager(this, 2)
                }
            }
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityWithAnimation(intent)
    }
    
    private fun showFavorites() {
        val intent = FavoritesActivity.getIntent(this)
        startActivityWithAnimation(intent)
    }
    
    private fun showAbout() {
        val intent = AboutActivity.getIntent(this)
        startActivityWithAnimation(intent)
    }
    
    override fun onNavigationItemClick(item: NavDrawerItem) {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        
        when (item.id) {
            Constants.NAV_HOME -> navigateToPath(Environment.getExternalStorageDirectory().absolutePath)
            Constants.NAV_DOWNLOADS -> navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)
            Constants.NAV_DOCUMENTS -> navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath)
            Constants.NAV_PICTURES -> navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath)
            Constants.NAV_MUSIC -> navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath)
            Constants.NAV_MOVIES -> navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath)
            Constants.NAV_FAVORITES -> showFavorites()
            Constants.NAV_RECENT -> showRecentFiles()
            Constants.NAV_ANALYZE -> showStorageAnalyzer()
            Constants.NAV_SETTINGS -> showSettings()
            Constants.NAV_ABOUT -> showAbout()
            Constants.NAV_RATE -> rateApp()
            Constants.NAV_SHARE -> shareApp()
            Constants.NAV_FEEDBACK -> sendFeedback()
            else -> {
                if (item.path != null) {
                    navigateToPath(item.path)
                }
            }
        }
    }
    
    override fun onStorageItemClick(storage: StorageInfo) {
        navigateToPath(storage.path)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }
    
    private fun showRecentFiles() {
        // Implement recent files
        showWarningSnackbar(R.string.coming_soon)
    }
    
    private fun showStorageAnalyzer() {
        // Implement storage analyzer
        showWarningSnackbar(R.string.coming_soon)
    }
    
    private fun rateApp() {
        openUrl("market://details?id=$packageName")
    }
    
    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text,
                "https://play.google.com/store/apps/details?id=$packageName"))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_title)))
    }
    
    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@exory550.com"))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject,
                BuildConfig.VERSION_NAME))
        }
        
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback)))
        } catch (e: Exception) {
            showErrorSnackbar(R.string.no_email_app_found)
        }
    }
    
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            showErrorSnackbar(R.string.cannot_open_url)
        }
    }
    
    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_denied)
            .setMessage(R.string.permission_denied_message)
            .setPositiveButton(R.string.settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.exit) { _, _ ->
                finish()
            }
            .show()
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        
        menu.findItem(R.id.action_select_all).isVisible = isSelectionMode
        menu.findItem(R.id.action_clear_selection).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        menu.findItem(R.id.action_cut).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        menu.findItem(R.id.action_copy).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        menu.findItem(R.id.action_delete).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        menu.findItem(R.id.action_rename).isVisible = isSelectionMode && selectedFiles.size == 1
        menu.findItem(R.id.action_properties).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        menu.findItem(R.id.action_share).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        menu.findItem(R.id.action_compress).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        menu.findItem(R.id.action_favorite).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        
        menu.findItem(R.id.action_paste).isVisible = clipboard.isNotEmpty() && !isSelectionMode
        
        menu.findItem(R.id.action_search).isVisible = !isSelectionMode && !isSearchMode
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_all -> {
                selectAll()
                true
            }
            R.id.action_clear_selection -> {
                clearSelection()
                true
            }
            R.id.action_cut -> {
                showCutDialog()
                true
            }
            R.id.action_copy -> {
                showCopyDialog()
                true
            }
            R.id.action_paste -> {
                showPasteOptions()
                true
            }
            R.id.action_delete -> {
                showDeleteDialog()
                true
            }
            R.id.action_rename -> {
                showRenameDialog()
                true
            }
            R.id.action_properties -> {
                showPropertiesDialog()
                true
            }
            R.id.action_share -> {
                showShareDialog()
                true
            }
            R.id.action_compress -> {
                compressSelected()
                true
            }
            R.id.action_favorite -> {
                addToFavorites()
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            R.id.action_view_mode -> {
                showViewModeDialog()
                true
            }
            R.id.action_search -> {
                enterSearchMode()
                true
            }
            R.id.action_new_folder -> {
                showCreateFolderDialog()
                true
            }
            R.id.action_new_file -> {
                showCreateFileDialog()
                true
            }
            R.id.action_settings -> {
                showSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        when {
            isSelectionMode -> exitSelectionMode()
            isSearchMode -> exitSearchMode()
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> 
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            File(currentPath).parentFile != null -> navigateToParent()
            else -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.exit_app)
                    .setMessage(R.string.exit_app_message)
                    .setPositiveButton(R.string.exit) { _, _ ->
                        finishAffinity()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }
    
    companion object {
        const val VIEW_MODE_LIST = 0
        const val VIEW_MODE_GRID = 1
        
        const val SORT_BY_NAME = 0
        const val SORT_BY_SIZE = 1
        const val SORT_BY_DATE = 2
        const val SORT_BY_TYPE = 3
        
        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
}
