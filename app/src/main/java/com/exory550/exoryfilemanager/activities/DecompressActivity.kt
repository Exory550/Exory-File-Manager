package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.DecompressAdapter
import com.exory550.exoryfilemanager.databinding.ActivityDecompressBinding
import com.exory550.exoryfilemanager.extensions.*
import com.exory550.exoryfilemanager.models.CompressedFile
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.tasks.DecompressTask
import com.exory550.exoryfilemanager.utils.CompressionManager
import com.exory550.exoryfilemanager.utils.Constants
import com.exory550.exoryfilemanager.utils.FileUtils
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipFile
import javax.inject.Inject

@AndroidEntryPoint
class DecompressActivity : BaseAbstractActivity() {

    override val layoutRes: Int = R.layout.activity_decompress
    
    private lateinit var binding: ActivityDecompressBinding
    private lateinit var adapter: DecompressAdapter
    
    @Inject
    lateinit var compressionManager: CompressionManager
    
    @Inject
    lateinit var fileUtils: FileUtils
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var compressedFile: CompressedFile? = null
    private var currentPath: String = ""
    private var extractedFiles: MutableList<ExoryFileItem> = mutableListOf()
    private var selectedFiles: MutableSet<ExoryFileItem> = mutableSetOf()
    private var isSelectionMode = false
    private var currentViewMode = VIEW_MODE_LIST
    private var currentSortMode = SORT_BY_NAME
    
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { extractToLocation(it) }
    }
    
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { openExtractedFile(it) }
    }
    
    override fun initializeViews() {
        binding = ActivityDecompressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupSwipeRefresh()
        
        parseIntent()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = ""
        }
        
        binding.toolbar.setNavigationOnClickListener {
            if (isSelectionMode) {
                exitSelectionMode()
            } else if (currentPath.isNotEmpty()) {
                navigateUp()
            } else {
                finishWithAnimation()
            }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = DecompressAdapter(
            onItemClick = { fileItem ->
                if (isSelectionMode) {
                    toggleSelection(fileItem)
                } else {
                    openFile(fileItem)
                }
            },
            onItemLongClick = { fileItem ->
                if (!isSelectionMode) {
                    enterSelectionMode()
                    toggleSelection(fileItem)
                }
                true
            },
            onSelectionChanged = { selectedCount ->
                updateSelectionModeTitle(selectedCount)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DecompressActivity)
            adapter = this@DecompressActivity.adapter
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.extracted_files))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.compressed_info))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showExtractedFiles()
                    1 -> showCompressedInfo()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshContent()
        }
    }
    
    private fun parseIntent() {
        intent?.let {
            when (it.action) {
                Intent.ACTION_VIEW -> {
                    it.data?.let { uri ->
                        loadCompressedFile(uri)
                    }
                }
                else -> {
                    val filePath = it.getStringExtra(Constants.EXTRA_FILE_PATH)
                    filePath?.let { path ->
                        loadCompressedFile(File(path))
                    }
                }
            }
        }
    }
    
    private fun loadCompressedFile(file: File) {
        lifecycleScope.launch {
            showProgress(R.string.loading)
            
            try {
                compressedFile = withContext(Dispatchers.IO) {
                    compressionManager.analyzeCompressedFile(file)
                }
                
                compressedFile?.let {
                    binding.toolbar.title = it.name
                    currentPath = ""
                    loadContents()
                }
            } catch (e: Exception) {
                showErrorSnackbar(R.string.error_loading_compressed_file)
                finish()
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun loadCompressedFile(uri: Uri) {
        lifecycleScope.launch {
            showProgress(R.string.loading)
            
            try {
                compressedFile = withContext(Dispatchers.IO) {
                    compressionManager.analyzeCompressedFile(uri, this@DecompressActivity)
                }
                
                compressedFile?.let {
                    binding.toolbar.title = it.name
                    currentPath = ""
                    loadContents()
                }
            } catch (e: Exception) {
                showErrorSnackbar(R.string.error_loading_compressed_file)
                finish()
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun loadContents() {
        lifecycleScope.launch {
            try {
                val files = withContext(Dispatchers.IO) {
                    compressedFile?.let {
                        compressionManager.listContents(it, currentPath)
                    } ?: emptyList()
                }
                
                extractedFiles.clear()
                extractedFiles.addAll(files)
                
                applySorting()
                updateUI()
                
                binding.emptyView.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
                
            } catch (e: Exception) {
                showErrorSnackbar(R.string.error_loading_contents)
            }
        }
    }
    
    private fun updateUI() {
        adapter.submitList(extractedFiles.toList())
        
        binding.infoLayout.apply {
            fileCountText.text = getString(R.string.files_count, extractedFiles.size)
            
            val totalSize = extractedFiles.sumOf { it.size }
            totalSizeText.text = fileUtils.formatFileSize(totalSize)
            
            val compressedSize = compressedFile?.size ?: 0
            val compressionRatio = if (compressedSize > 0) {
                ((totalSize - compressedSize) * 100 / totalSize.toDouble())
            } else 0.0
            
            compressionRatioText.text = String.format("%.1f%%", compressionRatio)
        }
    }
    
    private fun showExtractedFiles() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.infoContent.visibility = View.GONE
    }
    
    private fun showCompressedInfo() {
        binding.recyclerView.visibility = View.GONE
        binding.infoContent.visibility = View.VISIBLE
        
        compressedFile?.let { file ->
            binding.infoContent.apply {
                fileNameText.text = file.name
                filePathText.text = file.path
                fileSizeText.text = fileUtils.formatFileSize(file.size)
                modifiedDateText.text = SimpleDateFormat(
                    Constants.DATE_FORMAT, Locale.getDefault()
                ).format(Date(file.lastModified))
                
                compressionMethodText.text = file.compressionMethod
                commentText.text = file.comment ?: getString(R.string.no_comment)
                
                val itemCount = file.itemCount
                val folderCount = file.folderCount
                val fileCount = itemCount - folderCount
                
                structureText.text = getString(
                    R.string.compressed_structure,
                    folderCount,
                    fileCount,
                    itemCount
                )
            }
        }
    }
    
    private fun openFile(fileItem: ExoryExoryFileItem) {
        when {
            fileItem.isDirectory -> {
                currentPath = fileItem.path
                loadContents()
            }
            fileItem.isCompressed -> {
                openCompressedFile(fileItem)
            }
            else -> {
                previewFile(fileItem)
            }
        }
    }
    
    private fun openCompressedFile(fileItem: ExoryExoryFileItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.open_compressed_file)
            .setMessage(R.string.open_compressed_file_message)
            .setPositiveButton(R.string.open) { _, _ ->
                val intent = Intent(this, DecompressActivity::class.java)
                intent.putExtra(Constants.EXTRA_FILE_PATH, fileItem.path)
                startActivityWithAnimation(intent)
            }
            .setNegativeButton(R.string.extract) { _, _ ->
                extractSingleFile(fileItem)
            }
            .show()
    }
    
    private fun previewFile(fileItem: ExoryExoryFileItem) {
        val intent = Intent(this, FileViewerActivity::class.java).apply {
            putExtra(Constants.EXTRA_FILE_PATH, fileItem.path)
            putExtra(Constants.EXTRA_FILE_NAME, fileItem.name)
            putExtra(Constants.EXTRA_FILE_SIZE, fileItem.size)
            putExtra(Constants.EXTRA_MIME_TYPE, fileItem.mimeType)
        }
        startActivityWithAnimation(intent)
    }
    
    private fun extractAll() {
        val defaultName = compressedFile?.name?.substringBeforeLast(".") ?: "extracted"
        createDocumentLauncher.launch("$defaultName.zip")
    }
    
    private fun extractToLocation(uri: Uri) {
        lifecycleScope.launch {
            showProgress(R.string.extracting)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    compressionManager.extractAll(compressedFile!!, uri, this@DecompressActivity)
                }
                
                if (success) {
                    showSuccessSnackbar(R.string.extract_success)
                    Snackbar.make(binding.rootLayout, R.string.open_extracted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open) {
                            openExtractedLocation(uri)
                        }.show()
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
    
    private fun extractSelected() {
        if (selectedFiles.isEmpty()) {
            showWarningSnackbar(R.string.no_files_selected)
            return
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.extract_selected)
            .setMessage(getString(R.string.extract_selected_message, selectedFiles.size))
            .setPositiveButton(R.string.extract) { _, _ ->
                performExtractSelected()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun performExtractSelected() {
        lifecycleScope.launch {
            showProgress(R.string.extracting)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    compressionManager.extractFiles(
                        compressedFile!!,
                        selectedFiles.toList(),
                        this@DecompressActivity
                    )
                }
                
                if (success) {
                    showSuccessSnackbar(R.string.extract_success)
                    exitSelectionMode()
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
    
    private fun extractSingleFile(fileItem: ExoryExoryFileItem) {
        lifecycleScope.launch {
            showProgress(R.string.extracting)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    compressionManager.extractFile(
                        compressedFile!!,
                        fileItem,
                        this@DecompressActivity
                    )
                }
                
                if (success) {
                    showSuccessSnackbar(R.string.extract_success)
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
    
    private fun openExtractedLocation(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "resource/folder")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showErrorSnackbar(R.string.cannot_open_location)
        }
    }
    
    private fun openExtractedFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setData(uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showErrorSnackbar(R.string.cannot_open_file)
        }
    }
    
    private fun navigateUp() {
        val parentPath = File(currentPath).parent
        currentPath = parentPath ?: ""
        loadContents()
    }
    
    private fun refreshContent() {
        loadContents()
        binding.swipeRefreshLayout.isRefreshing = false
    }
    
    private fun enterSelectionMode() {
        isSelectionMode = true
        selectedFiles.clear()
        adapter.setSelectionMode(true)
        supportActionBar?.setDisplayHomeAsUpIndicator(R.drawable.ic_close)
        invalidateOptionsMenu()
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedFiles.clear()
        adapter.setSelectionMode(false)
        supportActionBar?.setDisplayHomeAsUpIndicator(R.drawable.ic_arrow_back)
        invalidateOptionsMenu()
        binding.toolbar.title = compressedFile?.name ?: ""
    }
    
    private fun toggleSelection(fileItem: ExoryExoryFileItem) {
        if (selectedFiles.contains(fileItem)) {
            selectedFiles.remove(fileItem)
        } else {
            selectedFiles.add(fileItem)
        }
        adapter.notifyItemChanged(extractedFiles.indexOf(fileItem))
        updateSelectionModeTitle(selectedFiles.size)
    }
    
    private fun selectAll() {
        selectedFiles.clear()
        selectedFiles.addAll(extractedFiles)
        adapter.notifyDataSetChanged()
        updateSelectionModeTitle(selectedFiles.size)
    }
    
    private fun clearSelection() {
        selectedFiles.clear()
        adapter.notifyDataSetChanged()
        updateSelectionModeTitle(0)
    }
    
    private fun updateSelectionModeTitle(count: Int) {
        if (isSelectionMode) {
            binding.toolbar.title = getString(R.string.selected_count, count)
        }
    }
    
    private fun applySorting() {
        extractedFiles.sortWith(when (currentSortMode) {
            SORT_BY_NAME -> compareBy { it.name.lowercase() }
            SORT_BY_SIZE -> compareByDescending { it.size }
            SORT_BY_DATE -> compareByDescending { it.lastModified }
            SORT_BY_TYPE -> compareBy { it.extension }
            else -> compareBy { it.name.lowercase() }
        })
        
        // Folders first
        extractedFiles.sortWith(compareByDescending<ExoryFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
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
            .setItems(sortOptions) { _, which ->
                currentSortMode = which
                applySorting()
                adapter.notifyDataSetChanged()
            }
            .show()
    }
    
    private fun showViewModeDialog() {
        val viewModes = arrayOf(
            getString(R.string.list_view),
            getString(R.string.grid_view)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.view_mode)
            .setItems(viewModes) { _, which ->
                currentViewMode = which
                // Update layout manager based on view mode
            }
            .show()
    }
    
    private fun showInfoDialog() {
        compressedFile?.let { file ->
            val info = buildString {
                appendLine(getString(R.string.file_name, file.name))
                appendLine(getString(R.string.file_path, file.path))
                appendLine(getString(R.string.file_size, fileUtils.formatFileSize(file.size)))
                appendLine(getString(R.string.modified, SimpleDateFormat(
                    Constants.DATE_FORMAT, Locale.getDefault()
                ).format(Date(file.lastModified))))
                appendLine(getString(R.string.compression_method, file.compressionMethod))
                file.comment?.let { appendLine(getString(R.string.comment, it)) }
                appendLine(getString(R.string.items_count, file.itemCount))
                appendLine(getString(R.string.folders_count, file.folderCount))
            }
            
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.file_info)
                .setMessage(info)
                .setPositiveButton(R.string.ok, null)
                .show()
        }
    }
    
    private fun searchFiles() {
        // Implement search functionality
        showWarningSnackbar(R.string.coming_soon)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_decompress, menu)
        
        menu.findItem(R.id.action_select_all).isVisible = isSelectionMode
        menu.findItem(R.id.action_clear_selection).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        menu.findItem(R.id.action_extract_selected).isVisible = isSelectionMode && selectedFiles.isNotEmpty()
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_extract_all -> {
                extractAll()
                true
            }
            R.id.action_extract_selected -> {
                extractSelected()
                true
            }
            R.id.action_select_all -> {
                selectAll()
                true
            }
            R.id.action_clear_selection -> {
                clearSelection()
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
            R.id.action_info -> {
                showInfoDialog()
                true
            }
            R.id.action_search -> {
                searchFiles()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        when {
            isSelectionMode -> exitSelectionMode()
            currentPath.isNotEmpty() -> navigateUp()
            else -> super.onBackPressed()
        }
    }
    
    companion object {
        const val VIEW_MODE_LIST = 0
        const val VIEW_MODE_GRID = 1
        
        const val SORT_BY_NAME = 0
        const val SORT_BY_SIZE = 1
        const val SORT_BY_DATE = 2
        const val SORT_BY_TYPE = 3
        
        fun getIntent(context: Context, filePath: String): Intent {
            return Intent(context, DecompressActivity::class.java).apply {
                putExtra(Constants.EXTRA_FILE_PATH, filePath)
            }
        }
        
        fun getIntent(context: Context, uri: Uri): Intent {
            return Intent(context, DecompressActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = uri
            }
        }
    }
}
