package dev.linguago.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.linguago.core.data.database.LinguagoDatabase
import dev.linguago.core.data.database.WordProgressEntity
import dev.linguago.core.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Actividad exclusiva de la variante `nonMinifiedRelease`: deja un estado
 * determinista para el generador de Baseline Profiles (curso activo y tarjetas
 * FSRS vencidas). Nunca se empaqueta en debug ni en release de distribución.
 */
@AndroidEntryPoint
class BenchmarkSeedActivity : ComponentActivity() {
    @Inject
    lateinit var database: LinguagoDatabase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            withContext(Dispatchers.IO) {
                seed()
            }
        }
        finishAndRemoveTask()
    }

    private suspend fun seed() {
        database.openHelper.writableDatabase.apply {
            execSQL("DELETE FROM lesson_progress")
            execSQL("DELETE FROM word_progress")
        }

        val now = System.currentTimeMillis()
        val dueCards =
            DUE_WORDS.map { (word, translation) ->
                WordProgressEntity(
                    word = word,
                    courseId = COURSE_ID,
                    translation = translation,
                    reviewCount = 1,
                    lastReviewEpochMillis = now - DAY_MILLIS,
                    nextReviewEpochMillis = now - SECOND_MILLIS,
                    stability = 2.5f,
                    difficulty = 5.0f,
                )
            }
        dueCards.forEach { card -> database.wordProgressDao().upsert(card) }
        settingsRepository.updateSelectedCourse(COURSE_ID)
    }

    private companion object {
        const val COURSE_ID = "es-en"
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
        const val SECOND_MILLIS = 1000L
        val DUE_WORDS =
            listOf(
                "deadline" to "fecha límite",
                "scope" to "alcance",
                "delay" to "retraso",
                "schedule" to "calendario",
                "budget" to "presupuesto",
                "contract" to "contrato",
                "meeting" to "reunión",
                "report" to "informe",
                "target" to "objetivo",
                "client" to "cliente",
                "proposal" to "propuesta",
                "agreement" to "acuerdo",
                "approval" to "aprobación",
                "deadline extension" to "prórroga",
                "stakeholder" to "interesado",
                "milestone" to "hito",
                "resource" to "recurso",
                "risk" to "riesgo",
                "estimate" to "estimación",
                "delivery" to "entrega",
            )
    }
}
