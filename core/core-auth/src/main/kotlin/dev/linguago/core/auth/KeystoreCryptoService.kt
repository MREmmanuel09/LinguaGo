package dev.linguago.core.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dev.linguago.core.common.CryptoService
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cifrado AEAD con clave en AndroidKeyStore (ADR-014): AES-GCM-256, IV
 * aleatorio por operación y tag de 128 bits. La clave nunca sale del
 * Keystore; si el dispositivo no lo soporta, el constructor lanza y DI
 * degrada a NoOpCryptoService (la app sigue funcional, I3).
 */
@Singleton
public class KeystoreCryptoService
    @Inject
    constructor() : CryptoService {
        private val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
        private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        init {
            if (keyStore.getEntry(KEY_ALIAS, null) == null) {
                generateKey()
            }
        }

        override fun encrypt(plaintext: ByteArray): ByteArray {
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(plaintext)
            return iv + ciphertext
        }

        override fun decrypt(payload: ByteArray): ByteArray? =
            runCatching {
                val iv = payload.copyOfRange(0, IV_BYTES)
                val body = payload.copyOfRange(IV_BYTES, payload.size)
                cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
                cipher.doFinal(body)
            }.getOrNull()

        private fun getOrCreateKey(): java.security.Key =
            (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
                ?: generateKey()

        private fun generateKey(): java.security.Key =
            KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                .apply {
                    init(
                        KeyGenParameterSpec
                            .Builder(
                                KEY_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .build(),
                    )
                }.generateKey()

        private companion object {
            const val ANDROID_KEYSTORE = "AndroidKeyStore"
            const val KEY_ALIAS = "linguago_crypto_key"
            const val TRANSFORMATION = "AES/GCM/NoPadding"
            const val IV_BYTES = 12
            const val TAG_BITS = 128
        }
    }
