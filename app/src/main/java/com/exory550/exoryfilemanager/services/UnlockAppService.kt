package com.exory550.exoryfilemanager.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.exory550.exoryfilemanager.utils.PreferenceManager

class UnlockAppService : Service() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        preferenceManager.wasAppProtectionHandled = true
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
