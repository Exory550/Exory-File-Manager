package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.adapters.PropertiesTabAdapter
import com.exory550.exoryfilemanager.models.FileItem
import com.exory550.exoryfilemanager.utils.FileUtils
import com.exory550.exoryfilemanager.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class PropertiesDialog(
    context: Context,
    private val files: List<FileItem>,
    private val onPropertiesChanged: (() -> Unit)? = null
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog_FullScreen) {

    companion object {
        private const val HASH_BUFFER_SIZE = 8192
    }

    private lateinit var binding: View
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btnClose: ImageButton
    private lateinit var progressBar: CircularProgressIndicator
    
    private lateinit var fileUtils: FileUtils
    private lateinit var preferenceManager: PreferenceManager
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private var hashJob: Job? = null
    
    data class FileProperties(
        val name: String,
        val path: String,
        val size: Long,
        val lastModified: Long,
        val isDirectory: Boolean,
        val isHidden: Boolean,
        val isReadable: Boolean,
        val isWritable: Boolean,
        val isExecutable: Boolean,
        val mimeType: String?,
        val owner: String?,
        val group: String?,
        val permissions: String?,
        val md5: String? = null,
        val sha1: String? = null,
        val sha256: String? = null
    )
    
    data class MediaMetadata(
        val duration: Long? = null,
        val width: Int? = null,
        val height: Int? = null,
        val bitrate: Int? = null,
        val frameRate: String? = null,
        val codec: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val title: String? = null,
        val genre: String? = null,
        val trackNumber: Int? = null,
        val year: Int? = null
    )
    
    data class ImageMetadata(
        val width: Int,
        val height: Int,
        val orientation: Int,
        val compression: String?,
        val bitsPerPixel: Int?,
        val hasAlpha: Boolean
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        fileUtils = FileUtils.getInstance()
        preferenceManager = PreferenceManager.getInstance(context)
        
        setupViews()
        setupTabs()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hashJob?.cancel()
        mainScope.cancel()
    }
    
    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_properties, null)
        setContentView(binding)
        
        tabLayout = binding.findViewById(R.id.tabLayout)
        viewPager = binding.findViewById(R.id.viewPager)
        btnClose = binding.findViewById(R.id.btnClose)
        progressBar = binding.findViewById(R.id.progressBar)
        
        // Set dialog window properties
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        btnClose.setOnClickListener {
            dismiss()
        }
    }
    
    private fun setupTabs() {
        val properties = files.map { loadProperties(it) }
        val adapter = PropertiesTabAdapter(this, properties)
        viewPager.adapter = adapter
        
        val tabTitles = if (files.size == 1) {
            arrayOf(
                context.getString(R.string.general),
                context.getString(R.string.details),
                context.getString(R.string.permissions),
                context.getString(R.string.checksums)
            )
        } else {
            arrayOf(
                context.getString(R.string.general),
                context.getString(R.string.details)
            )
        }
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
    
    private fun loadProperties(fileItem: FileItem): FileProperties {
        val file = File(fileItem.path)
        
        return FileProperties(
            name = file.name,
            path = file.absolutePath,
            size = if (file.isDirectory) calculateFolderSize(file) else file.length(),
            lastModified = file.lastModified(),
            isDirectory = file.isDirectory,
            isHidden = file.isHidden,
            isReadable = file.canRead(),
            isWritable = file.canWrite(),
            isExecutable = file.canExecute(),
            mimeType = fileItem.mimeType,
            owner = getFileOwner(file),
            group = getFileGroup(file),
            permissions = getFilePermissions(file)
        )
    }
    
    private fun calculateFolderSize(file: File): Long {
        return if (file.isDirectory) {
            file.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } else {
            file.length()
        }
    }
    
    private fun getFileOwner(file: File): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val files = java.nio.file.Files.getOwner(file.toPath())
                files.name
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    private fun getFileGroup(file: File): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                java.nio.file.Files.getAttribute(file.toPath(), "posix:group") as? String
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    private fun getFilePermissions(file: File): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val perms = java.nio.file.Files.getPosixFilePermissions(file.toPath())
                perms.joinToString("") {
                    when (it.toString()) {
                        "OWNER_READ" -> "r"
                        "OWNER_WRITE" -> "w"
                        "OWNER_EXECUTE" -> "x"
                        "GROUP_READ" -> "r"
                        "GROUP_WRITE" -> "w"
                        "GROUP_EXECUTE" -> "x"
                        "OTHERS_READ" -> "r"
                        "OTHERS_WRITE" -> "w"
                        "OTHERS_EXECUTE" -> "x"
                        else -> "-"
                    }
                }
            } catch (e: Exception) {
                null
            }
        } else {
            val perms = StringBuilder()
            perms.append(if (file.canRead()) "r" else "-")
            perms.append(if (file.canWrite()) "w" else "-")
            perms.append(if (file.canExecute()) "x" else "-")
            perms.toString()
        }
    }
    
    fun calculateHashes(file: File, onHashesCalculated: (String, String, String) -> Unit) {
        hashJob = mainScope.launch(Dispatchers.IO) {
            try {
                val md5 = calculateMD5(file)
                val sha1 = calculateSHA1(file)
                val sha256 = calculateSHA256(file)
                
                withContext(Dispatchers.Main) {
                    onHashesCalculated(md5, sha1, sha256)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun calculateMD5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        return calculateHash(file, md)
    }
    
    private fun calculateSHA1(file: File): String {
        val md = MessageDigest.getInstance("SHA-1")
        return calculateHash(file, md)
    }
    
    private fun calculateSHA256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        return calculateHash(file, md)
    }
    
    private fun calculateHash(file: File, md: MessageDigest): String {
        file.inputStream().use { fis ->
            val buffer = ByteArray(HASH_BUFFER_SIZE)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
    
    fun extractMediaMetadata(file: File): MediaMetadata? {
        if (!FileUtils.isMediaFile(file.name)) return null
        
        return try {
            val retriever = MediaMetadataRetriever()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
                retriever.setDataSource(context, uri)
            } else {
                retriever.setDataSource(file.absolutePath)
            }
            
            val metadata = MediaMetadata(
                duration = extractLong(retriever, MediaMetadataRetriever.METADATA_KEY_DURATION),
                width = extractInt(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
                height = extractInt(retriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT),
                bitrate = extractInt(retriever, MediaMetadataRetriever.METADATA_KEY_BITRATE),
                frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE),
                codec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC),
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                trackNumber = extractInt(retriever, MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER),
                year = extractInt(retriever, MediaMetadataRetriever.METADATA_KEY_YEAR)
            )
            
            retriever.release()
            metadata
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun extractImageMetadata(file: File): ImageMetadata? {
        if (!FileUtils.isImageFile(file.name)) return null
        
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, options)
            
            ImageMetadata(
                width = options.outWidth,
                height = options.outHeight,
                orientation = options.outMimeType?.let { getOrientation(file) } ?: 0,
                compression = options.outMimeType,
                bitsPerPixel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    options.outConfig?.name?.let { getBitsPerPixel(it) }
                } else {
                    null
                },
                hasAlpha = options.inPreferredConfig?.name?.contains("ALPHA") ?: false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun getOrientation(file: File): Int {
        return try {
            val ei = android.media.ExifInterface(file.absolutePath)
            val orientation = ei.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getBitsPerPixel(config: String): Int {
        return when (config) {
            "ALPHA_8" -> 8
            "ARGB_4444" -> 16
            "ARGB_8888" -> 32
            "RGB_565" -> 16
            "RGBA_F16" -> 64
            "HARDWARE" -> 32
            else -> 0
        }
    }
    
    private fun extractLong(retriever: MediaMetadataRetriever, key: Int): Long? {
        return retriever.extractMetadata(key)?.toLongOrNull()
    }
    
    private fun extractInt(retriever: MediaMetadataRetriever, key: Int): Int? {
        return retriever.extractMetadata(key)?.toIntOrNull()
    }
    
    fun formatFileSize(size: Long): String {
        return Formatter.formatFileSize(context, size)
    }
    
    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
            .format(Date(timestamp))
    }
    
    fun formatDuration(durationMs: Long?): String {
        if (durationMs == null) return "-"
        
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60)) % 24
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    companion object {
        fun show(
            context: Context,
            file: FileItem,
            onPropertiesChanged: (() -> Unit)? = null
        ) {
            PropertiesDialog(context, listOf(file), onPropertiesChanged).show()
        }
        
        fun show(
            context: Context,
            files: List<FileItem>,
            onPropertiesChanged: (() -> Unit)? = null
        ) {
            PropertiesDialog(context, files, onPropertiesChanged).show()
        }
    }
}
