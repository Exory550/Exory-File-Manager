package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.StorageAnalysisAdapter
import com.exory550.exoryfilemanager.adapters.StorageCleanupAdapter
import com.exory550.exoryfilemanager.databinding.ActivityManageStorageBinding
import com.exory550.exoryfilemanager.extensions.formatFileSize
import com.exory550.exoryfilemanager.extensions.showToast
import com.exory550.exoryfilemanager.models.CleanupSuggestion
import com.exory550.exoryfilemanager.models.StorageAnalysis
import com.exory550.exoryfilemanager.models.StorageCategory
import com.exory550.exoryfilemanager.tasks.StorageAnalysisTask
import com.exory550.exoryfilemanager.tasks.StorageCleanupTask
import com.exory550.exoryfilemanager.utils.Constants
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ManageStorageActivity : BaseAbstractActivity() {

    override val layoutRes: Int = R.layout.activity_manage_storage
    
    private lateinit var binding: ActivityManageStorageBinding
    private lateinit var analysisAdapter: StorageAnalysisAdapter
    private lateinit var cleanupAdapter: StorageCleanupAdapter
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var currentAnalysis: StorageAnalysis? = null
    private var cleanupSuggestions: List<CleanupSuggestion> = emptyList()
    private var selectedSuggestions: MutableSet<CleanupSuggestion> = mutableSetOf()
    private var isSelectionMode = false
    private var currentPath: String = Environment.getExternalStorageDirectory().absolutePath
    
    override fun initializeViews() {
        binding = ActivityManageStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupSwipeRefresh()
        
        analyzeStorage()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.manage_storage)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            if (isSelectionMode) {
                exitSelectionMode()
            } else {
                finishWithAnimation()
            }
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.storage_analysis))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.cleanup_suggestions))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.duplicate_files))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showAnalysisTab()
                    1 -> showCleanupTab()
                    2 -> showDuplicatesTab()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupRecyclerView() {
        analysisAdapter = StorageAnalysisAdapter(
            onCategoryClick = { category ->
                showCategoryDetails(category)
            }
        )
        
        cleanupAdapter = StorageCleanupAdapter(
            onItemClick = { suggestion ->
                if (isSelectionMode) {
                    toggleSelection(suggestion)
                } else {
                    showSuggestionDetails(suggestion)
                }
            },
            onItemLongClick = { suggestion ->
                if (!isSelectionMode) {
                    enterSelectionMode()
                    toggleSelection(suggestion)
                }
                true
            },
            onSelectionChanged = { selectedCount ->
                updateSelectionModeTitle(selectedCount)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ManageStorageActivity)
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            analyzeStorage()
        }
    }
    
    private fun analyzeStorage() {
        lifecycleScope.launch {
            showProgress(R.string.analyzing_storage)
            binding.swipeRefreshLayout.isRefreshing = true
            
            try {
                val analysis = withContext(Dispatchers.IO) {
                    StorageAnalysisTask.analyze(currentPath)
                }
                
                currentAnalysis = analysis
                updateAnalysisUI(analysis)
                
                // Generate cleanup suggestions
                cleanupSuggestions = withContext(Dispatchers.IO) {
                    generateCleanupSuggestions(analysis)
                }
                
            } catch (e: Exception) {
                showErrorSnackbar(R.string.analysis_failed)
            } finally {
                dismissProgress()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun updateAnalysisUI(analysis: StorageAnalysis) {
        binding.apply {
            // Update storage overview
            totalStorageText.text = formatFileSize(analysis.totalSpace)
            usedStorageText.text = formatFileSize(analysis.usedSpace)
            freeStorageText.text = formatFileSize(analysis.freeSpace)
            
            val usedPercentage = (analysis.usedSpace * 100 / analysis.totalSpace).toInt()
            storageProgressBar.progress = usedPercentage
            usagePercentageText.text = "$usedPercentage%"
            
            // Update category breakdown
            analysisAdapter.submitList(analysis.categories)
        }
    }
    
    private fun generateCleanupSuggestions(analysis: StorageAnalysis): List<CleanupSuggestion> {
        val suggestions = mutableListOf<CleanupSuggestion>()
        
        // Large files
        analysis.largeFiles.take(20).forEach { file ->
            suggestions.add(
                CleanupSuggestion(
                    id = file.path.hashCode().toLong(),
                    name = file.name,
                    path = file.path,
                    size = file.size,
                    type = CleanupSuggestion.TYPE_LARGE_FILE,
                    reason = R.string.suggestion_large_file,
                    canDelete = true,
                    lastModified = file.lastModified
                )
            )
        }
        
        // Old files (> 30 days)
        val cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        analysis.oldFiles.take(20).forEach { file ->
            suggestions.add(
                CleanupSuggestion(
                    id = file.path.hashCode().toLong(),
                    name = file.name,
                    path = file.path,
                    size = file.size,
                    type = CleanupSuggestion.TYPE_OLD_FILE,
                    reason = R.string.suggestion_old_file,
                    canDelete = true,
                    lastModified = file.lastModified
                )
            )
        }
        
        // Empty folders
        analysis.emptyFolders.take(20).forEach { folder ->
            suggestions.add(
                CleanupSuggestion(
                    id = folder.path.hashCode().toLong(),
                    name = folder.name,
                    path = folder.path,
                    size = 0,
                    type = CleanupSuggestion.TYPE_EMPTY_FOLDER,
                    reason = R.string.suggestion_empty_folder,
                    canDelete = true,
                    isDirectory = true
                )
            )
        }
        
        // Temporary files
        analysis.tempFiles.take(20).forEach { file ->
            suggestions.add(
                CleanupSuggestion(
                    id = file.path.hashCode().toLong(),
                    name = file.name,
                    path = file.path,
                    size = file.size,
                    type = CleanupSuggestion.TYPE_TEMP_FILE,
                    reason = R.string.suggestion_temp_file,
                    canDelete = true,
                    lastModified = file.lastModified
                )
            )
        }
        
        // Cache files
        analysis.cacheFiles.take(20).forEach { file ->
            suggestions.add(
                CleanupSuggestion(
                    id = file.path.hashCode().toLong(),
                    name = file.name,
                    path = file.path,
                    size = file.size,
                    type = CleanupSuggestion.TYPE_CACHE_FILE,
                    reason = R.string.suggestion_cache_file,
                    canDelete = true,
                    lastModified = file.lastModified
                )
            )
        }
        
        // APK files (after installation)
        analysis.apkFiles.take(20).forEach { file ->
            suggestions.add(
                CleanupSuggestion(
                    id = file.path.hashCode().toLong(),
                    name = file.name,
                    path = file.path,
                    size = file.size,
                    type = CleanupSuggestion.TYPE_APK_FILE,
                    reason = R.string.suggestion_apk_file,
                    canDelete = true,
                    lastModified = file.lastModified
                )
            )
        }
        
        // Log files
        analysis.logFiles.take(20).forEach { file ->
            suggestions.add(
                CleanupSuggestion(
                    id = file.path.hashCode().toLong(),
                    name = file.name,
                    path = file.path,
                    size = file.size,
                    type = CleanupSuggestion.TYPE_LOG_FILE,
                    reason = R.string.suggestion_log_file,
                    canDelete = true,
                    lastModified = file.lastModified
                )
            )
        }
        
        return suggestions.sortedByDescending { it.size }
    }
    
    private fun showAnalysisTab() {
        binding.recyclerView.adapter = analysisAdapter
        binding.cleanupActionsLayout.visibility = View.GONE
        binding.fabCleanup.hide()
        
        currentAnalysis?.let {
            updateAnalysisUI(it)
        }
    }
    
    private fun showCleanupTab() {
        binding.recyclerView.adapter = cleanupAdapter
        cleanupAdapter.submitList(cleanupSuggestions)
        binding.cleanupActionsLayout.visibility = View.VISIBLE
        binding.fabCleanup.show()
        
        updateCleanupSummary()
    }
    
    private fun showDuplicatesTab() {
        binding.recyclerView.adapter = null
        binding.cleanupActionsLayout.visibility = View.GONE
        binding.fabCleanup.hide()
        
        // Show duplicate files finder
        findDuplicateFiles()
    }
    
    private fun updateCleanupSummary() {
        val totalSize = cleanupSuggestions.sumOf { it.size }
        val itemCount = cleanupSuggestions.size
        
        binding.cleanupSummaryText.text = getString(
            R.string.cleanup_summary,
            itemCount,
            formatFileSize(totalSize)
        )
        
        binding.btnCleanupAll.setOnClickListener {
            showCleanupAllConfirmation()
        }
        
        binding.fabCleanup.setOnClickListener {
            if (isSelectionMode) {
                cleanupSelected()
            } else {
                startQuickCleanup()
            }
        }
    }
    
    private fun showCategoryDetails(category: StorageCategory) {
        val intent = Intent(this, CategoryDetailsActivity::class.java).apply {
            putExtra(Constants.EXTRA_CATEGORY_NAME, category.name)
            putExtra(Constants.EXTRA_CATEGORY_PATH, category.path)
            putExtra(Constants.EXTRA_CATEGORY_SIZE, category.size)
            putExtra(Constants.EXTRA_CATEGORY_COUNT, category.fileCount)
        }
        startActivityWithAnimation(intent)
    }
    
    private fun showSuggestionDetails(suggestion: CleanupSuggestion) {
        MaterialAlertDialogBuilder(this)
            .setTitle(suggestion.name)
            .setMessage(
                buildString {
                    appendLine(getString(R.string.path, suggestion.path))
                    appendLine(getString(R.string.size, formatFileSize(suggestion.size)))
                    appendLine(getString(R.string.reason, getString(suggestion.reason)))
                    if (suggestion.lastModified > 0) {
                        appendLine(getString(R.string.last_modified, 
                            java.text.SimpleDateFormat(
                                Constants.DATE_FORMAT, 
                                java.util.Locale.getDefault()
                            ).format(java.util.Date(suggestion.lastModified))
                        ))
                    }
                }
            )
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteSuggestion(suggestion)
            }
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.open) { _, _ ->
                openFileLocation(suggestion)
            }
            .show()
    }
    
    private fun openFileLocation(suggestion: CleanupSuggestion) {
        val file = File(suggestion.path)
        val parentPath = if (suggestion.isDirectory) file.parent else file.parent
        
        parentPath?.let {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(Constants.EXTRA_OPEN_PATH, it)
            }
            startActivityWithAnimation(intent)
        }
    }
    
    private fun deleteSuggestion(suggestion: CleanupSuggestion) {
        lifecycleScope.launch {
            showProgress(R.string.deleting)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    val file = File(suggestion.path)
                    if (suggestion.isDirectory) {
                        file.deleteRecursively()
                    } else {
                        file.delete()
                    }
                }
                
                if (success) {
                    cleanupSuggestions = cleanupSuggestions.filter { it.id != suggestion.id }
                    cleanupAdapter.submitList(cleanupSuggestions)
                    updateCleanupSummary()
                    showSuccessSnackbar(R.string.delete_success)
                } else {
                    showErrorSnackbar(R.string.delete_error)
                }
            } catch (e: Exception) {
                showErrorSnackbar(R.string.delete_error)
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun findDuplicateFiles() {
        lifecycleScope.launch {
            showProgress(R.string.searching_duplicates)
            
            try {
                val duplicates = withContext(Dispatchers.IO) {
                    findDuplicates(currentPath)
                }
                
                if (duplicates.isEmpty()) {
                    showEmptyDuplicates()
                } else {
                    showDuplicatesList(duplicates)
                }
            } catch (e: Exception) {
                showErrorSnackbar(R.string.duplicate_search_failed)
            } finally {
                dismissProgress()
            }
        }
    }
    
    private fun findDuplicates(path: String): Map<String, List<File>> {
        val filesBySize = mutableMapOf<Long, MutableList<File>>()
        val duplicates = mutableMapOf<String, MutableList<File>>()
        
        File(path).walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                filesBySize.getOrPut(file.length()) { mutableListOf() }.add(file)
            }
        
        filesBySize.filter { it.value.size > 1 }
            .forEach { (_, files) ->
                val filesByHash = mutableMapOf<String, MutableList<File>>()
                files.forEach { file ->
                    val hash = fileUtils.calculateMD5(file)
                    filesByHash.getOrPut(hash) { mutableListOf() }.add(file)
                }
                
                filesByHash.filter { it.value.size > 1 }
                    .forEach { (hash, duplicateFiles) ->
                        duplicates[hash] = duplicateFiles
                    }
            }
        
        return duplicates
    }
    
    private fun showEmptyDuplicates() {
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.root.visibility = View.VISIBLE
        binding.emptyView.emptyText.text = getString(R.string.no_duplicates_found)
    }
    
    private fun showDuplicatesList(duplicates: Map<String, List<File>>) {
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.root.visibility = View.GONE
        
        // Show duplicates in adapter
        val duplicateList = duplicates.flatMap { (_, files) ->
            files.map { file ->
                CleanupSuggestion(
                    id = file.path.hashCode().toLong(),
                    name = file.name,
                    path = file.path,
                    size = file.length(),
                    type = CleanupSuggestion.TYPE_DUPLICATE,
                    reason = R.string.suggestion_duplicate_file,
                    canDelete = true,
                    lastModified = file.lastModified()
                )
            }
        }
        
        // Implement duplicate adapter
    }
    
    private fun startQuickCleanup() {
        val deletableSuggestions = cleanupSuggestions.filter { it.canDelete }
        
        if (deletableSuggestions.isEmpty()) {
            showWarningSnackbar(R.string.nothing_to_clean)
            return
        }
        
        val totalSize = deletableSuggestions.sumOf { it.size }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.quick_cleanup)
            .setMessage(getString(R.string.quick_cleanup_message, 
                deletableSuggestions.size, 
                formatFileSize(totalSize)))
            .setPositiveButton(R.string.clean) { _, _ ->
                performCleanup(deletableSuggestions)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showCleanupAllConfirmation() {
        if (selectedSuggestions.isNotEmpty() && isSelectionMode) {
            cleanupSelected()
            return
        }
        
        val totalSize = cleanupSuggestions.sumOf { it.size }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cleanup_all)
            .setMessage(getString(R.string.cleanup_all_message, 
                cleanupSuggestions.size, 
                formatFileSize(totalSize)))
            .setPositiveButton(R.string.clean) { _, _ ->
                performCleanup(cleanupSuggestions)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun cleanupSelected() {
        if (selectedSuggestions.isEmpty()) return
        
        val totalSize = selectedSuggestions.sumOf { it.size }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.cleanup_selected)
            .setMessage(getString(R.string.cleanup_selected_message,
                selectedSuggestions.size,
                formatFileSize(totalSize)))
            .setPositiveButton(R.string.clean) { _, _ ->
                performCleanup(selectedSuggestions.toList())
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                exitSelectionMode()
            }
            .show()
    }
    
    private fun performCleanup(suggestions: List<CleanupSuggestion>) {
        lifecycleScope.launch {
            showProgress(R.string.cleaning)
            
            try {
                val task = StorageCleanupTask(this@ManageStorageActivity)
                val results = withContext(Dispatchers.IO) {
                    task.cleanup(suggestions)
                }
                
                val successCount = results.count { it }
                val failedCount = results.size - successCount
                
                dismissProgress()
                
                if (failedCount == 0) {
                    showSuccessSnackbar(R.string.cleanup_complete)
                } else {
                    showWarningSnackbar(getString(R.string.cleanup_partial, successCount, failedCount))
                }
                
                // Refresh analysis
                analyzeStorage()
                
                if (isSelectionMode) {
                    exitSelectionMode()
                }
                
            } catch (e: Exception) {
                dismissProgress()
                showErrorSnackbar(R.string.cleanup_failed)
            }
        }
    }
    
    private fun enterSelectionMode() {
        isSelectionMode = true
        selectedSuggestions.clear()
        cleanupAdapter.setSelectionMode(true)
        supportActionBar?.setDisplayHomeAsUpIndicator(R.drawable.ic_close)
        binding.fabCleanup.setImageResource(R.drawable.ic_delete)
        invalidateOptionsMenu()
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedSuggestions.clear()
        cleanupAdapter.setSelectionMode(false)
        supportActionBar?.setDisplayHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.title = getString(R.string.manage_storage)
        binding.fabCleanup.setImageResource(R.drawable.ic_clean)
        invalidateOptionsMenu()
    }
    
    private fun toggleSelection(suggestion: CleanupSuggestion) {
        if (selectedSuggestions.contains(suggestion)) {
            selectedSuggestions.remove(suggestion)
        } else {
            selectedSuggestions.add(suggestion)
        }
        cleanupAdapter.notifyItemChanged(cleanupSuggestions.indexOf(suggestion))
        updateSelectionModeTitle(selectedSuggestions.size)
    }
    
    private fun selectAll() {
        selectedSuggestions.clear()
        selectedSuggestions.addAll(cleanupSuggestions.filter { it.canDelete })
        cleanupAdapter.notifyDataSetChanged()
        updateSelectionModeTitle(selectedSuggestions.size)
    }
    
    private fun clearSelection() {
        selectedSuggestions.clear()
        cleanupAdapter.notifyDataSetChanged()
        updateSelectionModeTitle(0)
    }
    
    private fun updateSelectionModeTitle(count: Int) {
        if (isSelectionMode) {
            supportActionBar?.title = getString(R.string.selected_count, count)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manage_storage, menu)
        
        menu.findItem(R.id.action_select_all).isVisible = isSelectionMode
        menu.findItem(R.id.action_clear_selection).isVisible = isSelectionMode && selectedSuggestions.isNotEmpty()
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                analyzeStorage()
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
            R.id.action_settings -> {
                showCleanupSettings()
                true
            }
            R.id.action_help -> {
                showHelp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showCleanupSettings() {
        // Implement cleanup settings dialog
        showWarningSnackbar(R.string.coming_soon)
    }
    
    private fun showHelp() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.help)
            .setMessage(R.string.manage_storage_help)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    override fun onBackPressed() {
        when {
            isSelectionMode -> exitSelectionMode()
            else -> super.onBackPressed()
        }
    }
    
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ManageStorageActivity::class.java)
        }
        
        fun getIntent(context: Context, path: String): Intent {
            return Intent(context, ManageStorageActivity::class.java).apply {
                putExtra(Constants.EXTRA_PATH, path)
            }
        }
    }
}
