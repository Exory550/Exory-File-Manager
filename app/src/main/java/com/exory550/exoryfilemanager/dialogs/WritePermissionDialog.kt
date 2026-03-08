package com.exory550.exoryfilemanager.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.exory550.exoryfilemanager.BuildConfig
import com.exory550.exoryfilemanager.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.io.File

class WritePermissionDialog(
    context: Context,
    private val config: Config
) : Dialog(context, R.style.Theme_ExoryFileManager_Dialog) {

    data class Config(
        val targetPath: String? = null,
        val mode: Int = MODE_NORMAL,
        val showDoNotAskAgain: Boolean = true,
        val onPermissionGranted: () -> Unit,
        val onPermissionDenied: (() -> Unit)? = null,
        val onDoNotAskAgain: (() -> Unit)? = null
    ) {
        companion object {
            const val MODE_NORMAL = 0
            const val MODE_MANAGE_STORAGE = 1
            const val MODE_SAF = 2
        }
    }

    private lateinit var binding: View
    private lateinit var ivIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvPathInfo: TextView
    private lateinit var tvAndroidVersion: TextView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var btnGrant: Button
    private lateinit var btnCancel: Button
    private lateinit var btnSettings: Button
    private lateinit var cbDoNotAskAgain: CheckBox
    private lateinit var step1Indicator: ImageView
    private lateinit var step2Indicator: ImageView
    private lateinit var step3Indicator: ImageView
    private lateinit var step1Text: TextView
    private lateinit var step2Text: TextView
    private lateinit var step3Text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        setupViews()
        updateUIForAndroidVersion()
        setupListeners()
    }

    private fun setupViews() {
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_write_permission, null)
        setContentView(binding)

        ivIcon = binding.findViewById(R.id.ivIcon)
        tvTitle = binding.findViewById(R.id.tvTitle)
        tvMessage = binding.findViewById(R.id.tvMessage)
        tvPathInfo = binding.findViewById(R.id.tvPathInfo)
        tvAndroidVersion = binding.findViewById(R.id.tvAndroidVersion)
        progressBar = binding.findViewById(R.id.progressBar)
        btnGrant = binding.findViewById(R.id.btnGrant)
        btnCancel = binding.findViewById(R.id.btnCancel)
        btnSettings = binding.findViewById(R.id.btnSettings)
        cbDoNotAskAgain = binding.findViewById(R.id.cbDoNotAskAgain)
        step1Indicator = binding.findViewById(R.id.step1Indicator)
        step2Indicator = binding.findViewById(R.id.step2Indicator)
        step3Indicator = binding.findViewById(R.id.step3Indicator)
        step1Text = binding.findViewById(R.id.step1Text)
        step2Text = binding.findViewById(R.id.step2Text)
        step3Text = binding.findViewById(R.id.step3Text)

        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set background
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.bg_dialog_rounded)
        )

        // Show path info if available
        if (!config.targetPath.isNullOrBlank()) {
            tvPathInfo.text = context.getString(R.string.target_path, config.targetPath)
            tvPathInfo.visibility = View.VISIBLE
        } else {
            tvPathInfo.visibility = View.GONE
        }

        // Show/hide "Don't ask again" checkbox
        cbDoNotAskAgain.visibility = if (config.showDoNotAskAgain) View.VISIBLE else View.GONE

        // Update title and message based on mode
        when (config.mode) {
            Config.MODE_MANAGE_STORAGE -> {
                tvTitle.text = context.getString(R.string.manage_storage_permission_title)
                tvMessage.text = context.getString(R.string.manage_storage_permission_message)
            }
            Config.MODE_SAF -> {
                tvTitle.text = context.getString(R.string.saf_permission_title)
                tvMessage.text = context.getString(R.string.saf_permission_message)
            }
            else -> {
                tvTitle.text = context.getString(R.string.write_permission_title)
                tvMessage.text = context.getString(R.string.write_permission_message)
            }
        }
    }

    private fun updateUIForAndroidVersion() {
        val androidVersion = Build.VERSION.RELEASE
        val sdkInt = Build.VERSION.SDK_INT

        tvAndroidVersion.text = context.getString(R.string.android_version, androidVersion, sdkInt)

        when {
            sdkInt >= Build.VERSION_CODES.R -> { // Android 11+
                showStepsForAndroid11()
            }
            sdkInt >= Build.VERSION_CODES.Q -> { // Android 10
                showStepsForAndroid10()
            }
            sdkInt >= Build.VERSION_CODES.M -> { // Android 6-9
                showStepsForAndroid6To9()
            }
            else -> { // Below Android 6
                showStepsForLegacy()
            }
        }
    }

    private fun showStepsForAndroid11() {
        step1Text.text = context.getString(R.string.step_open_settings)
        step2Text.text = context.getString(R.string.step_find_app)
        step3Text.text = context.getString(R.string.step_enable_manage_storage)
        
        step1Indicator.setImageResource(R.drawable.ic_circle)
        step2Indicator.setImageResource(R.drawable.ic_circle)
        step3Indicator.setImageResource(R.drawable.ic_circle)
    }

    private fun showStepsForAndroid10() {
        step1Text.text = context.getString(R.string.step_click_grant)
        step2Text.text = context.getString(R.string.step_select_folder)
        step3Text.text = context.getString(R.string.step_allow_access)
        
        step1Indicator.setImageResource(R.drawable.ic_circle)
        step2Indicator.setImageResource(R.drawable.ic_circle)
        step3Indicator.setImageResource(R.drawable.ic_circle)
    }

    private fun showStepsForAndroid6To9() {
        step1Text.text = context.getString(R.string.step_click_grant)
        step2Text.text = context.getString(R.string.step_allow_permission)
        step3Text.visibility = View.GONE
        step3Indicator.visibility = View.GONE
        
        step1Indicator.setImageResource(R.drawable.ic_circle)
        step2Indicator.setImageResource(R.drawable.ic_circle)
    }

    private fun showStepsForLegacy() {
        step1Text.text = context.getString(R.string.step_install_file_manager)
        step2Text.text = context.getString(R.string.step_grant_permission)
        step3Text.visibility = View.GONE
        step3Indicator.visibility = View.GONE
        
        step1Indicator.setImageResource(R.drawable.ic_circle)
        step2Indicator.setImageResource(R.drawable.ic_circle)
    }

    private fun setupListeners() {
        btnGrant.setOnClickListener {
            showProgress(true)
            requestPermission()
        }

        btnCancel.setOnClickListener {
            if (cbDoNotAskAgain.isChecked) {
                config.onDoNotAskAgain?.invoke()
            } else {
                config.onPermissionDenied?.invoke()
            }
            dismiss()
        }

        btnSettings.setOnClickListener {
            openAppSettings()
        }
    }

    private fun requestPermission() {
        when (config.mode) {
            Config.MODE_MANAGE_STORAGE -> requestManageStoragePermission()
            Config.MODE_SAF -> requestSafPermission()
            else -> requestNormalPermission()
        }
    }

    private fun requestNormalPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, we need MANAGE_EXTERNAL_STORAGE
            requestManageStoragePermission()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android 6-10, request WRITE_EXTERNAL_STORAGE
            val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            
            (context as? androidx.fragment.app.FragmentActivity)?.let { activity ->
                val launcher = activity.activityResultRegistry.register(
                    "request_write_permission",
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    showProgress(false)
                    if (isGranted) {
                        config.onPermissionGranted()
                        dismiss()
                    } else {
                        showPermissionDenied()
                    }
                }
                launcher.launch(permission)
            }
        } else {
            // Below Android 6, permissions are granted at install time
            config.onPermissionGranted()
            dismiss()
        }
    }

    private fun requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            
            (context as? androidx.fragment.app.FragmentActivity)?.let { activity ->
                val launcher = activity.activityResultRegistry.register(
                    "request_manage_storage",
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    showProgress(false)
                    if (Environment.isExternalStorageManager()) {
                        config.onPermissionGranted()
                        dismiss()
                    } else {
                        showPermissionDenied()
                    }
                }
                launcher.launch(intent)
            }
        } else {
            requestNormalPermission()
        }
    }

    private fun requestSafPermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            
            config.targetPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val uri = DocumentsContract.buildDocumentUri(
                        "com.android.externalstorage.documents",
                        file.absolutePath
                    )
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                }
            }
        }

        (context as? androidx.fragment.app.FragmentActivity)?.let { activity ->
            val launcher = activity.activityResultRegistry.register(
                "request_saf",
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                showProgress(false)
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        takePersistableUriPermission(uri)
                        config.onPermissionGranted()
                        dismiss()
                    } ?: showPermissionDenied()
                } else {
                    showPermissionDenied()
                }
            }
            launcher.launch(intent)
        }
    }

    private fun takePersistableUriPermission(uri: Uri) {
        try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showPermissionDenied() {
        btnSettings.visibility = View.VISIBLE
        btnGrant.visibility = View.GONE
        tvMessage.text = context.getString(R.string.permission_denied_message)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        }
        context.startActivity(intent)
        dismiss()
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnGrant.isEnabled = !show
        btnCancel.isEnabled = !show
    }

    class Builder(private val context: Context) {
        private var targetPath: String? = null
        private var mode: Int = Config.MODE_NORMAL
        private var showDoNotAskAgain: Boolean = true
        private var onPermissionGranted: () -> Unit = {}
        private var onPermissionDenied: (() -> Unit)? = null
        private var onDoNotAskAgain: (() -> Unit)? = null

        fun setTargetPath(path: String?) = apply { this.targetPath = path }
        fun setMode(mode: Int) = apply { this.mode = mode }
        fun setShowDoNotAskAgain(show: Boolean) = apply { this.showDoNotAskAgain = show }
        fun setOnPermissionGranted(listener: () -> Unit) = apply { this.onPermissionGranted = listener }
        fun setOnPermissionDenied(listener: () -> Unit) = apply { this.onPermissionDenied = listener }
        fun setOnDoNotAskAgain(listener: () -> Unit) = apply { this.onDoNotAskAgain = listener }

        fun build(): Config {
            return Config(
                targetPath = targetPath,
                mode = mode,
                showDoNotAskAgain = showDoNotAskAgain,
                onPermissionGranted = onPermissionGranted,
                onPermissionDenied = onPermissionDenied,
                onDoNotAskAgain = onDoNotAskAgain
            )
        }

        fun show() {
            WritePermissionDialog(context, build()).show()
        }
    }

    companion object {
        fun show(context: Context, config: Config.() -> Unit) {
            val builder = Builder(context)
            config.invoke(builder)
            builder.show()
        }

        fun requestWritePermission(
            context: Context,
            onGranted: () -> Unit,
            onDenied: (() -> Unit)? = null
        ) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    if (Environment.isExternalStorageManager()) {
                        onGranted()
                    } else {
                        Builder(context)
                            .setMode(Config.MODE_MANAGE_STORAGE)
                            .setOnPermissionGranted(onGranted)
                            .setOnPermissionDenied(onDenied)
                            .show()
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        onGranted()
                    } else {
                        Builder(context)
                            .setMode(Config.MODE_NORMAL)
                            .setOnPermissionGranted(onGranted)
                            .setOnPermissionDenied(onDenied)
                            .show()
                    }
                }
                else -> {
                    onGranted()
                }
            }
        }

        fun requestSafPermission(
            context: Context,
            targetPath: String? = null,
            onGranted: () -> Unit,
            onDenied: (() -> Unit)? = null
        ) {
            Builder(context)
                .setMode(Config.MODE_SAF)
                .setTargetPath(targetPath)
                .setOnPermissionGranted(onGranted)
                .setOnPermissionDenied(onDenied)
                .show()
        }
    }
}
