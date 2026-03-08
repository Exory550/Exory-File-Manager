package com.exory550.exoryfilemanager.helpers.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class Key private constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"

        fun createAesKey(alias: String, keySize: Int = 256): SecretKey? {
            return try {
                val keyGenerator = javax.crypto.KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )

                val builder = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(keySize)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(false)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    builder.setIsStrongBoxBacked(true)
                }

                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun createRsaKeyPair(
            alias: String,
            keySize: Int = 2048,
            purpose: Int = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ): KeyPair? {
            return try {
                val keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA,
                    ANDROID_KEYSTORE
                )

                val builder = KeyGenParameterSpec.Builder(
                    alias,
                    purpose
                )
                    .setKeySize(keySize)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1,
                        KeyProperties.ENCRYPTION_PADDING_RSA_OAEP
                    )
                    .setDigests(
                        KeyProperties.DIGEST_SHA256,
                        KeyProperties.DIGEST_SHA512
                    )
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(false)

                keyPairGenerator.initialize(builder.build())
                keyPairGenerator.generateKeyPair()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getSecretKey(alias: String): SecretKey? {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                keyStore.getKey(alias, null) as? SecretKey
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getPrivateKey(alias: String): PrivateKey? {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                keyStore.getKey(alias, null) as? PrivateKey
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getPublicKey(alias: String): PublicKey? {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                val certificate = keyStore.getCertificate(alias) ?: return null
                certificate.publicKey
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getCertificate(alias: String): Certificate? {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                keyStore.getCertificate(alias)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun deleteKey(alias: String): Boolean {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                keyStore.deleteEntry(alias)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun hasKey(alias: String): Boolean {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                keyStore.containsAlias(alias)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun generatePasswordBasedKey(password: String, salt: ByteArray): SecretKey {
            val keySpec = javax.crypto.spec.PBEKeySpec(
                password.toCharArray(),
                salt,
                10000,
                256
            )
            val keyFactory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val secret = keyFactory.generateSecret(keySpec)
            return SecretKeySpec(secret.encoded, "AES")
        }

        fun generateSalt(size: Int = 32): ByteArray {
            val random = java.security.SecureRandom()
            val salt = ByteArray(size)
            random.nextBytes(salt)
            return salt
        }

        fun importAesKey(alias: String, keyBytes: ByteArray): Boolean {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                val secretKey = SecretKeySpec(keyBytes, "AES")
                val keyStoreEntry = KeyStore.SecretKeyEntry(secretKey)
                val protectionParam = KeyStore.PasswordProtection(null)
                keyStore.setEntry(alias, keyStoreEntry, protectionParam)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun exportAesKey(alias: String): ByteArray? {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                val secretKey = keyStore.getKey(alias, null) as? SecretKey ?: return null
                secretKey.encoded
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getAvailableAliases(): List<String> {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                    load(null)
                }
                keyStore.aliases().toList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
