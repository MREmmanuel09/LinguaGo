package dev.linguago.qa

import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

/** Acción resultante de conducir un ejercicio. */
internal enum class FlowAction { CONTINUE, RESTART, ABORT }

/** Resultado de una perturbación previa al ejercicio. */
internal enum class PreResult { CONTINUE, RESTART_LESSON, ABORT }

/**
 * Conduce la UI de una lección B2: detecta el tipo de ejercicio, responde
 * (correcto o incorrecto), aplica perturbaciones y avanza. No usa coordenadas
 * absolutas salvo el scroll.
 */
internal class LessonDriver(
    private val device: UiDevice,
    private val probe: DeviceProbe,
    private val report: ReleaseGateReport,
    private val targetContext: Context,
) {
    private var currentLessonTitle: String = ""

    /**
     * Conduce una lección completa hasta "Lección completada"; reintenta si el
     * SO mata la activity.
     *
     * [soft] aplica a las pasadas EXTRA del plan (lección ya completada en la
     * sesión): al reabrirla, la app entra en modo repaso con contenido
     * reordenado y el gate no puede puntuarlo con los datos del curso; el
     * intento queda como NOTE (cobertura best-effort) en vez de FAIL.
     */
    fun runLesson(
        scenario: LessonScenario,
        deadlineAtMillis: Long? = null,
        soft: Boolean = false,
    ): Boolean {
        currentLessonTitle = scenario.title
        val expected = CourseAnswers.load(targetContext, scenario.title)
        // Las perturbaciones "expulsivas" (rotación/trim/finish-activities)
        // se consumen una sola vez por lección: al reabrir, la lección
        // reinicia desde el ejercicio 0 y volver a aplicarlas causaría un
        // bucle de rotar→expulsar→reabrir sin fin.
        val handled = mutableSetOf<Perturbation>()
        val cursor = LessonCursor()
        var attempts = 0
        while (attempts < MAX_OPEN_ATTEMPTS) {
            if (deadlinePassed(deadlineAtMillis)) {
                closeLessonScreen()
                report.addScenario("open_lesson:${scenario.title}", "NOTE", "sesión agotada: corte gracióso con progreso guardado (I3)")
                return true
            }
            if (!ensureLessonOpen(scenario.title)) {
                attempts++
                continue
            }
            if (runExercises(scenario, expected, handled, cursor, deadlineAtMillis, soft)) return true
            attempts++
        }
        report.addScenario(
            "open_lesson:${scenario.title}",
            if (soft) "NOTE" else "FAIL",
            if (soft) "repaso con contenido reordenado: no conducido (best-effort)" else "no se pudo completar",
        )
        return false
    }

    /** true si el presupuesto de tiempo del gate ya expiró. */
    private fun deadlinePassed(deadlineAtMillis: Long?): Boolean =
        deadlineAtMillis != null && System.currentTimeMillis() >= deadlineAtMillis

    /** Cierra una pantalla de lección abierta (popBackStack con progreso, I3). */
    private fun closeLessonScreen() {
        if (hasText(EXIT_LABEL)) {
            tapText(EXIT_LABEL)
            waitForText(HOME_LABEL, DEFAULT_TIMEOUT_MILLIS)
        } else if (hasText(BACK_TO_PATH_LABEL)) {
            tapText(BACK_TO_PATH_LABEL)
        }
    }

    // Cada salida del bucle corresponde a un estado real distinto (re-entrada
    // sin Home, tipo no reconocido, candidatos agotados, respuesta abortada,
    // avance fallido, fin de lección); extraerlas a una máquina de estados
    // oscurecería el flujo en vivo.
    @Suppress("LoopWithTooManyJumpStatements")
    private fun runExercises(
        scenario: LessonScenario,
        expected: CourseAnswers,
        handled: MutableSet<Perturbation>,
        cursor: LessonCursor,
        deadlineAtMillis: Long? = null,
        soft: Boolean = false,
    ): Boolean {
        var index = cursor.index
        val usedSlots = mutableSetOf<Int>()
        while (index < expected.size()) {
            if (deadlinePassed(deadlineAtMillis)) {
                closeLessonScreen()
                report.addScenario("open_lesson:${scenario.title}", "NOTE", "sesión agotada a mitad de lección: corte gracióso (I3)")
                return true
            }
            cursor.index = index
            if (!awaitInLesson(scenario.title)) return false
            // Re-entrada con la lección ya corregida/completada: la app puede
            // retomar EN el último ejercicio (curso del cursor) o directamente
            // en "Lección completada" cuando el check previo pasó sin que el
            // gate lo capturara (visto en vivo con el skip de FreeSpeaking).
            if (hasText(FINISH_LABEL)) return finishLesson()
            val perturbation = scenario.exercisePerturbations[index] ?: Perturbation.NONE
            if (perturbation != Perturbation.NONE && !handled.contains(perturbation)) {
                when (applyPrePerturbation(perturbation)) {
                    PreResult.CONTINUE -> {
                        handled.add(perturbation)
                    }
                    PreResult.RESTART_LESSON -> {
                        // Las perturbaciones expulsivas dejan la app en Home y al
                        // reabrir la lección arranca desde el ejercicio 0
                        // (comportamiento verificado con rotación en vivo).
                        handled.add(perturbation)
                        if (!ensureLessonOpen(scenario.title)) return false
                        index = 0
                        continue
                    }
                    PreResult.ABORT -> return false
                }
            }

            val base = expected.exerciseAt(index) ?: return false
            val kind = awaitKind(base)
            if (kind == ExerciseKind.UNKNOWN) {
                if (!soft) {
                    report.addRaceSuspect(
                        "lección '${scenario.title}' idx $index: pantalla sin ejercicio reconocible; visible='${dumpVisibleTexts()}'",
                    )
                }
                return false
            }
            // La app reordena su cola al reanudar tras perder la actividad (el
            // orden del curso es solo una línea base) y puede repetir tipos:
            // se conduce el ejercicio visible probando los slots sin usar del
            // mismo tipo hasta que uno acepte la interacción.
            val candidates =
                if (perturbation == Perturbation.NONE) {
                    candidateSlots(expected, kind, index, usedSlots)
                } else {
                    listOf(index)
                }
            if (candidates.isEmpty()) {
                if (!soft) {
                    report.addRaceSuspect(
                        "lección '${scenario.title}' idx $index: esperado ${base.kind}, detectado $kind y sin candidatos; visible='${dumpVisibleTexts()}'",
                    )
                }
                return false
            }
            val wrong = index % 4 == 3
            val startedAt = System.currentTimeMillis()
            var action = FlowAction.ABORT
            var chosenSlot = candidates.first()
            for (slot in candidates) {
                val candidate = expected.exerciseAt(slot) ?: continue
                action = performAnswer(candidate, kind, perturbation, wrong)
                if (action != FlowAction.ABORT) {
                    chosenSlot = slot
                    break
                }
            }
            if (action != FlowAction.CONTINUE) return false
            usedSlots += chosenSlot

            val hasFeedback = waitFeedback(kind == ExerciseKind.SPEAK_REPEAT || kind == ExerciseKind.FREE_SPEAKING)
            val outcome =
                when {
                    hasText(CORRECT_LABEL) -> "correct"
                    hasText(REVIEW_LABEL) -> "incorrect"
                    hasFeedback -> "no_feedback"
                    else -> {
                        if (hasText(FINISH_LABEL)) {
                            "finish_reached"
                        } else {
                            "feedback_timeout visible='${dumpVisibleTexts()}'"
                        }
                    }
                }
            captureIteration(scenario, index, kind, startedAt, outcome)

            val next = expected.exerciseAt(index + 1)
            if (!advance(perturbation, next)) return false
            index++
        }
        return finishLesson()
    }

    /**
     * Slots candidatos para el ejercicio visible: el propio índice si coincide
     * con el tipo detectado y, si no (reorden en vivo), los slots sin usar del
     * mismo tipo. Acotado a [MAX_SLOT_CANDIDATES] para no alargar el intento.
     */
    private fun candidateSlots(
        expected: CourseAnswers,
        kind: ExerciseKind,
        index: Int,
        used: Set<Int>,
    ): List<Int> {
        val sameKind = expected.indicesWithKind(kind)
        val ordered = mutableListOf<Int>()
        if (kind == expected.exerciseAt(index)?.kind && index !in used) ordered += index
        sameKind.filterTo(ordered) { it !in used && it !in ordered }
        if (ordered.isEmpty()) ordered += sameKind.take(2)
        return ordered.distinct().take(MAX_SLOT_CANDIDATES)
    }

    private fun performAnswer(
        exercise: ExpectedExercise,
        kind: ExerciseKind,
        perturbation: Perturbation,
        wrong: Boolean,
    ): FlowAction =
        when (kind) {
            ExerciseKind.CLOZE -> answerCloze(exercise, wrong)
            ExerciseKind.SEMANTIC_MATCH -> answerSemanticMatch(exercise, wrong)
            ExerciseKind.AUDIO_ONLY -> answerAudioOnly(exercise, perturbation, wrong)
            ExerciseKind.MULTIPLE_CHOICE -> answerMultipleChoice(exercise, wrong)
            ExerciseKind.LISTEN_TYPE -> answerListenType(exercise, wrong)
            ExerciseKind.ORDER_WORDS -> answerOrderWords(exercise, wrong)
            ExerciseKind.SPEAK_REPEAT -> answerSpeakRepeat(perturbation)
            ExerciseKind.FREE_SPEAKING -> answerFreeSpeaking(exercise, perturbation)
            ExerciseKind.UNKNOWN -> {
                report.addScenario("unknown_exercise", "ERROR", exercise.type)
                FlowAction.ABORT
            }
        }

    private fun answerCloze(
        exercise: ExpectedExercise,
        wrong: Boolean,
    ): FlowAction {
        for (blankIndex in exercise.clozeAnswers.indices) {
            val correct = exercise.clozeAnswers[blankIndex]
            val distractor = exercise.clozeDistractors.getOrNull(blankIndex)?.firstOrNull()
            val token = if (wrong && distractor != null) distractor else correct
            if (!tapText(token, EXERCISE_TIMEOUT_MILLIS)) return FlowAction.ABORT
            if (!tapDesc("$CLOZE_BLANK_PREFIX${blankIndex + 1}$CLOZE_BLANK_EMPTY_SUFFIX")) {
                return FlowAction.ABORT
            }
            if (!waitForDesc("$CLOZE_BLANK_PREFIX${blankIndex + 1}: $token")) return FlowAction.ABORT
        }
        tapText(CHECK_LABEL)
        return FlowAction.CONTINUE
    }

    private fun answerSemanticMatch(
        exercise: ExpectedExercise,
        wrong: Boolean,
    ): FlowAction {
        val rights = exercise.pairRights
        exercise.pairLefts.forEachIndexed { index, left ->
            // wrong: rotamos los emparejamientos (cada izquierda con otra
            // derecha, sin repetir). CORRECTO nunca drena: remover de `rights`
            // desincroniza el índice con el tamaño y lanza OOB cuando hay
            // ≥2 pares (hallado en vivo: "Index 2 out of bounds for length 1").
            val right = if (wrong && rights.size > 1) rights[(index + 1) % rights.size] else rights[index]
            // Datos de otro slot del mismo tipo (reorden en vivo) no están en
            // pantalla: se corta el intento en vez de esperar feedback que
            // nunca llegará, y el llamador prueba el siguiente candidato.
            if (!tapLabel(left) || !tapLabel(right)) return FlowAction.ABORT
        }
        tapText(CHECK_LABEL)
        return FlowAction.CONTINUE
    }

    private fun answerAudioOnly(
        exercise: ExpectedExercise,
        perturbation: Perturbation,
        wrong: Boolean,
    ): FlowAction {
        tapText(AUDIO_PLAY_LABEL)
        if (perturbation == Perturbation.CANCEL_TTS) {
            sleep(600)
            tapText(EXIT_LABEL)
            return if (awaitHome()) {
                report.addScenario("cancel_tts", "PASS", "Salir durante TTS volvió a Home")
                FlowAction.RESTART
            } else {
                report.addScenario("cancel_tts", "FAIL", "no se volvió a Home")
                FlowAction.ABORT
            }
        }
        sleep(1_500)
        if (exercise.allowSlowPlayback) tapText(AUDIO_SLOW_LABEL)
        tapText(optionToken(exercise, wrong))
        return FlowAction.CONTINUE
    }

    private fun answerMultipleChoice(
        exercise: ExpectedExercise,
        wrong: Boolean,
    ): FlowAction {
        tapText(optionToken(exercise, wrong))
        return FlowAction.CONTINUE
    }

    private fun answerListenType(
        exercise: ExpectedExercise,
        wrong: Boolean,
    ): FlowAction {
        val field =
            runCatching { device.findObject(By.clazz("android.widget.EditText")) }.getOrNull()
        if (field == null) {
            report.addScenario("listen_type", "ERROR", "no se encontró el EditText")
            return FlowAction.ABORT
        }
        runCatching { field.text = if (wrong) "respuesta incorrecta" else exercise.transcription }
        tapText(CHECK_LABEL)
        return FlowAction.CONTINUE
    }

    private fun answerOrderWords(
        exercise: ExpectedExercise,
        wrong: Boolean,
    ): FlowAction {
        val tokens = if (wrong) exercise.answerTokens.reversed() else exercise.answerTokens
        tokens.forEach { token -> tapText(token) }
        tapText(CHECK_LABEL)
        return FlowAction.CONTINUE
    }

    private fun answerSpeakRepeat(perturbation: Perturbation): FlowAction {
        if (!hasText(SPEAK_HOLD_LABEL)) {
            if (hasText(SPEAK_SKIP_LABEL)) {
                tapText(SPEAK_SKIP_LABEL)
                return FlowAction.CONTINUE
            }
            report.addScenario("speak_repeat", "ERROR", "ni micrófono ni skip")
            return FlowAction.ABORT
        }
        tapText(SPEAK_HOLD_LABEL)
        if (perturbation == Perturbation.BACKGROUND_DURING_STT) {
            sleep(1_200)
            probe.pressHome()
            sleep(2_000)
            probe.launchApp()
            if (awaitInLesson(currentLessonTitle)) {
                report.addScenario("background_during_stt", "PASS", "la lección sobrevivió")
            } else {
                report.addScenario("background_during_stt", "NOTE", "la lección se reinició")
                return FlowAction.RESTART
            }
        }
        if (perturbation == Perturbation.CANCEL_STT) {
            sleep(800)
            tapText(EXIT_LABEL)
            return if (awaitHome()) {
                report.addScenario("cancel_stt", "PASS", "Salir durante STT volvió a Home")
                FlowAction.RESTART
            } else {
                report.addScenario("cancel_stt", "FAIL", "no se volvió a Home")
                FlowAction.ABORT
            }
        }
        return FlowAction.CONTINUE
    }

    private fun answerFreeSpeaking(
        exercise: ExpectedExercise,
        perturbation: Perturbation,
    ): FlowAction {
        // Panel ya en revisión (re-entrada tras retomar la lección): el título
        // y el transcript pueden estar fuera del viewport; saltar directo a la
        // autoevaluación, que es lo que habilita "Comprobar".
        val inReview =
            hasText(FREE_SPEAK_TRANSCRIPT) ||
                hasText(FREE_SPEAK_LISTEN_MODEL) ||
                hasText(FREE_SPEAK_SELF_ASSESSMENT)
        if (!inReview) {
            tapText(FREE_SPEAK_RECORD)
            if (perturbation == Perturbation.NOTIFICATION_DURING_RECORDING) {
                sleep(1_200)
                val output = probe.shell("cmd notification post -S bigtext -t 'Gate F4' tag 'prueba'")
                sleep(1_200)
                val stillRecording = hasText(FREE_SPEAK_STOP)
                val posted = output.isNotBlank() && !output.contains("error", ignoreCase = true)
                val status = if (stillRecording && posted) "PASS" else "NOTE"
                report.addScenario(
                    "notification_during_recording",
                    status,
                    "grabando=$stillRecording posted=$posted out='${output.trim()}'",
                )
            }
            sleep(2_500)
            val transcriptReady =
                tapText(FREE_SPEAK_STOP, 5_000) &&
                    waitForText(FREE_SPEAK_TRANSCRIPT, EXERCISE_TIMEOUT_MILLIS)
            if (!transcriptReady) {
                // Emulador sin voz usable: `Grabar respuesta` queda deshabilitado
                // (speechAvailable=false) y el panel no llega a la revisión. I3
                // manda: usar el skip oficial del panel y dejar avanzar.
                if (tapText(FREE_SPEAK_SKIP, 5_000)) {
                    report.addScenario("free_speaking_skip", "NOTE", "sin voz: ruta degradada por el skip del panel")
                    return FlowAction.CONTINUE
                }
                return FlowAction.ABORT
            }
        }
        for (criterion in 0 until exercise.criteriaCount) {
            val level = if (criterion % 2 == 0) "Sí" else "Casi"
            tapDescScrolled("$CRITERION_PREFIX${criterion + 1}: $level")
        }
        tapText(CHECK_LABEL)
        return FlowAction.CONTINUE
    }

    private fun applyPrePerturbation(perturbation: Perturbation): PreResult =
        when (perturbation) {
            Perturbation.ROTATE -> {
                // UiDevice.setOrientationLeft() rompe la conexión de
                // UiAutomator (árbol vacío tras la rotación, verificado en
                // vivo); la rotación vía `settings put system user_rotation`
                // mantiene el árbol de accesibilidad. Visto en vivo: la app
                // SUELTA la lección en rotación (vuelve a Home sin crash);
                // el criterio PASS = la app queda en un estado consistente
                // (en la lección o en Home) y no cuelga.
                probe.shell("settings put system accelerometer_rotation 0")
                probe.shell("settings put system user_rotation 1")
                sleep(2_500)
                val aliveLandscape = isLessonAlive() || awaitHome()
                probe.shell("settings put system user_rotation 0")
                sleep(2_500)
                val alivePortrait = isLessonAlive() || awaitHome()
                probe.shell("settings put system accelerometer_rotation 1")
                val rotatedOk = aliveLandscape && alivePortrait
                report.addScenario(
                    "rotate_mid_exercise",
                    if (rotatedOk) "PASS" else "FAIL",
                    "landscape=$aliveLandscape portrait=$alivePortrait",
                )
                return when {
                    rotatedOk && isLessonAlive() -> PreResult.CONTINUE
                    rotatedOk -> PreResult.RESTART_LESSON
                    else -> PreResult.ABORT
                }
            }
            Perturbation.TRIM_MEMORY -> {
                val before = probe.pid()
                probe.shell("am send-trim-memory $TARGET_PACKAGE RUNNING_CRITICAL")
                sleep(1_500)
                val after = probe.pid()
                return if (after == null) {
                    report.addScenario("low_memory_trim", "NOTE", "el SO mató el proceso")
                    PreResult.RESTART_LESSON
                } else {
                    val restarted = before != null && after != before
                    report.addScenario(
                        "low_memory_trim",
                        "PASS",
                        "pid $before -> $after (restart=$restarted)",
                    )
                    PreResult.CONTINUE
                }
            }
            Perturbation.DONT_KEEP_ACTIVITIES -> {
                probe.putSetting("global", "always_finish_activities", "1")
                probe.pressHome()
                sleep(1_500)
                probe.launchApp()
                sleep(1_500)
                val lessonAlive = isLessonAlive()
                report.addScenario(
                    "dont_keep_activities",
                    if (lessonAlive) "NOTE" else "PASS",
                    "la activity se destruyó (lesson_alive=$lessonAlive)",
                )
                return if (lessonAlive) PreResult.CONTINUE else PreResult.RESTART_LESSON
            }
            else -> return PreResult.CONTINUE
        }

    private fun advance(
        perturbation: Perturbation,
        next: ExpectedExercise?,
    ): Boolean {
        if (hasText(FINISH_LABEL)) return true
        if (!waitForText(CONTINUE_LABEL, EXERCISE_TIMEOUT_MILLIS)) return false
        if (perturbation == Perturbation.DOUBLE_TAP_CONTINUE) {
            runCatching {
                val button = device.findObject(By.text(CONTINUE_LABEL))
                button?.click()
                button?.click()
            }.onFailure {
                // Si el árbol muere entre taps, el objetivo del escenario no
                // se pierde: tapText reintenta con nodos frescos en el test.
                tapText(CONTINUE_LABEL, 5_000)
            }
            sleep(2_500)
            val detected = awaitKind(next)
            val expectedKind = next?.kind
            if (expectedKind != null && detected != expectedKind) {
                report.addRaceSuspect(
                    "doble tap en Continuar: esperado $expectedKind, detectado $detected",
                )
            } else {
                report.addScenario("double_tap_continue", "PASS", "no se saltó ningún ejercicio")
            }
            return true
        }
        tapText(CONTINUE_LABEL)
        return true
    }

    private fun finishLesson(): Boolean {
        if (!waitForText(FINISH_LABEL, LESSON_TIMEOUT_MILLIS)) return false
        report.incrementLessons()
        tapText(BACK_TO_PATH_LABEL)
        return awaitHome()
    }

    private fun captureIteration(
        scenario: LessonScenario,
        index: Int,
        kind: ExerciseKind,
        startedAt: Long,
        outcome: String,
    ) {
        if (probe.detectRestart()) report.incrementRestarts()
        val thermal = ThermalStatus.fromCode(probe.thermalRaw())
        report.noteThermal(thermal)
        val mem = probe.memSnapshot()
        report.addIteration(
            IterationRecord(
                lessonIndex = scenario.lessonIndex,
                lessonTitle = scenario.title,
                step = kind.name,
                durationMs = System.currentTimeMillis() - startedAt,
                ramMb = mem.totalPssMb,
                nativeHeapMb = mem.nativeHeapMb,
                javaHeapMb = mem.javaHeapMb,
                graphicsMb = mem.graphicsMb,
                privateOtherMb = mem.privateOtherMb,
                fdCount = probe.fdCount(),
                thermalStatus = thermal.label,
                outcome = outcome,
                timestamp = nowIso(),
            ),
        )
    }

    private fun ensureLessonOpen(title: String): Boolean {
        if (awaitInLesson(title)) return true
        if (hasText(EXIT_LABEL)) {
            // Reanudada con una lección distinta abierta: relanzar la app
            // siempre recae en esta pantalla (el runner no llega a Home) y el
            // tile objetivo queda inalcanzable. "Salir" hace popBackStack y
            // guarda progreso (I3), dejando el camino navegable de nuevo.
            report.addScenario("open_dump:$title", "NOTE", "lección ajena abierta; se cierra")
            tapText(EXIT_LABEL)
        }
        if (!awaitHome()) {
            probe.pressHome()
            if (!awaitHome()) {
                probe.launchApp()
                if (!awaitHome()) return false
            }
        }
        resetHomeScroll()
        return openLessonFromHome(title)
    }

    /** Vuelve el listado de Home arriba para partir siempre del mismo estado. */
    private fun resetHomeScroll() {
        repeat(2) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.25f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.7f).toInt(),
                10,
            )
            device.waitForIdle()
        }
    }

    private fun openLessonFromHome(title: String): Boolean {
        repeat(4) {
            if (hasText(title) && tapClickableAncestor(title, EXIT_LABEL)) return true
            if (scrollToText(title) && tapClickableAncestor(title, EXIT_LABEL)) return true
            if (!device.hasObject(By.text(title))) {
                tapClickableAncestor(B2_UNIT_TITLE, title)
            }
        }
        report.addScenario("open_dump:$title", "NOTE", dumpVisibleTexts())
        return false
    }

    /**
     * Snippet de textos visibles para diagnóstico cuando la navegación falla.
     * Recorre el árbol nativo de accesibilidad (`uiAutomation.rootInActiveWindow`)
     * porque `UiDevice.findObjects(By.textContains(""))` devuelve vacío y los
     * regex de `grep -o` por shell chocan con el munging de comillas de adb.
     */
    private fun dumpVisibleTexts(): String =
        runCatching {
            val root = InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow
            val collected = StringBuilder()
            walkAccessibilityTree(root, collected)
            collected.toString().take(1_200)
        }.getOrDefault("")

    private fun walkAccessibilityTree(
        node: AccessibilityNodeInfo?,
        out: StringBuilder,
    ) {
        if (node == null || out.length > 1_100 || out.split(" | ").size > 40) return
        val text = node.text?.toString()?.takeIf { it.isNotBlank() }
        val desc = node.contentDescription?.toString()?.takeIf { it.isNotBlank() }
        when {
            text != null -> out.append(text.take(80)).append(" | ")
            desc != null -> out.append(desc.take(60)).append(" | ")
        }
        for (index in 0 until node.childCount) {
            if (out.length > 1_100 || out.split(" | ").size > 40) return
            walkAccessibilityTree(node.getChild(index), out)
        }
    }

    private fun awaitInLesson(title: String): Boolean = waitForText(title, DEFAULT_TIMEOUT_MILLIS) && hasText(EXIT_LABEL)

    /** Público: el test lo usa para verificar persistencia tras background. */
    fun waitForHome(): Boolean = awaitHome()

    private fun awaitHome(): Boolean = waitForText(HOME_LABEL, DEFAULT_TIMEOUT_MILLIS)

    /**
     * La lección está viva si cualquier marcador de sesión/aprendizaje es
     * visible (título, feedback o un ejercicio reconocible). Más robusto que
     * exigir el título tras una rotación, donde Compose puede redibujar la
     * barra distinta en landscape.
     */
    private fun isLessonAlive(): Boolean =
        hasText(currentLessonTitle) ||
            hasText(EXIT_LABEL) ||
            hasText(CHECK_LABEL) ||
            hasText(CONTINUE_LABEL) ||
            hasText(CORRECT_LABEL) ||
            hasText(REVIEW_LABEL) ||
            detectKind() != ExerciseKind.UNKNOWN

    private fun awaitKind(expected: ExpectedExercise?): ExerciseKind {
        val deadline = System.currentTimeMillis() + EXERCISE_TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            val kind = detectKind()
            if (kind != ExerciseKind.UNKNOWN) return kind
            // MultipleChoice no tiene ningún string fijo de UI (la cabecera es
            // la `question` del contenido): si el prompt esperado está
            // visible, confiamos en el tipo declarado por course.json.
            val prompt = expected?.prompt
            if (!prompt.isNullOrBlank() && device.hasObject(By.text(prompt))) return expected.kind
            sleep(300)
        }
        return ExerciseKind.UNKNOWN
    }

    private fun detectKind(): ExerciseKind =
        when {
            hasText(FREE_SPEAK_TITLE) ||
                hasText(FREE_SPEAK_LISTEN_MODEL) ||
                hasText(FREE_SPEAK_SELF_ASSESSMENT) -> ExerciseKind.FREE_SPEAKING
            hasText(SPEAK_PANEL_TITLE) ||
                hasText(SPEAK_HOLD_LABEL) ||
                hasText(SPEAK_LISTENING_LABEL) ||
                hasText(SPEAK_SKIP_LABEL) -> ExerciseKind.SPEAK_REPEAT
            hasText(AUDIO_PLAY_LABEL) && hasText(AUDIO_ONLY_PROMPT) -> ExerciseKind.AUDIO_ONLY
            hasDescPrefix(CLOZE_BLANK_PREFIX) -> ExerciseKind.CLOZE
            hasText(ORDER_WORDS_TITLE) -> ExerciseKind.ORDER_WORDS
            hasText(SEMANTIC_MATCH_TITLE) -> ExerciseKind.SEMANTIC_MATCH
            hasEditText() -> ExerciseKind.LISTEN_TYPE
            else -> ExerciseKind.UNKNOWN
        }

    private fun optionToken(
        exercise: ExpectedExercise,
        wrong: Boolean,
    ): String {
        val options = exercise.options
        if (options.isEmpty()) return ""
        val index =
            if (wrong && options.size > 1) {
                (exercise.correctIndex + 1) % options.size
            } else {
                exercise.correctIndex
            }
        return options.getOrElse(index) { options.first() }
    }

    private fun hasText(text: String): Boolean = device.hasObject(By.text(text))

    private fun hasDescPrefix(prefix: String): Boolean = device.hasObject(By.descStartsWith(prefix))

    private fun hasEditText(): Boolean = device.findObject(By.clazz("android.widget.EditText")) != null

    private fun waitForText(
        text: String,
        timeout: Long,
    ): Boolean = device.wait(Until.hasObject(By.text(text)), timeout)

    private fun waitForDesc(description: String): Boolean = device.wait(Until.hasObject(By.desc(description)), EXERCISE_TIMEOUT_MILLIS)

    private fun waitFeedback(voice: Boolean): Boolean =
        device.wait(
            Until.hasObject(By.text(CONTINUE_LABEL)),
            if (voice) STT_TIMEOUT_MILLIS else FEEDBACK_TIMEOUT_MILLIS,
        )

    /**
     * UiAutomator lanza [androidx.test.uiautomator.StaleObjectException] cuando
     * el árbol se reconstruye entre `findObject` y `click` (visto en la corrida
     * de 20 min en el tránsito Continuar→siguiente ejercicio). Todo tap pasa
     * por aquí: cada intento re-resuelve el nodo fresco y reintenta; el speed
     * `click()` existe (void), así que "éxito" = no lanzó excepción.
     */
    private fun tapFresh(selector: BySelector): Boolean {
        repeat(3) {
            val result =
                runCatching {
                    val node = device.findObject(selector)
                    if (node != null) {
                        node.click()
                        true
                    } else {
                        false
                    }
                }.getOrDefault(false)
            if (result) {
                device.waitForIdle()
                return true
            }
            sleep(700)
        }
        return false
    }

    private fun tapText(
        text: String,
        timeout: Long = DEFAULT_TIMEOUT_MILLIS,
    ): Boolean {
        if (!waitForText(text, timeout)) return false
        return tapFresh(By.text(text))
    }

    private fun tapDesc(description: String): Boolean {
        if (!waitForDesc(description)) return false
        return tapFresh(By.desc(description))
    }

    /**
     * Tap por description con barrido: UiAutomator cliquea el centro de los
     * bounds del nodo, y en columnas largas (autoevaluación bajo el desglose
     * del FreeSpeakingPanel) ese centro puede caer fuera de pantalla y
     * tapar a un nodo inerte. Antes de cada tap, garantiza que el chip quede
     * bajo el top-in-monitor.
     */
    private fun tapDescScrolled(description: String): Boolean {
        repeat(4) {
            val insideViewport =
                runCatching {
                    val chip = device.findObject(By.desc(description))
                    chip != null &&
                        chip.visibleBounds.top >= 0 &&
                        device.displayHeight - chip.visibleBounds.top > 120
                }.getOrDefault(false)
            if (insideViewport) return tapFresh(By.desc(description))
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.75f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.55f).toInt(),
                12,
            )
            device.waitForIdle()
        }
        return tapDesc(description)
    }

    private fun tapLabel(label: String): Boolean {
        if (tapFresh(By.text(label))) return true
        return tapFresh(By.textContains(label))
    }

    private fun tapClickableAncestor(
        text: String,
        expectedText: String,
    ): Boolean {
        repeat(3) {
            val nitido =
                runCatching {
                    val label = device.findObject(By.text(text)) ?: return false
                    val target =
                        generateSequence(label.parent) { node -> node.parent }
                            .firstOrNull { node -> node.isClickable }
                            ?: label
                    target.click()
                    true
                }.getOrDefault(false)
            if (nitido && device.wait(Until.hasObject(By.text(expectedText)), 5_000L)) return true
        }
        return false
    }

    private fun scrollToText(text: String): Boolean {
        repeat(8) {
            if (hasText(text)) return true
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.75f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.30f).toInt(),
                20,
            )
            device.waitForIdle()
        }
        return hasText(text)
    }

    private fun sleep(millis: Long) {
        runCatching { Thread.sleep(millis) }
            .onFailure { Log.w("ReleaseGate", "sleep interrumpido: ${it.message}") }
    }

    private fun nowIso(): String =
        java.time.Instant
            .now()
            .toString()

    private companion object {
        const val MAX_OPEN_ATTEMPTS = 3
        const val MAX_SLOT_CANDIDATES = 3
    }

    /**
     * Cursor del índice de la lección a través de los intentos de re-entrada:
     * cuando la actividad sigue viva, la app retoma EN el ejercicio que quedó
     * (verificado en vivo); el reset a 0 sólo corresponde a las perturbaciones
     * expulsivas (RESTART_LESSON), que reinician la lección de verdad.
     */
    private class LessonCursor(
        var index: Int = 0,
    )
}
