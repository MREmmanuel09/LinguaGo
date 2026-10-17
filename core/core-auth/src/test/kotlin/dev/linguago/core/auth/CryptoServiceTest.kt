package dev.linguago.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/** Cifrado AES-GCM en JVM puro (Keystore no existe en unit tests). */
public class CryptoServiceTest {
    private val crypto = SoftwareCryptoService()

    @Test
    public fun `encrypt-decrypt roundtrip recupera el plaintext`() {
        val plaintext = "progreso del usuario: {xp: 120}".toByteArray(Charsets.UTF_8)
        val ciphertext = crypto.encrypt(plaintext)
        val recovered = crypto.decrypt(ciphertext)
        assertEquals(String(plaintext, Charsets.UTF_8), String(recovered!!, Charsets.UTF_8))
    }

    @Test
    public fun `ciphertext incluye IV prefix y no es plaintext`() {
        val plaintext = "hola".toByteArray(Charsets.UTF_8)
        val ciphertext = crypto.encrypt(plaintext)
        assertNotEquals(String(plaintext, Charsets.UTF_8), String(ciphertext, Charsets.UTF_8))
        assertTrue(ciphertext.size > LOCAL_IV_BYTES)
    }

    @Test
    public fun `cifrado aleatorio mismo plaintext da ciphertext distinto`() {
        val plaintext = "igual".toByteArray(Charsets.UTF_8)
        val a = crypto.encrypt(plaintext)
        val b = crypto.encrypt(plaintext)
        assertNotEquals(a.toHex(), b.toHex())
    }

    @Test
    public fun `tampering invalida el tag GCM`() {
        val ciphertext = crypto.encrypt("secreto".toByteArray(Charsets.UTF_8)).copyOf()
        ciphertext[ciphertext.size - 1] = (ciphertext[ciphertext.size - 1] + 1).toByte()
        assertNull(crypto.decrypt(ciphertext))
    }

    @Test
    public fun `descifrar bytes ajenos devuelve null`() {
        assertNull(crypto.decrypt(byteArrayOf(1, 2, 3, 4, 5)))
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private companion object {
        const val LOCAL_IV_BYTES = 12
    }
}

/** AES-GCM-256 con clave en memoria (solo para tests unitarios). */
internal class SoftwareCryptoService : dev.linguago.core.common.CryptoService {
    private val key =
        KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    override fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        return iv + cipher.doFinal(plaintext)
    }

    override fun decrypt(ciphertext: ByteArray): ByteArray? =
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ciphertext.copyOfRange(0, IV_BYTES)
            val body = ciphertext.copyOfRange(IV_BYTES, ciphertext.size)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            cipher.doFinal(body)
        } catch (error: Exception) {
            null
        }

    private companion object {
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
