package com.exory550.exoryfilemanager.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.FileAdapter
import com.exory550.exoryfilemanager.databinding.ActivitySaveAsBinding
import com.exory550.exoryfilemanager.extensions.*
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.utils.Constants
import com.exory550.exoryfilemanager.utils.FileUtils
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SaveAsActivity : BaseAbstractActivity() {

    override val layoutRes: Int = R.layout.activity_save_as
    
    private lateinit var binding: ActivitySaveAsBinding
    private lateinit var fileAdapter: FileAdapter
    
    @Inject
    lateinit var fileUtils: FileUtils
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var currentPath: String = Environment.getExternalStorageDirectory().absolutePath
    private var currentFiles: List<ExoryFileItem> = emptyList()
    private var sourceData: ByteArray? = null
    private var sourceUri: Uri? = null
    private var sourceFile: File? = null
    private var suggestedFileName: String = ""
    private var mimeType: String = "*/*"
    private var fileSize: Long = 0
    
    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        uri?.let { saveToUri(it) }
    }
    
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                saveToUri(uri)
            }
        }
    }
    
    override fun initializeViews() {
        binding = ActivitySaveAsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupPathNavigation()
        parseIntent()
        loadDirectory(currentPath)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.save_as)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finishWithAnimation()
        }
        
        binding.btnSave.setOnClickListener {
            showSaveDialog()
        }
        
        binding.btnNewFolder.setOnClickListener {
            showCreateFolderDialog()
        }
    }
    
    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            onItemClick = { fileItem ->
                if (fileItem.isDirectory) {
                    navigateToDirectory(fileItem.path)
                }
            },
            onItemLongClick = { _ ->
                false
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SaveAsActivity)
            adapter = fileAdapter
        }
    }
    
    private fun setupPathNavigation() {
        binding.pathChipGroup.setOnItemClickListener { _, position, _ ->
            val path = getPathFromChip(position)
            navigateToDirectory(path)
        }
        
        binding.btnNavigateUp.setOnClickListener {
            navigateUp()
        }
        
        binding.btnRefresh.setOnClickListener {
            refreshDirectory()
        }
    }
    
    private fun parseIntent() {
        intent?.let {
            when {
                it.hasExtra(Constants.EXTRA_DATA) -> {
                    sourceData = it.getByteArrayExtra(Constants.EXTRA_DATA)
                    suggestedFileName = it.getStringExtra(Constants.EXTRA_FILE_NAME) ?: 
                        "file_${System.currentTimeMillis()}"
                    mimeType = it.getStringExtra(Constants.EXTRA_MIME_TYPE) ?: "*/*"
                    fileSize = sourceData?.size?.toLong() ?: 0
                }
                it.hasExtra(Constants.EXTRA_URI) -> {
                    sourceUri = it.getParcelableExtra(Constants.EXTRA_URI)
                    suggestedFileName = it.getStringExtra(Constants.EXTRA_FILE_NAME) ?: 
                        "file_${System.currentTimeMillis()}"
                    mimeType = it.getStringExtra(Constants.EXTRA_MIME_TYPE) ?: "*/*"
                    
                    lifecycleScope.launch {
                        fileSize = withContext(Dispatchers.IO) {
                            sourceUri?.let { uri ->
                                contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                                    pfd.statSize
                                } ?: 0
                            } ?: 0
                        }
                    }
                }
                it.hasExtra(Constants.EXTRA_FILE_PATH) -> {
                    val path = it.getStringExtra(Constants.EXTRA_FILE_PATH)
                    sourceFile = path?.let { path -> File(path) }
                    suggestedFileName = sourceFile?.name ?: "file_${System.currentTimeMillis()}"
                    mimeType = it.getStringExtra(Constants.EXTRA_MIME_TYPE) ?: "*/*"
                    fileSize = sourceFile?.length() ?: 0
                }
            }
        }
        
        binding.fileNameEdit.setText(suggestedFileName)
        updateFileInfo()
    }
    
    private fun updateFileInfo() {
        binding.apply {
            fileSizeText.text = getString(R.string.file_size, fileUtils.formatFileSize(fileSize))
            fileTypeText.text = getString(R.string.file_type, mimeType)
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
                currentFiles = files.filter { it.isDirectory } + files.filter { !it.isDirectory }
                
                updateUI()
                updatePathChips()
                
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
            directoryInfoText.text = getString(R.string.items_count, currentFiles.size)
        }
    }
    
    private fun updatePathChips() {
        binding.pathChipGroup.removeAllViews()
        
        val parts = currentPath.split(File.separator).filter { it.isNotEmpty() }
        var cumulativePath = ""
        
        // Add root
        val rootChip = createPathChip(getString(R.string.root))
        rootChip.setOnClickListener {
            navigateToDirectory(File.separator)
        }
        binding.pathChipGroup.addView(rootChip)
        
        parts.forEachIndexed { index, part ->
            cumulativePath += File.separator + part
            val chip = createPathChip(part)
            chip.setOnClickListener {
                navigateToDirectory(cumulativePath)
            }
            binding.pathChipGroup.addView(chip)
        }
    }
    
    private fun createPathChip(text: String): com.google.android.material.chip.Chip {
        return com.google.android.material.chip.Chip(this).apply {
            this.text = text
            isClickable = true
            isCheckable = false
            chipBackgroundColor = androidx.core.content.ContextCompat.getColorStateList(
                this@SaveAsActivity,
                R.color.chip_background
            )
            setTextColor(androidx.core.content.ContextCompat.getColorStateList(
                this@SaveAsActivity,
                R.color.chip_text
            ))
        }
    }
    
    private fun getPathFromChip(position: Int): String {
        if (position == 0) return File.separator
        
        val parts = currentPath.split(File.separator).filter { it.isNotEmpty() }
        return File.separator + parts.take(position).joinToString(File.separator)
    }
    
    private fun navigateToDirectory(path: String) {
        loadDirectory(path)
    }
    
    private fun navigateUp() {
        val parentFile = File(currentPath).parentFile
        if (parentFile != null) {
            loadDirectory(parentFile.absolutePath)
        }
    }
    
    private fun refreshDirectory() {
        loadDirectory(currentPath)
    }
    
    private fun showCreateFolderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.inputLayout)
        val inputEditText = dialogView.findViewById<TextInputEditText>(R.id.inputEditText)
        
        inputLayout.hint = getString(R.string.folder_name)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.create_folder)
            .setView(dialogView)
            .setPositiveButton(R.string.create) { _, _ ->
                val folderName = inputEditText.text.toString()
                if (folderName.isNotBlank()) {
                    createFolder(folderName)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun createFolder(name: String) {
        lifecycleScope.launch {
            showProgress(R.string.creating)
            
            try {
                val folder = File(currentPath, name)
                val success = withContext(Dispatchers.IO) {
                    folder.mkdirs()
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
    
    private fun showSaveDialog() {
        val fileName = binding.fileNameEdit.text.toString()
        
        if (fileName.isBlank()) {
            binding.fileNameEdit.error = getString(R.string.error_file_name_required)
            return
        }
        
        val targetFile = File(currentPath, fileName)
        
        if (targetFile.exists()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.file_exists)
                .setMessage(getString(R.string.file_exists_message, fileName))
                .setPositiveButton(R.string.overwrite) { _, _ ->
                    saveFile(targetFile)
                }
                .setNegativeButton(R.string.rename) { _, _ ->
                    suggestNewFileName(fileName)
                }
                .setNeutralButton(R.string.cancel, null)
                .show()
        } else {
            saveFile(targetFile)
        }
    }
    
    private fun suggestNewFileName(originalName: String) {
        val nameWithoutExt = originalName.substringBeforeLast(".")
        val extension = originalName.substringAfterLast(".", "")
        
        var counter = 1
        var newName: String
        do {
            newName = if (extension.isEmpty()) {
                "${nameWithoutExt}_$counter"
            } else {
                "${nameWithoutExt}_$counter.$extension"
            }
            counter++
        } while (File(currentPath, newName).exists())
        
        binding.fileNameEdit.setText(newName)
    }
    
    private fun saveFile(file: File) {
        lifecycleScope.launch {
            showProgress(R.string.saving)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    when {
                        sourceData != null -> {
                            file.writeBytes(sourceData!!)
                            true
                        }
                        sourceUri != null -> {
                            contentResolver.openInputStream(sourceUri!!)?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            } ?: false
                        }
                        sourceFile != null -> {
                            sourceFile!!.copyTo(file, overwrite = true)
                            true
                        }
                        else -> false
                    }
                }
                
                if (success) {
                    // Scan file for media store
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val values = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, file.parent)
                            put(MediaStore.MediaColumns.SIZE, file.length())
                            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                            put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        }
                        
                        val collection = when {
                            mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            mimeType.startsWith("audio/") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            else -> MediaStore.Files.getContentUri("external")
                        }
                        
                        contentResolver.insert(collection, values)
                    } else {
                        // Broadcast file scan for older Android
                        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        mediaScanIntent.data = Uri.fromFile(file)
                        sendBroadcast(mediaScanIntent)
                    }
                    
                    dismissProgress()
                    
                    // Return result
                    val resultIntent = Intent().apply {
                        putExtra(Constants.EXTRA_FILE_PATH, file.absolutePath)
                        putExtra(Constants.EXTRA_FILE_NAME, file.name)
                        putExtra(Constants.EXTRA_FILE_SIZE, file.length())
                        putExtra(Constants.EXTRA_MIME_TYPE, mimeType)
                        
                        // Return URI for the saved file
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val uri = FileProvider.getUriForFile(
                                this@SaveAsActivity,
                                "${BuildConfig.APPLICATION_ID}.fileprovider",
                                file
                            )
                            putExtra(Constants.EXTRA_URI, uri)
                        }
                    }
                    
                    setResult(RESULT_OK, resultIntent)
                    finishWithAnimation()
                    
                } else {
                    dismissProgress()
                    showErrorSnackbar(R.string.save_error)
                }
            } catch (e: Exception) {
                dismissProgress()
                showErrorSnackbar(R.string.save_error)
            }
        }
    }
    
    private fun saveToUri(uri: Uri) {
        lifecycleScope.launch {
            showProgress(R.string.saving)
            
            try {
                val success = withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { output ->
                        when {
                            sourceData != null -> {
                                output.write(sourceData!!)
                                true
                            }
                            sourceUri != null -> {
                                contentResolver.openInputStream(sourceUri!!)?.use { input ->
                                    input.copyTo(output)
                                }
                                true
                            }
                            sourceFile != null -> {
                                sourceFile!!.inputStream().use { input ->
                                    input.copyTo(output)
                                }
                                true
                            }
                            else -> false
                        }
                    } ?: false
                }
                
                if (success) {
                    dismissProgress()
                    
                    val resultIntent = Intent().apply {
                        putExtra(Constants.EXTRA_URI, uri)
                        putExtra(Constants.EXTRA_FILE_NAME, suggestedFileName)
                        putExtra(Constants.EXTRA_MIME_TYPE, mimeType)
                    }
                    
                    setResult(RESULT_OK, resultIntent)
                    finishWithAnimation()
                } else {
                    dismissProgress()
                    showErrorSnackbar(R.string.save_error)
                }
            } catch (e: Exception) {
                dismissProgress()
                showErrorSnackbar(R.string.save_error)
            }
        }
    }
    
    private fun showSaveToExternal() {
        createFileLauncher.launch(suggestedFileName)
    }
    
    private fun showAdvancedOptions() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.save_options)
            .setItems(R.array.save_options) { _, which ->
                when (which) {
                    0 -> showSaveToExternal()
                    1 -> shareFile()
                    2 -> showFileProperties()
                }
            }
            .show()
    }
    
    private fun shareFile() {
        lifecycleScope.launch {
            showProgress(R.string.preparing_share)
            
            try {
                val uri = withContext(Dispatchers.IO) {
                    if (sourceFile != null) {
                        FileProvider.getUriForFile(
                            this@SaveAsActivity,
                            "${BuildConfig.APPLICATION_ID}.fileprovider",
                            sourceFile!!
                        )
                    } else if (sourceUri != null) {
                        sourceUri
                    } else if (sourceData != null) {
                        // Create temp file for sharing
                        val tempFile = File(cacheDir, "share_${System.currentTimeMillis()}_$suggestedFileName")
                        tempFile.writeBytes(sourceData!!)
                        FileProvider.getUriForFile(
                            this@SaveAsActivity,
                            "${BuildConfig.APPLICATION_ID}.fileprovider",
                            tempFile
                        )
                    } else {
                        null
                    }
                }
                
                dismissProgress()
                
                uri?.let {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, it)
                        type = mimeType
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_file)))
                }
            } catch (e: Exception) {
                dismissProgress()
                showErrorSnackbar(R.string.share_error)
            }
        }
    }
    
    private fun showFileProperties() {
        val properties = buildString {
            appendLine(getString(R.string.file_name, suggestedFileName))
            appendLine(getString(R.string.file_size, fileUtils.formatFileSize(fileSize)))
            appendLine(getString(R.string.file_type, mimeType))
            sourceFile?.let { file ->
                appendLine(getString(R.string.modified, 
                    SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
                        .format(Date(file.lastModified()))))
            }
            appendLine(getString(R.string.save_location, currentPath))
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.file_properties)
            .setMessage(properties)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_save_as, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_to_external -> {
                showSaveToExternal()
                true
            }
            R.id.action_advanced -> {
                showAdvancedOptions()
                true
            }
            R.id.action_new_folder -> {
                showCreateFolderDialog()
                true
            }
            R.id.action_refresh -> {
                refreshDirectory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        if (File(currentPath).parentFile != null) {
            navigateUp()
        } else {
            super.onBackPressed()
        }
    }
    
    companion object {
        fun getIntent(context: Context, data: ByteArray, fileName: String, mimeType: String): Intent {
            return Intent(context, SaveAsActivity::class.java).apply {
                putExtra(Constants.EXTRA_DATA, data)
                putExtra(Constants.EXTRA_FILE_NAME, fileName)
                putExtra(Constants.EXTRA_MIME_TYPE, mimeType)
            }
        }
        
        fun getIntent(context: Context, uri: Uri, fileName: String, mimeType: String): Intent {
            return Intent(context, SaveAsActivity::class.java).apply {
                putExtra(Constants.EXTRA_URI, uri)
                putExtra(Constants.EXTRA_FILE_NAME, fileName)
                putExtra(Constants.EXTRA_MIME_TYPE, mimeType)
            }
        }
        
        fun getIntent(context: Context, filePath: String, mimeType: String): Intent {
            return Intent(context, SaveAsActivity::class.java).apply {
                putExtra(Constants.EXTRA_FILE_PATH, filePath)
                putExtra(Constants.EXTRA_MIME_TYPE, mimeType)
                putExtra(Constants.EXTRA_FILE_NAME, File(filePath).name)
            }
        }
    }
}
