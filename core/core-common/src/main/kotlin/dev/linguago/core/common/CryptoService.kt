package dev.linguago.core.common

/**
 * Contrato de cifrado (ADR-014): transforma plaintext ↔ ciphertext con
 * AEAD (AES-GCM). Los bytes que salen de `encrypt` son opacos: el prefijo
 * es el IV aleatorio y el sufijo el tag GCM.
 *
 * Implementaciones: `KeystoreCryptoService` (prod, AndroidKeyStore) y
 * `NoOpCryptoService` (tests / fallback de emergencia).
 */
public interface CryptoService {
    /** Cifra plaintext y devuelve `IV || ciphertext || tag`. */
    public fun encrypt(plaintext: ByteArray): ByteArray

    /**
     * Descifra el payload de `encrypt`. Devuelve `null` si el tag no
     * valida (payload corrupto o ajeno); nunca devuelve texto parcial.
     */
    public fun decrypt(ciphertext: ByteArray): ByteArray?
}

/**
 * Cifrado identidad (sin encriptación): solo para tests, emulador de
 * laboratorio y como fallback extremo cuando el Keystore no está
 * disponible. La app sigue 100% funcional (I3) pero no cifra.
 */
public class NoOpCryptoService : CryptoService {
    override fun encrypt(plaintext: ByteArray): ByteArray = plaintext.copyOf()

    override fun decrypt(ciphertext: ByteArray): ByteArray? = ciphertext.copyOf()
}
