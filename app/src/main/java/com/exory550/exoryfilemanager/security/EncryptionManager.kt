package com.exory550.exoryfilemanager.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun wipeAllEncryptedData() {}

    fun hashPassword(password: String): String {
        return com.exory550.exoryfilemanager.helpers.crypto.Password.hashPassword(password)
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return com.exory550.exoryfilemanager.helpers.crypto.Password.verifyPassword(password, hash)
    }

    companion object {
        @Volatile
        private var instance: EncryptionManager? = null

        fun getInstance(context: Context): EncryptionManager {
            return instance ?: synchronized(this) {
                instance ?: EncryptionManager(context).also { instance = it }
            }
        }
    }
}
