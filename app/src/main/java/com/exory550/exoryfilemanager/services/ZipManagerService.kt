package com.exory550.exoryfilemanager.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.activities.MainActivity
import com.exory550.exoryfilemanager.helpers.Constants
import com.exory550.exoryfilemanager.interfaces.ZipOperationListener
import com.exory550.exoryfilemanager.tasks.CompressTask
import com.exory550.exoryfilemanager.tasks.DecompressTask
import java.io.File

class ZipManagerService : Service() {

    private val binder = ZipManagerBinder()
    private lateinit var notificationManager: NotificationManager
    private var currentOperation: ZipOperation? = null

    inner class ZipManagerBinder : Binder() {
        fun getService(): ZipManagerService = this@ZipManagerService
    }

    data class ZipOperation(
        val type: Int,
        val sourceFiles: List<File>,
        val destinationFile: File,
        val password: String? = null,
        val progress: Int = 0,
        val isRunning: Boolean = false,
        val isComplete: Boolean = false,
        val error: String? = null
    )

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "File compression and extraction notifications"
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun compressFiles(
        files: List<File>,
        destination: File,
        password: String? = null,
        listener: ZipOperationListener
    ) {
        currentOperation = ZipOperation(
            type = 0,
            sourceFiles = files,
            destinationFile = destination,
            password = password,
            isRunning = true
        )

        startForeground(Constants.NOTIFICATION_ID_PROGRESS, createProgressNotification("Compressing files..."))

        CompressTask(applicationContext, object : ZipOperationListener {
            override fun onStart(totalItems: Int, totalSize: Long) {
                listener.onStart(totalItems, totalSize)
                updateNotification("Preparing compression...", 0)
            }

            override fun onProgress(fileName: String, progress: Int, total: Int, bytesProcessed: Long, totalBytes: Long) {
                listener.onProgress(fileName, progress, total, bytesProcessed, totalBytes)
                val percent = ((progress + 1) * 100 / total).coerceIn(0, 100)
                updateNotification("Compressing: $fileName", percent)
            }

            override fun onComplete(successCount: Int, failCount: Int) {
                listener.onComplete(successCount, failCount)
                currentOperation = currentOperation?.copy(isRunning = false, isComplete = true)
                stopForeground(false)
                showCompleteNotification("Compression complete", "$successCount files compressed")
                stopSelf()
            }

            override fun onError(error: String) {
                listener.onError(error)
                currentOperation = currentOperation?.copy(isRunning = false, error = error)
                stopForeground(false)
                showErrorNotification("Compression failed", error)
                stopSelf()
            }
        }).execute(files.toTypedArray(), destination, password)
    }

    fun decompressFile(
        file: File,
        destination: File,
        password: String? = null,
        listener: ZipOperationListener
    ) {
        currentOperation = ZipOperation(
            type = 1,
            sourceFiles = listOf(file),
            destinationFile = destination,
            password = password,
            isRunning = true
        )

        startForeground(Constants.NOTIFICATION_ID_PROGRESS, createProgressNotification("Extracting files..."))

        DecompressTask(applicationContext, object : ZipOperationListener {
            override fun onStart(totalItems: Int, totalSize: Long) {
                listener.onStart(totalItems, totalSize)
                updateNotification("Preparing extraction...", 0)
            }

            override fun onProgress(fileName: String, progress: Int, total: Int, bytesProcessed: Long, totalBytes: Long) {
                listener.onProgress(fileName, progress, total, bytesProcessed, totalBytes)
                val percent = ((progress + 1) * 100 / total).coerceIn(0, 100)
                updateNotification("Extracting: $fileName", percent)
            }

            override fun onComplete(successCount: Int, failCount: Int) {
                listener.onComplete(successCount, failCount)
                currentOperation = currentOperation?.copy(isRunning = false, isComplete = true)
                stopForeground(false)
                showCompleteNotification("Extraction complete", "$successCount files extracted")
                stopSelf()
            }

            override fun onError(error: String) {
                listener.onError(error)
                currentOperation = currentOperation?.copy(isRunning = false, error = error)
                stopForeground(false)
                showErrorNotification("Extraction failed", error)
                stopSelf()
            }
        }).execute(file, destination, password)
    }

    private fun createProgressNotification(message: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Exory File Manager")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(message: String, progress: Int) {
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Exory File Manager")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID_PROGRESS, notification)
    }

    private fun showCompleteNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID_COMPLETE, notification)
    }

    private fun showErrorNotification(title: String, error: String) {
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(error)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID_ERROR, notification)
    }

    fun cancelOperation() {
        currentOperation = currentOperation?.copy(isRunning = false)
        stopForeground(true)
        stopSelf()
    }

    fun getCurrentOperation(): ZipOperation? = currentOperation

    fun isRunning(): Boolean = currentOperation?.isRunning == true
}
