package com.exory550.exoryfilemanager.helpers.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.*
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class FileCrypto(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_KEY_ALIAS = "exory_file_encryption_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12
        private const val KEY_SIZE = 256
        private const val BUFFER_SIZE = 8192
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    private val secureRandom = SecureRandom()

    fun encryptFile(inputFile: File, outputFile: File, password: String? = null): Boolean {
        return try {
            val secretKey = if (password != null) deriveKeyFromPassword(password) else getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = ByteArray(IV_SIZE).apply { secureRandom.nextBytes(this) }
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            FileOutputStream(outputFile).use { fos ->
                fos.write(iv)
                FileInputStream(inputFile).use { fis ->
                    val cipherOutputStream = CipherOutputStream(fos, cipher)
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        cipherOutputStream.write(buffer, 0, bytesRead)
                    }
                    cipherOutputStream.flush()
                    cipherOutputStream.close()
                }
            }
            true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    fun decryptFile(inputFile: File, outputFile: File, password: String? = null): Boolean {
        return try {
            val secretKey = if (password != null) deriveKeyFromPassword(password) else getSecretKey() ?: return false
            FileInputStream(inputFile).use { fis ->
                val iv = ByteArray(IV_SIZE)
                if (fis.read(iv) != IV_SIZE) return false
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
                FileOutputStream(outputFile).use { fos ->
                    val cipherInputStream = CipherInputStream(fis, cipher)
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    while (cipherInputStream.read(buffer).also { bytesRead = it } != -1) {
                        fos.write(buffer, 0, bytesRead)
                    }
                }
            }
            true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    fun encryptBytes(data: ByteArray, password: String? = null): ByteArray? {
        return try {
            val secretKey = if (password != null) deriveKeyFromPassword(password) else getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = ByteArray(IV_SIZE).apply { secureRandom.nextBytes(this) }
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            iv + cipher.doFinal(data)
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    fun decryptBytes(encryptedData: ByteArray, password: String? = null): ByteArray? {
        return try {
            val secretKey = if (password != null) deriveKeyFromPassword(password) else getSecretKey() ?: return null
            val iv = encryptedData.copyOfRange(0, IV_SIZE)
            val data = encryptedData.copyOfRange(IV_SIZE, encryptedData.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            cipher.doFinal(data)
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        return keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: createSecretKey()
    }

    private fun getSecretKey(): SecretKey? {
        return keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey
    }

    private fun createSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(KEY_SIZE)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            builder.setIsStrongBoxBacked(true)
        }
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    private fun deriveKeyFromPassword(password: String): SecretKey {
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return SecretKeySpec(hash, "AES")
    }

    fun deleteKey() { keyStore.deleteEntry(AES_KEY_ALIAS) }
    fun hasKey(): Boolean = keyStore.containsAlias(AES_KEY_ALIAS)

    fun wipeFile(file: File, passes: Int = 3): Boolean {
        return try {
            if (!file.exists()) return false
            val length = file.length()
            val random = SecureRandom()
            for (pass in 1..passes) {
                when (pass) {
                    1 -> {
                        val zeros = ByteArray(BUFFER_SIZE)
                        file.outputStream().use { out ->
                            for (i in 0 until length step BUFFER_SIZE) {
                                out.write(zeros, 0, minOf(BUFFER_SIZE.toLong(), length - i).toInt())
                            }
                        }
                    }
                    2 -> {
                        val ones = ByteArray(BUFFER_SIZE).apply { fill(0xFF.toByte()) }
                        file.outputStream().use { out ->
                            for (i in 0 until length step BUFFER_SIZE) {
                                out.write(ones, 0, minOf(BUFFER_SIZE.toLong(), length - i).toInt())
                            }
                        }
                    }
                    else -> {
                        val randomBytes = ByteArray(BUFFER_SIZE)
                        file.outputStream().use { out ->
                            for (i in 0 until length step BUFFER_SIZE) {
                                random.nextBytes(randomBytes)
                                out.write(randomBytes, 0, minOf(BUFFER_SIZE.toLong(), length - i).toInt())
                            }
                        }
                    }
                }
            }
            file.delete()
            true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    fun secureDelete(file: File): Boolean = wipeFile(file, 7)
    fun encryptFileWithPassword(inputFile: File, outputFile: File, password: String): Boolean = encryptFile(inputFile, outputFile, password)
    fun decryptFileWithPassword(inputFile: File, outputFile: File, password: String): Boolean = decryptFile(inputFile, outputFile, password)
}
