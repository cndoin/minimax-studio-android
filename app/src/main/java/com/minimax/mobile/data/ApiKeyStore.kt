package com.minimax.mobile.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Stores the API key in the app's private storage, encrypted when possible. */
class ApiKeyStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun readKey(): String {
        val encrypted = preferences.getString(KEY_API, "").orEmpty()
        if (encrypted.isNotBlank()) {
            val decrypted = runCatching { decrypt(encrypted) }.getOrNull()
            if (!decrypted.isNullOrBlank()) return decrypted
        }
        return preferences.getString(KEY_API_FALLBACK, "").orEmpty()
    }

    fun readRegion(): MiniMaxRegion {
        return MiniMaxRegion.fromId(preferences.getString(KEY_REGION, MiniMaxRegion.CN.id))
    }

    fun save(apiKey: String, region: MiniMaxRegion) {
        val cleanKey = apiKey.trim()
        val editor = preferences.edit().putString(KEY_REGION, region.id)

        if (cleanKey.isBlank()) {
            editor.remove(KEY_API).remove(KEY_API_FALLBACK).apply()
            return
        }

        // Some vendor Android Keystore implementations reject AES-256 or the
        // old key format. Keep this UI action non-fatal and try a local fallback.
        val encrypted = runCatching { encrypt(cleanKey) }.getOrNull()
        if (encrypted != null) {
            editor.putString(KEY_API, encrypted).remove(KEY_API_FALLBACK).apply()
        } else {
            editor.remove(KEY_API).putString(KEY_API_FALLBACK, cleanKey).apply()
        }
    }

    fun clear() {
        preferences.edit().clear().apply()
        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                .takeIf { it.containsAlias(KEY_ALIAS) }
                ?.deleteEntry(KEY_ALIAS)
        }
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val existing = runCatching {
                (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
            }.getOrNull()
            if (existing != null) return existing
            runCatching { keyStore.deleteEntry(KEY_ALIAS) }
        }

        return generateKey(256) ?: generateKey(128)
        ?: error("当前设备无法创建安全存储密钥")
    }

    private fun generateKey(size: Int): SecretKey? {
        return runCatching {
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setKeySize(size)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
                init(spec)
            }.generateKey()
        }.getOrNull()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val combined = Base64.decode(value, Base64.NO_WRAP)
        require(combined.size > IV_LENGTH)
        val iv = combined.copyOfRange(0, IV_LENGTH)
        val encrypted = combined.copyOfRange(IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "minimax-studio-api-key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val PREFERENCES = "mini_max_secure_config"
        private const val KEY_API = "api_key"
        private const val KEY_API_FALLBACK = "api_key_local_fallback"
        private const val KEY_REGION = "region"
    }
}
