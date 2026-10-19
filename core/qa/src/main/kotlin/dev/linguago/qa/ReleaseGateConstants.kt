package dev.linguago.qa

/** Constantes y umbrales del RELEASE GATE F4 (ROADMAP_V2.md, Fase 4). */
internal const val TARGET_PACKAGE: String = "dev.linguago.app"
internal const val MAIN_ACTIVITY: String = "dev.linguago.app.MainActivity"

internal const val HOME_LABEL: String = "Inicio"
internal const val COURSE_TILE_LABEL: String = "Español → Inglés"
internal const val START_COURSE_LABEL: String = "Comenzar"
internal const val B2_UNIT_TITLE: String = "Negociación y decisiones"
internal const val EXIT_LABEL: String = "Salir"
internal const val CONTINUE_LABEL: String = "Continuar"
internal const val CHECK_LABEL: String = "Comprobar"
internal const val FINISH_LABEL: String = "Lección completada"
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
internal const val DICTATION_LABEL: String = "Tu dictado"

internal const val CORRECT_LABEL: String = "¡Correcto!"
internal const val REVIEW_LABEL: String = "Revisa la respuesta y sigue"

// Umbrales duros del gate.
// MAX_RAM_MB cubre el proceso COMPLETO con los modelos Piper (~200 MB) y Vosk
// residentes una vez que la sesión usó voz (peak visto en smoke: ~390-417 MB,
// nativo incluido). El criterio de FUGA se juzga con la curva por iteración
// del JSON y con el stress determinista de F4.1/ADR-022, no con este pico.
internal const val MAX_RAM_MB: Int = 480
internal const val MAX_FD_GROWTH: Int = 25

// El heap nativo crece de forma legítima cuando Piper/Vosk cargan modelos
// (~320 MB crecientes en el primer uso, idle 11 MB); el gate busca fugas
// SOSTENIDAS: el drift por iteración queda en el JSON, no este salto inicial.
internal const val MAX_NATIVE_HEAP_GROWTH_MB: Int = 400

internal const val DEFAULT_TIMEOUT_MILLIS: Long = 20_000L
internal const val EXERCISE_TIMEOUT_MILLIS: Long = 60_000L
internal const val LESSON_TIMEOUT_MILLIS: Long = 240_000L
internal const val STT_TIMEOUT_MILLIS: Long = 45_000L

// Parámetros de la sesión del gate.
internal const val FULL_TARGET_MINUTES: Int = 20
internal const val BACKGROUND_MINUTES: Long = 5L
internal const val OVERHEAD_MINUTES: Long = 12L
internal const val MAX_CONSECUTIVE_FAILURES: Int = 3
internal const val CRASH_LOOKAHEAD_LINES: Int = 6
internal const val MAX_LOG_LINE_CHARS: Int = 300
internal const val LOG_TAG: String = "ReleaseGate"
internal const val REPORT_FILE_NAME: String = "release-gate-f4-results.json"
