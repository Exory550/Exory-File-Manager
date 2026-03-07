package com.exory550.exoryfilemanager.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.FavoritesAdapter
import com.exory550.exoryfilemanager.databinding.ActivityFavoritesBinding
import com.exory550.exoryfilemanager.dialogs.AddFavoriteDialog
import com.exory550.exoryfilemanager.extensions.showToast
import com.exory550.exoryfilemanager.models.FavoriteItem
import com.exory550.exoryfilemanager.utils.Constants
import com.exory550.exoryfilemanager.utils.FavoritesManager
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FavoritesActivity : BaseAbstractActivity() {

    override val layoutRes: Int = R.layout.activity_favorites
    
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: FavoritesAdapter
    
    @Inject
    lateinit var favoritesManager: FavoritesManager
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var favorites: List<FavoriteItem> = emptyList()
    private var isSelectionMode = false
    private var selectedFavorites: MutableSet<FavoriteItem> = mutableSetOf()
    
    private val itemTouchHelper by lazy {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                
                if (fromPosition != toPosition) {
                    lifecycleScope.launch {
                        favoritesManager.reorderFavorites(fromPosition, toPosition)
                    }
                }
                return true
            }
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val favorite = favorites.getOrNull(position)
                
                favorite?.let {
                    showDeleteConfirmation(it)
                }
            }
            
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                adapter.notifyDataSetChanged()
            }
        }
        ItemTouchHelper(callback)
    }
    
    override fun initializeViews() {
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeFavorites()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.favorites)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            if (isSelectionMode) {
                exitSelectionMode()
            } else {
                finishWithAnimation()
            }
        }
        
        binding.fabAddFavorite.setOnClickListener {
            showAddFavoriteDialog()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(
            onItemClick = { favorite ->
                if (isSelectionMode) {
                    toggleSelection(favorite)
                } else {
                    openFavorite(favorite)
                }
            },
            onItemLongClick = { favorite ->
                if (!isSelectionMode) {
                    enterSelectionMode()
                    toggleSelection(favorite)
                }
                true
            },
            onSelectionChanged = { selectedCount ->
                updateSelectionModeTitle(selectedCount)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = this@FavoritesActivity.adapter
        }
        
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshFavorites()
        }
    }
    
    private fun observeFavorites() {
        lifecycleScope.launch {
            favoritesManager.getAllFavorites().collectLatest { favoriteList ->
                favorites = favoriteList
                updateUI()
            }
        }
    }
    
    private fun updateUI() {
        adapter.submitList(favorites)
        
        binding.apply {
            if (favorites.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.root.visibility = View.VISIBLE
                fabAddFavorite.show()
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.root.visibility = View.GONE
                
                // Hide FAB when scrolling
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 0) {
                            fabAddFavorite.hide()
                        } else if (dy < 0) {
                            fabAddFavorite.show()
                        }
                    }
                })
            }
        }
        
        binding.swipeRefreshLayout.isRefreshing = false
    }
    
    private fun refreshFavorites() {
        favoritesManager.refreshFavorites()
    }
    
    private fun showAddFavoriteDialog() {
        AddFavoriteDialog(this,
            onFavoriteAdded = { name, path, type ->
                addFavorite(name, path, type)
            }
        ).show()
    }
    
    private fun addFavorite(name: String, path: String, type: String) {
        lifecycleScope.launch {
            val favorite = FavoriteItem(
                name = name,
                path = path,
                type = type,
                dateAdded = System.currentTimeMillis()
            )
            
            val result = favoritesManager.addFavorite(favorite)
            
            if (result.isSuccess) {
                showSuccessSnackbar(R.string.favorite_added)
            } else {
                showErrorSnackbar(R.string.favorite_add_error)
            }
        }
    }
    
    private fun openFavorite(favorite: FavoriteItem) {
        when (favorite.type) {
            Constants.FILE_TYPE_FOLDER -> {
                openFolder(favorite)
            }
            Constants.FILE_TYPE_FILE -> {
                openFile(favorite)
            }
            Constants.FILE_TYPE_COMPRESSED -> {
                openCompressed(favorite)
            }
            else -> {
                openWithDefaultApp(favorite)
            }
        }
    }
    
    private fun openFolder(favorite: FavoriteItem) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(Constants.EXTRA_OPEN_PATH, favorite.path)
            putExtra(Constants.EXTRA_FAVORITE_NAME, favorite.name)
        }
        startActivityWithAnimation(intent)
    }
    
    private fun openFile(favorite: FavoriteItem) {
        val intent = Intent(this, FileViewerActivity::class.java).apply {
            putExtra(Constants.EXTRA_FILE_PATH, favorite.path)
            putExtra(Constants.EXTRA_FILE_NAME, favorite.name)
        }
        startActivityWithAnimation(intent)
    }
    
    private fun openCompressed(favorite: FavoriteItem) {
        val intent = Intent(this, DecompressActivity::class.java).apply {
            putExtra(Constants.EXTRA_FILE_PATH, favorite.path)
        }
        startActivityWithAnimation(intent)
    }
    
    private fun openWithDefaultApp(favorite: FavoriteItem) {
        try {
            val file = java.io.File(favorite.path)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, favorite.mimeType ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(intent)
        } catch (e: Exception) {
            showErrorSnackbar(R.string.cannot_open_file)
        }
    }
    
    private fun showDeleteConfirmation(favorite: FavoriteItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.remove_favorite)
            .setMessage(getString(R.string.remove_favorite_message, favorite.name))
            .setPositiveButton(R.string.remove) { _, _ ->
                removeFavorite(favorite)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                adapter.notifyDataSetChanged()
            }
            .show()
    }
    
    private fun removeFavorite(favorite: FavoriteItem) {
        lifecycleScope.launch {
            val result = favoritesManager.removeFavorite(favorite)
            
            if (result.isSuccess) {
                Snackbar.make(binding.rootLayout, R.string.favorite_removed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) {
                        undoRemoveFavorite(favorite)
                    }.show()
            } else {
                showErrorSnackbar(R.string.favorite_remove_error)
            }
        }
    }
    
    private fun undoRemoveFavorite(favorite: FavoriteItem) {
        lifecycleScope.launch {
            favoritesManager.addFavorite(favorite)
        }
    }
    
    private fun removeSelected() {
        if (selectedFavorites.isEmpty()) return
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.remove_selected)
            .setMessage(getString(R.string.remove_selected_message, selectedFavorites.size))
            .setPositiveButton(R.string.remove) { _, _ ->
                performRemoveSelected()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                exitSelectionMode()
            }
            .show()
    }
    
    private fun performRemoveSelected() {
        lifecycleScope.launch {
            showProgress(R.string.removing)
            
            val results = selectedFavorites.map { favorite ->
                favoritesManager.removeFavorite(favorite)
            }
            
            dismissProgress()
            
            val successCount = results.count { it.isSuccess }
            
            if (successCount == selectedFavorites.size) {
                showSuccessSnackbar(R.string.favorites_removed)
            } else {
                showErrorSnackbar(R.string.favorites_remove_error)
            }
            
            exitSelectionMode()
        }
    }
    
    private fun enterSelectionMode() {
        isSelectionMode = true
        selectedFavorites.clear()
        adapter.setSelectionMode(true)
        supportActionBar?.setDisplayHomeAsUpIndicator(R.drawable.ic_close)
        itemTouchHelper.attachToRecyclerView(null) // Disable drag & drop in selection mode
        invalidateOptionsMenu()
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedFavorites.clear()
        adapter.setSelectionMode(false)
        supportActionBar?.setDisplayHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.title = getString(R.string.favorites)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView) // Re-enable drag & drop
        invalidateOptionsMenu()
    }
    
    private fun toggleSelection(favorite: FavoriteItem) {
        if (selectedFavorites.contains(favorite)) {
            selectedFavorites.remove(favorite)
        } else {
            selectedFavorites.add(favorite)
        }
        adapter.notifyItemChanged(favorites.indexOf(favorite))
        updateSelectionModeTitle(selectedFavorites.size)
    }
    
    private fun selectAll() {
        selectedFavorites.clear()
        selectedFavorites.addAll(favorites)
        adapter.notifyDataSetChanged()
        updateSelectionModeTitle(selectedFavorites.size)
    }
    
    private fun clearSelection() {
        selectedFavorites.clear()
        adapter.notifyDataSetChanged()
        updateSelectionModeTitle(0)
    }
    
    private fun updateSelectionModeTitle(count: Int) {
        if (isSelectionMode) {
            supportActionBar?.title = getString(R.string.selected_count, count)
        }
    }
    
    private fun editFavorite(favorite: FavoriteItem) {
        AddFavoriteDialog(this,
            favorite = favorite,
            onFavoriteUpdated = { name, path, type ->
                updateFavorite(favorite, name, path, type)
            }
        ).show()
    }
    
    private fun updateFavorite(oldFavorite: FavoriteItem, newName: String, newPath: String, newType: String) {
        lifecycleScope.launch {
            val updatedFavorite = oldFavorite.copy(
                name = newName,
                path = newPath,
                type = newType
            )
            
            val result = favoritesManager.updateFavorite(updatedFavorite)
            
            if (result.isSuccess) {
                showSuccessSnackbar(R.string.favorite_updated)
                if (isSelectionMode) {
                    exitSelectionMode()
                }
            } else {
                showErrorSnackbar(R.string.favorite_update_error)
            }
        }
    }
    
    private fun showFavoriteInfo(favorite: FavoriteItem) {
        val info = buildString {
            appendLine(getString(R.string.name, favorite.name))
            appendLine(getString(R.string.path, favorite.path))
            appendLine(getString(R.string.type, getFavoriteTypeString(favorite.type)))
            appendLine(getString(R.string.date_added, favorite.getFormattedDate()))
            favorite.tags?.let {
                if (it.isNotEmpty()) {
                    appendLine(getString(R.string.tags, it.joinToString(", ")))
                }
            }
            favorite.description?.let {
                appendLine(getString(R.string.description, it))
            }
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.favorite_info)
            .setMessage(info)
            .setPositiveButton(R.string.ok, null)
            .setNeutralButton(R.string.edit) { _, _ ->
                editFavorite(favorite)
            }
            .show()
    }
    
    private fun getFavoriteTypeString(type: String): String {
        return when (type) {
            Constants.FILE_TYPE_FOLDER -> getString(R.string.folder)
            Constants.FILE_TYPE_FILE -> getString(R.string.file)
            Constants.FILE_TYPE_COMPRESSED -> getString(R.string.compressed)
            else -> getString(R.string.other)
        }
    }
    
    private fun shareFavorites() {
        val favoritesText = favorites.joinToString("\n") { favorite ->
            "${favorite.name}: ${favorite.path}"
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, favoritesText)
            type = "text/plain"
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_favorites)))
    }
    
    private fun exportFavorites() {
        lifecycleScope.launch {
            showProgress(R.string.exporting)
            
            val result = favoritesManager.exportFavorites(this@FavoritesActivity)
            
            dismissProgress()
            
            if (result.isSuccess) {
                showSuccessSnackbar(R.string.favorites_exported)
                result.getOrNull()?.let { uri ->
                    showExportedFileLocation(uri)
                }
            } else {
                showErrorSnackbar(R.string.favorites_export_error)
            }
        }
    }
    
    private fun showExportedFileLocation(uri: android.net.Uri) {
        Snackbar.make(binding.rootLayout, R.string.favorites_exported, Snackbar.LENGTH_LONG)
            .setAction(R.string.open) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/json")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    showErrorSnackbar(R.string.cannot_open_file)
                }
            }.show()
    }
    
    private fun importFavorites() {
        // Implement file picker for JSON import
        showWarningSnackbar(R.string.coming_soon)
    }
    
    private fun searchFavorites() {
        // Implement search functionality
        showWarningSnackbar(R.string.coming_soon)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_favorites, menu)
        
        menu.findItem(R.id.action_select_all).isVisible = isSelectionMode
        menu.findItem(R.id.action_clear_selection).isVisible = isSelectionMode && selectedFavorites.isNotEmpty()
        menu.findItem(R.id.action_remove_selected).isVisible = isSelectionMode && selectedFavorites.isNotEmpty()
        menu.findItem(R.id.action_edit).isVisible = isSelectionMode && selectedFavorites.size == 1
        menu.findItem(R.id.action_info).isVisible = isSelectionMode && selectedFavorites.size == 1
        
        // Disable drag & drop indicator when in selection mode
        menu.findItem(R.id.action_reorder).isVisible = !isSelectionMode && favorites.size > 1
        
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                showAddFavoriteDialog()
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
            R.id.action_remove_selected -> {
                removeSelected()
                true
            }
            R.id.action_edit -> {
                if (selectedFavorites.size == 1) {
                    editFavorite(selectedFavorites.first())
                }
                true
            }
            R.id.action_info -> {
                if (selectedFavorites.size == 1) {
                    showFavoriteInfo(selectedFavorites.first())
                }
                true
            }
            R.id.action_reorder -> {
                // Toggle reorder mode
                binding.recyclerView.tag = if (binding.recyclerView.tag == "reorder") {
                    itemTouchHelper.attachToRecyclerView(null)
                    null
                } else {
                    itemTouchHelper.attachToRecyclerView(binding.recyclerView)
                    "reorder"
                }
                true
            }
            R.id.action_search -> {
                searchFavorites()
                true
            }
            R.id.action_share -> {
                shareFavorites()
                true
            }
            R.id.action_export -> {
                exportFavorites()
                true
            }
            R.id.action_import -> {
                importFavorites()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        if (isSelectionMode) {
            exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }
    
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, FavoritesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
    }
}
