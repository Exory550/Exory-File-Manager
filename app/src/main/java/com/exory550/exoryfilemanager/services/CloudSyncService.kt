package com.exory550.exoryfilemanager.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class CloudSyncService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
