package com.exory550.exoryfilemanager.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.ItemsAdapter
import com.exory550.exoryfilemanager.databinding.FragmentItemsBinding
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.utils.FileUtils
import com.exory550.exoryfilemanager.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ItemsFragment : Fragment() {

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ItemsAdapter

    @Inject
    lateinit var fileUtils: FileUtils

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var currentPath: String = Environment.getExternalStorageDirectory().absolutePath
    private var currentFiles: List<FileItem> = emptyList()
    private var currentViewMode = VIEW_MODE_LIST
    private var currentSortMode = SORT_BY_NAME
    private var sortAscending = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadDirectory(currentPath)
    }

    private fun setupRecyclerView() {
        adapter = ItemsAdapter(
            items = currentFiles,
            onItemClick = { fileItem ->
                if (fileItem.isDirectory) {
                    loadDirectory(fileItem.path)
                } else {
                    openFile(fileItem)
                }
            },
            onItemLongClick = { _ ->
                true
            }
        )

        binding.recyclerView.layoutManager = if (currentViewMode == VIEW_MODE_LIST) {
            LinearLayoutManager(requireContext())
        } else {
            GridLayoutManager(requireContext(), 2)
        }
        binding.recyclerView.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshDirectory()
        }
    }

    private fun loadDirectory(path: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.root.visibility = View.GONE

            try {
                val files = withContext(Dispatchers.IO) {
                    fileUtils.getFilesInDirectory(path, preferenceManager.showHiddenFiles)
                }

                currentPath = path
                currentFiles = files

                applySorting()
                updateUI()
            } catch (e: Exception) {
                com.exory550.exoryfilemanager.extensions.showToast(requireContext(), R.string.error_loading_directory)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI() {
        adapter.updateItems(currentFiles)

        if (currentFiles.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.root.visibility = View.VISIBLE
            binding.emptyView.emptyText.text = getString(R.string.folder_empty)
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.root.visibility = View.GONE
        }

        binding.pathTextView.text = currentPath
        binding.fileCountTextView.text = getString(R.string.items_count, currentFiles.size)

        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun refreshDirectory() {
        loadDirectory(currentPath)
    }

    private fun openFile(fileItem: FileItem) {
        val intent = Intent(requireContext(), FileViewerActivity::class.java).apply {
            putExtra(EXTRA_FILE_PATH, fileItem.path)
            putExtra(EXTRA_FILE_NAME, fileItem.name)
            putExtra(EXTRA_FILE_SIZE, fileItem.size)
            putExtra(EXTRA_MIME_TYPE, fileItem.mimeType)
        }
        startActivity(intent)
    }

    private fun applySorting() {
        when (currentSortMode) {
            SORT_BY_NAME -> {
                currentFiles = if (sortAscending) {
                    currentFiles.sortedBy { it.name.lowercase() }
                } else {
                    currentFiles.sortedByDescending { it.name.lowercase() }
                }
            }
            SORT_BY_SIZE -> {
                currentFiles = if (sortAscending) {
                    currentFiles.sortedBy { it.size }
                } else {
                    currentFiles.sortedByDescending { it.size }
                }
            }
            SORT_BY_DATE -> {
                currentFiles = if (sortAscending) {
                    currentFiles.sortedBy { it.lastModified }
                } else {
                    currentFiles.sortedByDescending { it.lastModified }
                }
            }
            SORT_BY_TYPE -> {
                currentFiles = if (sortAscending) {
                    currentFiles.sortedBy { it.extension }
                } else {
                    currentFiles.sortedByDescending { it.extension }
                }
            }
        }

        val (folders, files) = currentFiles.partition { it.isDirectory }
        currentFiles = folders + files
    }

    fun navigateToParent(): Boolean {
        val parentFile = File(currentPath).parentFile
        return if (parentFile != null) {
            loadDirectory(parentFile.absolutePath)
            true
        } else {
            false
        }
    }

    fun getCurrentPath(): String = currentPath

    fun setViewMode(mode: Int) {
        currentViewMode = mode
        binding.recyclerView.layoutManager = if (mode == VIEW_MODE_LIST) {
            LinearLayoutManager(requireContext())
        } else {
            GridLayoutManager(requireContext(), 2)
        }
    }

    fun setSortMode(mode: Int, ascending: Boolean) {
        currentSortMode = mode
        sortAscending = ascending
        applySorting()
        adapter.updateItems(currentFiles)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val VIEW_MODE_LIST = 0
        const val VIEW_MODE_GRID = 1

        const val SORT_BY_NAME = 0
        const val SORT_BY_SIZE = 1
        const val SORT_BY_DATE = 2
        const val SORT_BY_TYPE = 3

        private const val EXTRA_FILE_PATH = "file_path"
        private const val EXTRA_FILE_NAME = "file_name"
        private const val EXTRA_FILE_SIZE = "file_size"
        private const val EXTRA_MIME_TYPE = "mime_type"

        fun newInstance(): ItemsFragment {
            return ItemsFragment()
        }
    }
}
