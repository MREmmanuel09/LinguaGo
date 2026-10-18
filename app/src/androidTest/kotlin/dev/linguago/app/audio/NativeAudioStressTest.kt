package dev.linguago.app.audio

import android.Manifest
import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.linguago.app.BuildConfig
import dev.linguago.core.audio.PiperTtsEngine
import dev.linguago.core.audio.VoskSttEngine
import dev.linguago.core.domain.model.Language
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Stress nativo F4.1/ADR-022: 50 ciclos de apertura de lección, reproducción
 * TTS y STT cancelado a los 500 ms. Verifica que no se acumule Native Heap
 * (JNA Vosk + JNI sherpa-onnx) y que no haya crashes nativos. El baseline se
 * toma tras 10 ciclos de calentamiento para estabilizar las arenas nativas.
 */
@RunWith(AndroidJUnit4::class)
class NativeAudioStressTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun prepareDevice() {
        assumeTrue(
            "NativeAudioStressTest requiere los modelos del flavor en/full",
            BuildConfig.FLAVOR == "en" || BuildConfig.FLAVOR == "full",
        )
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.RECORD_AUDIO,
            )
        }
        assertTrue(
            "RECORD_AUDIO es obligatorio para el stress de Vosk",
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
        )
    }

    @Test
    fun cincuenta_ciclos_de_leccion_cancelan_stt_sin_fuga_nativa() =
        runBlocking {
            val tts = PiperTtsEngine(context)
            val stt = VoskSttEngine(context)
            val startedAt = System.currentTimeMillis()
            tts.warmUp(Language.ENGLISH)
            stt.warmUp(Language.ENGLISH)
            assertTrue("Piper debe estar listo (flavor en/full)", tts.isReadyFor(Language.ENGLISH))
            assertTrue("Vosk debe estar listo (flavor en/full)", stt.isReadyFor(Language.ENGLISH))

            try {
                repeat(WARMUP_CYCLES) { index -> lessonCycle(tts, stt, index, cancelTts = false) }
                val baseline = nativeHeapAfterGc()
                Log.i(TAG, "baseline nativo=${baseline / MEGABYTE} MB")

                repeat(STRESS_CYCLES) { index ->
                    lessonCycle(tts, stt, index, cancelTts = index % TTS_CANCEL_EVERY == 0)
                    if ((index + 1) % PROGRESS_EVERY == 0) {
                        Log.i(TAG, "ciclos=${index + 1} nativeHeap=${Debug.getNativeHeapAllocatedSize()}")
                    }
                }

                val grownBytes = nativeHeapAfterGc() - baseline
                Log.i(TAG, "delta nativo=${grownBytes / MEGABYTE} MB ($grownBytes bytes)")
                assertTrue(
                    "Native Heap creció ${grownBytes / MEGABYTE} MB (máximo ${MAX_NATIVE_GROWTH_MB} MB)",
                    grownBytes < MAX_NATIVE_GROWTH_MB * MEGABYTE,
                )
                assertNoNativeCrashes(startedAt)
            } finally {
                tts.release()
                stt.release()
            }
        }

    private suspend fun lessonCycle(
        tts: PiperTtsEngine,
        stt: VoskSttEngine,
        index: Int,
        cancelTts: Boolean,
    ) = coroutineScope {
        tts.warmUp(Language.ENGLISH)
        stt.warmUp(Language.ENGLISH)

        val speech =
            launch(start = CoroutineStart.UNDISPATCHED) {
                tts.speak(text = if (index % 2 == 0) "Hello" else "Good morning", language = Language.ENGLISH)
            }
        if (cancelTts) {
            delay(TTS_CANCEL_MILLIS)
            speech.cancelAndJoin()
        } else {
            speech.join()
        }

        val listening =
            launch(start = CoroutineStart.UNDISPATCHED) {
                stt.transcribe(Language.ENGLISH)
            }
        delay(STT_CANCEL_MILLIS)
        assertTrue("La captura STT terminó antes de poder cancelarse", listening.isActive)
        listening.cancelAndJoin()
    }

    private fun nativeHeapAfterGc(): Long {
        repeat(GC_ROUNDS) {
            Runtime.getRuntime().gc()
            System.runFinalization()
            Thread.sleep(GC_SETTLE_MILLIS)
        }
        return Debug.getNativeHeapAllocatedSize()
    }

    private fun assertNoNativeCrashes(startedAt: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val manager = context.getSystemService(ActivityManager::class.java)
        val crashes =
            manager
                .getHistoricalProcessExitReasons(context.packageName, 0, EXIT_REASONS_LIMIT)
                .filter { info ->
                    info.reason == ApplicationExitInfo.REASON_CRASH_NATIVE &&
                        info.timestamp >= startedAt
                }
        assertTrue("Crashes nativos detectados: $crashes", crashes.isEmpty())
    }

    private companion object {
        const val TAG = "NativeAudioStress"
        const val STRESS_CYCLES = 50
        const val WARMUP_CYCLES = 10
        const val TTS_CANCEL_EVERY = 5
        const val TTS_CANCEL_MILLIS = 250L
        const val STT_CANCEL_MILLIS = 500L
        const val GC_ROUNDS = 3
        const val GC_SETTLE_MILLIS = 100L
        const val PROGRESS_EVERY = 10
        const val EXIT_REASONS_LIMIT = 20
        const val MAX_NATIVE_GROWTH_MB = 15
        const val MEGABYTE = 1024L * 1024L
    }
}
