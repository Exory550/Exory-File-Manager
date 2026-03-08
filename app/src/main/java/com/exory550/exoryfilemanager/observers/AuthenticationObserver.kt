package com.exory550.exoryfilemanager.observers

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Settings

class AuthenticationObserver(
    handler: Handler,
    private val onAuthenticationChanged: (Boolean) -> Unit
) : ContentObserver(handler) {

    private val uri = Settings.Secure.getUriFor(Settings.Secure.ANDROID_ID)

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        onAuthenticationChanged(true)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        onAuthenticationChanged(true)
    }

    fun getObserveUri(): Uri {
        return uri
    }
}
