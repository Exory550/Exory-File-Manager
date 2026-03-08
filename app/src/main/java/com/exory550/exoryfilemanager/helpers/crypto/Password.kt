package com.exory550.exoryfilemanager.helpers.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Password private constructor() {

    companion object {
        private const val PBKDF2_ITERATIONS = 10000
        private const val HASH_SIZE = 256
        private const val SALT_SIZE = 32

        fun hashPassword(password: String): String {
            val salt = generateSalt()
            val hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_SIZE)
            return Base64.encodeToString(salt) + ":" + Base64.encodeToString(hash)
        }

        fun verifyPassword(password: String, storedHash: String): Boolean {
            return try {
                val parts = storedHash.split(":")
                if (parts.size != 2) return false

                val salt = Base64.decode(parts[0], Base64.DEFAULT)
                val hash = Base64.decode(parts[1], Base64.DEFAULT)

                val testHash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_SIZE)
                MessageDigest.isEqual(hash, testHash)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun generateSalt(size: Int = SALT_SIZE): ByteArray {
            val random = SecureRandom()
            val salt = ByteArray(size)
            random.nextBytes(salt)
            return salt
        }

        fun pbkdf2(
            password: CharArray,
            salt: ByteArray,
            iterations: Int,
            keyLength: Int
        ): ByteArray {
            val spec: KeySpec = PBEKeySpec(password, salt, iterations, keyLength)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            return factory.generateSecret(spec).encoded
        }

        fun deriveKey(password: String, salt: ByteArray): SecretKey {
            val keySpec = PBEKeySpec(
                password.toCharArray(),
                salt,
                PBKDF2_ITERATIONS,
                256
            )
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val secret = keyFactory.generateSecret(keySpec)
            return SecretKeySpec(secret.encoded, "AES")
        }

        fun generateRandomPassword(length: Int = 16): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()"
            val random = SecureRandom()
            return (1..length)
                .map { chars[random.nextInt(chars.length)] }
                .joinToString("")
        }

        fun checkPasswordStrength(password: String): PasswordStrength {
            var score = 0

            if (password.length >= 8) score++
            if (password.length >= 12) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { it.isLowerCase() }) score++
            if (password.any { it.isUpperCase() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++

            return when {
                score < 3 -> PasswordStrength.WEAK
                score < 5 -> PasswordStrength.MEDIUM
                else -> PasswordStrength.STRONG
            }
        }

        fun getPasswordRequirements(): List<String> {
            return listOf(
                "At least 8 characters",
                "At least one uppercase letter",
                "At least one lowercase letter",
                "At least one number",
                "At least one special character"
            )
        }

        fun hashWithSha256(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray())
            return Base64.encodeToString(hash)
        }

        fun hashWithSha512(input: String): String {
            val digest = MessageDigest.getInstance("SHA-512")
            val hash = digest.digest(input.toByteArray())
            return Base64.encodeToString(hash)
        }

        fun hashWithMd5(input: String): String {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(input.toByteArray())
            return hash.joinToString("") { "%02x".format(it) }
        }

        fun encryptPassword(password: String, masterKey: ByteArray): ByteArray {
            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            val secretKey = SecretKeySpec(masterKey, "AES")
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(password.toByteArray())
            return iv + encrypted
        }

        fun decryptPassword(encryptedData: ByteArray, masterKey: ByteArray): String? {
            return try {
                val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
                val secretKey = SecretKeySpec(masterKey, "AES")
                val iv = encryptedData.copyOfRange(0, 12)
                val data = encryptedData.copyOfRange(12, encryptedData.size)
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, javax.crypto.spec.GCMParameterSpec(128, iv))
                String(cipher.doFinal(data))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    enum class PasswordStrength {
        WEAK,
        MEDIUM,
        STRONG
    }
}
