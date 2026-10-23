package com.example.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestor Criptográfico para Android (Cumplimiento de seguridad y cero contraseñas en plano).
 *
 * Emplea:
 * 1. Hardware Keystore de Android ("AndroidKeyStore") para generación y resguardo de la llave maestra.
 * 2. Cifrado simétrico AES-256 en modo GCM (Galois/Counter Mode) con etiqueta de autenticación de 128 bits.
 * 3. Hashing criptográfico SHA-256 con Salt aleatorio de 16 bytes para credenciales.
 */
object CryptoManager {

    private const val KEY_ALIAS = "AutoIoT_MasterKey_AES256"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    private var fallbackKey: SecretKey? = null

    private val keyStore: KeyStore? by lazy {
        try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
        } catch (_: Exception) {
            // En entornos de prueba JVM/Robolectric donde AndroidKeyStore no está registrado
            null
        }
    }

    private fun ensureMasterKeyExists() {
        val ks = keyStore
        if (ks != null) {
            try {
                if (!ks.containsAlias(KEY_ALIAS)) {
                    val keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        ANDROID_KEYSTORE
                    )
                    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setRandomizedEncryptionRequired(true)
                        .build()

                    keyGenerator.init(keyGenParameterSpec)
                    keyGenerator.generateKey()
                }
            } catch (_: Exception) {
                initFallbackKey()
            }
        } else {
            initFallbackKey()
        }
    }

    private fun initFallbackKey() {
        if (fallbackKey == null) {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            fallbackKey = keyGen.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        ensureMasterKeyExists()
        val ks = keyStore
        return if (ks != null && ks.containsAlias(KEY_ALIAS)) {
            try {
                (ks.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
            } catch (_: Exception) {
                initFallbackKey()
                fallbackKey!!
            }
        } else {
            initFallbackKey()
            fallbackKey!!
        }
    }

    /**
     * Cifra un texto en claro empleando AES-256-GCM.
     * Retorna una cadena Base64 que contiene [IV de 12 bytes + Ciphertext + Tag de autenticación].
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Concatenar IV + Texto cifrado
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Descifra una cadena previamente cifrada con [encrypt].
     */
    fun decrypt(encryptedBase64: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        if (combined.size < GCM_IV_LENGTH) {
            throw IllegalArgumentException("Longitud de datos cifrados inválida.")
        }

        val iv = ByteArray(GCM_IV_LENGTH)
        val cipherText = ByteArray(combined.size - GCM_IV_LENGTH)

        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
        System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decryptedBytes = cipher.doFinal(cipherText)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Genera un Salt criptográfico de 16 bytes.
     */
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    /**
     * Calcula el hash criptográfico SHA-256 de una contraseña combinada con un salt.
     * Cero contraseñas en texto plano: nunca se almacena la contraseña original.
     */
    fun hashPassword(password: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashBase64 = Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
        // Formato: salt$hash
        return "$saltBase64$$hashBase64"
    }

    /**
     * Verifica si una contraseña coincide con el hash salado almacenado.
     */
    fun verifyPassword(password: String, storedSaltedHash: String): Boolean {
        return try {
            val parts = storedSaltedHash.split("$")
            if (parts.size != 2) return false
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expectedHash = parts[1]

            val md = MessageDigest.getInstance("SHA-256")
            md.update(salt)
            val actualHashBytes = md.digest(password.toByteArray(Charsets.UTF_8))
            val actualHash = Base64.encodeToString(actualHashBytes, Base64.NO_WRAP)

            // Comparación de tiempo constante para evitar ataques de temporización
            MessageDigest.isEqual(expectedHash.toByteArray(), actualHash.toByteArray())
        } catch (e: Exception) {
            false
        }
    }
}
