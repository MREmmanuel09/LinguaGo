package dev.linguago.core.audio

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.text.Normalizer

/**
 * Datos fonéticos precalculados para una voz Piper (ADR-057). Formato LGP2:
 * tabla de frases deduplicadas (u16 codepoints) y entradas texto→frases.
 * El APK nunca incluye espeak-ng; este archivo lo reemplaza en runtime.
 */
internal class VoicePhonemeData private constructor(
    private val entries: Map<String, Array<IntArray>>,
) {
    /** `true` si [text] tiene fonemas precalculados para esta voz. */
    fun canSpeak(text: String): Boolean = entries.containsKey(normalizeKey(text))

    /** Frases de codepoints para [text], o `null` si no hay cobertura. */
    fun sentencesFor(text: String): Array<IntArray>? = entries[normalizeKey(text)]

    internal companion object {
        private val MAGIC = byteArrayOf('L'.code.toByte(), 'G'.code.toByte(), 'P'.code.toByte(), '2'.code.toByte())
        private const val VERSION: Int = 2
        private val WHITESPACE = Regex("\\s+")

        fun normalizeKey(text: String): String {
            val normalized =
                Normalizer
                    .normalize(text, Normalizer.Form.NFC)
                    .replace("\u200B", "")
                    .replace("\uFEFF", "")
            return WHITESPACE.replace(normalized, " ").trim()
        }

        fun load(file: File): VoicePhonemeData? =
            runCatching {
                val bytes = file.readBytes()
                require(bytes.size >= 12) { "phonemes.bin truncado" }
                val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                val magic = ByteArray(4)
                buffer.get(magic)
                require(magic.contentEquals(MAGIC)) { "magic inválido" }
                val version = buffer.short.toInt() and 0xFFFF
                buffer.short // flags
                require(version == VERSION) { "versión $version no soportada" }
                val sentenceCount = buffer.int
                val sentences = Array(sentenceCount) { IntArray(0) }
                repeat(sentenceCount) { index ->
                    val count = buffer.short.toInt() and 0xFFFF
                    val cps = IntArray(count)
                    repeat(count) { i -> cps[i] = buffer.short.toInt() and 0xFFFF }
                    sentences[index] = cps
                }
                val entryCount = buffer.int
                val map = HashMap<String, Array<IntArray>>(entryCount * 4 / 3 + 1)
                repeat(entryCount) {
                    val keyLen = buffer.short.toInt() and 0xFFFF
                    val keyBytes = ByteArray(keyLen)
                    buffer.get(keyBytes)
                    val key = String(keyBytes, StandardCharsets.UTF_8)
                    val refCount = buffer.short.toInt() and 0xFFFF
                    val refs = Array(refCount) { IntArray(0) }
                    repeat(refCount) { i ->
                        val ref = buffer.short.toInt() and 0xFFFF
                        refs[i] = sentences[ref]
                    }
                    map[key] = refs
                }
                require(!buffer.hasRemaining()) { "bytes sobrantes en phonemes.bin" }
                VoicePhonemeData(map)
            }.getOrNull()
    }
}
