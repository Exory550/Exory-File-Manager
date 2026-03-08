package com.exory550.exoryfilemanager.helpers.crypto

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class PrefCrypto(private val context: Context) {

    companion object {
        private const val KEY_ALIAS = "exory_pref_encryption_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFS_NAME = "exory_secure_prefs"
    }

    private val masterKeyAlias: String by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        } else {
            createPreMKey()
            KEY_ALIAS
        }
    }

    val securePreferences: SharedPreferences by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } else {
            createPreMEncryptedPrefs()
        }
    }

    private fun createPreMKey(): String {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
        return KEY_ALIAS
    }

    private fun createPreMEncryptedPrefs(): SharedPreferences {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        return object : SharedPreferences {
            private val prefs = context.getSharedPreferences("${PREFS_NAME}_encrypted", Context.MODE_PRIVATE)

            override fun edit(): Editor = EditorImpl(prefs.edit(), secretKey)

            override fun getAll(): MutableMap<String, *> {
                val map = mutableMapOf<String, Any>()
                prefs.all.forEach { (key, value) ->
                    if (value is String && value.startsWith("ENC:")) {
                        decryptValue(value, secretKey)?.let { map[key] = it }
                    } else {
                        map[key] = value
                    }
                }
                return map
            }

            override fun getString(key: String, defValue: String?): String? {
                val value = prefs.getString(key, null) ?: return defValue
                return if (value.startsWith("ENC:")) {
                    decryptValue(value, secretKey) ?: defValue
                } else {
                    value
                }
            }

            override fun getStringSet(key: String, defValue: MutableSet<String>?): MutableSet<String>? {
                return prefs.getStringSet(key, defValue)
            }

            override fun getInt(key: String, defValue: Int): Int = prefs.getInt(key, defValue)
            override fun getLong(key: String, defValue: Long): Long = prefs.getLong(key, defValue)
            override fun getFloat(key: String, defValue: Float): Float = prefs.getFloat(key, defValue)
            override fun getBoolean(key: String, defValue: Boolean): Boolean = prefs.getBoolean(key, defValue)
            override fun contains(key: String): Boolean = prefs.contains(key)
            override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
                prefs.registerOnSharedPreferenceChangeListener(listener)
            }
            override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    private inner class EditorImpl(
        private val editor: SharedPreferences.Editor,
        private val secretKey: SecretKey
    ) : SharedPreferences.Editor by editor {

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            return if (value != null) {
                editor.putString(key, "ENC:" + encryptValue(value, secretKey))
            } else {
                editor.putString(key, null)
            }
        }

        override fun commit(): Boolean = editor.commit()
        override fun apply() = editor.apply()
    }

    private fun encryptValue(value: String, secretKey: SecretKey): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(value.toByteArray())
            android.util.Base64.encodeToString(iv + encrypted, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun decryptValue(encryptedValue: String, secretKey: SecretKey): String? {
        return try {
            val decoded = android.util.Base64.decode(encryptedValue.substring(4), android.util.Base64.DEFAULT)
            val iv = decoded.copyOfRange(0, 12)
            val data = decoded.copyOfRange(12, decoded.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            String(cipher.doFinal(data))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun encryptString(value: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val key = getSecretKey() ?: return value
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(value.toByteArray())
            android.util.Base64.encodeToString(iv + encrypted, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            value
        }
    }

    fun decryptString(encryptedValue: String): String? {
        return try {
            val decoded = android.util.Base64.decode(encryptedValue, android.util.Base64.DEFAULT)
            val iv = decoded.copyOfRange(0, 12)
            val data = decoded.copyOfRange(12, decoded.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val key = getSecretKey() ?: return null
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(data))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun hasEncryptionKey(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            false
        }
    }

    fun deleteEncryptionKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
            keyStore.deleteEntry(KEY_ALIAS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
