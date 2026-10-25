package dev.linguago.qa

/** Constantes y umbrales del RELEASE GATE F4 (ROADMAP_V2.md, Fase 4). */
internal const val TARGET_PACKAGE: String = "dev.linguago.app"
internal const val MAIN_ACTIVITY: String = "dev.linguago.app.MainActivity"

internal const val HOME_LABEL: String = "Inicio"
internal const val COURSE_TILE_LABEL: String = "Español → Inglés"
internal const val START_COURSE_LABEL: String = "Comenzar"
internal const val FIRST_UNIT_TITLE: String = "Primeros pasos"
internal const val EXIT_LABEL: String = "Salir"
internal const val CONTINUE_LABEL: String = "Continuar"
internal const val CHECK_LABEL: String = "Comprobar"
internal const val FINISH_LABEL: String = "¡Lección completada!"
internal const val BACK_TO_PATH_LABEL: String = "Volver al camino"

internal const val CLOZE_BLANK_PREFIX: String = "Hueco "
internal const val CLOZE_BLANK_EMPTY_SUFFIX: String = " vacío"
internal const val CRITERION_PREFIX: String = "Criterio "

internal const val SPEAK_PANEL_TITLE: String = "Escucha y repite"
internal const val SPEAK_HOLD_LABEL: String = "Sostén el botón para repetir"
internal const val SPEAK_SKIP_LABEL: String = "No puedo hablar ahora"
internal const val SPEAK_LISTENING_LABEL: String = "Escuchando…"

internal const val FREE_SPEAK_TITLE: String = "Producción oral"
internal const val FREE_SPEAK_RECORD: String = "Grabar respuesta"
internal const val FREE_SPEAK_STOP: String = "Detener grabación"
internal const val FREE_SPEAK_TRANSCRIPT: String = "Lo que entendimos"
internal const val FREE_SPEAK_SKIP: String = "No puedo hablar ahora"

// Al reabrir la lección a mitad del panel en revisión, el título y el prompt
// quedan fuera del viewport y Compose los excluye del árbol: los marcadores
// de abajo del panel son los que sobreviven.
internal const val FREE_SPEAK_LISTEN_MODEL: String = "Escuchar respuesta modelo"
internal const val FREE_SPEAK_SELF_ASSESSMENT: String = "Autoevaluación"

internal const val AUDIO_PLAY_LABEL: String = "Reproducir audio (toca aquí)"
internal const val AUDIO_SLOW_LABEL: String = "Reproducir más despacio (0.75x)"
internal const val AUDIO_ONLY_PROMPT: String = "Escucha y elige"

internal const val ORDER_WORDS_TITLE: String = "Ordena la frase"
internal const val SEMANTIC_MATCH_TITLE: String = "Equivalentes"
internal const val MATCH_PAIRS_TITLE: String = "Empareja las columnas"
internal const val DICTATION_LABEL: String = "Tu dictado"

internal const val CORRECT_LABEL: String = "¡Correcto!"
internal const val REVIEW_LABEL: String = "Revisa la respuesta y sigue"

// Umbrales duros del gate.
// MAX_RAM_MB es un techo CATASTRÓFICO: la base legítima del proceso incluye
// Piper + Vosk residentes (~360 MB de heap nativo medidos en emulador) más
// Compose y buffers (peak real ~483 MB). La FUGA se juzga con el drift
// nativo por lección (abajo) y con el stress determinista de F4.1/ADR-022.
internal const val MAX_RAM_MB: Int = 600
internal const val MAX_FD_GROWTH: Int = 25

// Crecimiento nativo tolerado POR LECCIÓN sobre la primera (modelos ya
// cargados): churn medido ~+10-16 MB/lección. Una fuga sostenida rompe esta
// pendiente aunque el pico absoluto quede bajo el techo.
internal const val MAX_NATIVE_GROWTH_PER_LESSON_MB: Int = 30

internal const val DEFAULT_TIMEOUT_MILLIS: Long = 20_000L
internal const val EXERCISE_TIMEOUT_MILLIS: Long = 60_000L
internal const val LESSON_TIMEOUT_MILLIS: Long = 240_000L
internal const val STT_TIMEOUT_MILLIS: Long = 45_000L

// Feedback de ejercicios sin voz: si el emparejamiento no se aceptó, no hay
// razón para esperar más de lo que tarda la animación del banner.
internal const val FEEDBACK_TIMEOUT_MILLIS: Long = 20_000L

// Parámetros de la sesión del gate.
internal const val FULL_TARGET_MINUTES: Int = 20
internal const val BACKGROUND_MINUTES: Long = 5L
internal const val OVERHEAD_MINUTES: Long = 12L
internal const val MAX_CONSECUTIVE_FAILURES: Int = 3

// Vidas (ADR-025): el driver falla a propósito para ejercitar el banner de
// error, pero con presupuesto acotado para no cancelar la lección. Si las
// vidas bajan del umbral, responde correcto. Cada lección reiniciada recibe
// dos fallos como máximo.
internal const val MAX_WRONG_ANSWERS_PER_LESSON: Int = 2
internal const val MIN_HEARTS_TO_ANSWER_WRONG: Int = 3
internal const val DEFAULT_HEARTS: Int = 10
internal const val CRASH_LOOKAHEAD_LINES: Int = 6
internal const val MAX_LOG_LINE_CHARS: Int = 300
internal const val LOG_TAG: String = "ReleaseGate"
internal const val REPORT_FILE_NAME: String = "release-gate-f4-results.json"
