package dev.linguago.baselineprofile

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

internal const val PACKAGE_NAME: String = "dev.linguago.app"
internal const val HOME_TAB_LABEL: String = "Inicio"
internal const val COURSE_TILE_LABEL: String = "Español → Inglés"
internal const val START_COURSE_LABEL: String = "Comenzar"
internal const val FIRST_LESSON_DESC_PREFIX: String = "Lección: Saludos."
internal const val FIRST_OPTION_LABEL: String = "Hello"
internal const val REVIEW_CARD_TITLE: String = "Repasar ahora"
internal const val REVIEW_SCREEN_TITLE: String = "Repaso"
internal const val SHOW_TRANSLATION_LABEL: String = "Mostrar traducción"
internal const val GOOD_RATING_LABEL: String = "Bien"
internal const val CONTINUE_LABEL: String = "Continuar"
internal const val EXIT_LESSON_LABEL: String = "Salir"
internal const val BRIEF_START_LABEL: String = "Empezar lección"
internal const val BRIEF_RESUME_LABEL: String = "Volver a la lección"

private const val DEFAULT_TIMEOUT_MILLIS: Long = 20_000L

internal fun seedBenchmarkData(uiDevice: UiDevice) {
    uiDevice.executeShellCommand("am force-stop $PACKAGE_NAME")
    val output =
        uiDevice.executeShellCommand(
            "am start -W -n $PACKAGE_NAME/dev.linguago.app.BenchmarkSeedActivity",
        )
    check(output.contains("Status: ok")) {
        "No se pudo sembrar el estado de benchmark: $output"
    }
    uiDevice.executeShellCommand("am force-stop $PACKAGE_NAME")
}

internal fun ensureCourseSelected(uiDevice: UiDevice) {
    uiDevice.executeShellCommand("am force-stop $PACKAGE_NAME")
    uiDevice.executeShellCommand(
        "am start -W -n $PACKAGE_NAME/dev.linguago.app.MainActivity",
    )

    if (uiDevice.wait(Until.hasObject(By.text(HOME_TAB_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        uiDevice.executeShellCommand("am force-stop $PACKAGE_NAME")
        return
    }

    check(uiDevice.wait(Until.hasObject(By.text(COURSE_TILE_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        "No apareció la selección de curso ni Home"
    }
    uiDevice.findObject(By.text(COURSE_TILE_LABEL)).click()
    check(uiDevice.wait(Until.hasObject(By.text(START_COURSE_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        "No apareció el botón para comenzar"
    }
    uiDevice.findObject(By.text(START_COURSE_LABEL)).click()
    check(uiDevice.wait(Until.hasObject(By.text(HOME_TAB_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        "No se llegó a Home tras seleccionar el curso"
    }
    uiDevice.executeShellCommand("am force-stop $PACKAGE_NAME")
}

internal fun awaitHome(uiDevice: UiDevice) {
    check(uiDevice.wait(Until.hasObject(By.text(HOME_TAB_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        "Home no quedó visible"
    }
}

internal fun scrollToTop(
    uiDevice: UiDevice,
    marker: String,
    maxSwipes: Int = 8,
): Boolean {
    repeat(maxSwipes) {
        if (uiDevice.hasObject(By.text(marker))) return true
        uiDevice.swipe(
            uiDevice.displayWidth / 2,
            (uiDevice.displayHeight * 0.30f).toInt(),
            uiDevice.displayWidth / 2,
            (uiDevice.displayHeight * 0.80f).toInt(),
            20,
        )
        uiDevice.waitForIdle()
    }
    return uiDevice.hasObject(By.text(marker))
}

/**
 * Abre el primer nodo del Camino (ADR-026). El perfil se genera con el
 * `BenchmarkSeedActivity`, que deja el progreso vacío: el primer nodo
 * disponible es "Saludos".
 */
internal fun openFirstLesson(uiDevice: UiDevice) {
    check(
        uiDevice.wait(
            Until.hasObject(By.descStartsWith(FIRST_LESSON_DESC_PREFIX)),
            DEFAULT_TIMEOUT_MILLIS,
        ),
    ) {
        "No se encontró el nodo de la primera lección en el camino"
    }
    val node = uiDevice.findObject(By.descStartsWith(FIRST_LESSON_DESC_PREFIX))
    check(node.isEnabled) { "El nodo de la primera lección está bloqueado" }
    node.click()
    check(uiDevice.wait(Until.hasObject(By.text(EXIT_LESSON_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        "No se abrió la lección desde el camino"
    }
}

/** Responde el primer ejercicio (MultipleChoice de "Saludos") y avanza. */
internal fun interactWithFirstExercise(uiDevice: UiDevice) {
    when {
        uiDevice.hasObject(By.text(BRIEF_START_LABEL)) -> tapText(uiDevice, BRIEF_START_LABEL)
        uiDevice.hasObject(By.text(BRIEF_RESUME_LABEL)) -> tapText(uiDevice, BRIEF_RESUME_LABEL)
    }
    tapText(uiDevice, FIRST_OPTION_LABEL)
    check(uiDevice.wait(Until.hasObject(By.text(CONTINUE_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        "No apareció el feedback del primer ejercicio"
    }
    tapText(uiDevice, CONTINUE_LABEL)
}

internal fun exitLesson(uiDevice: UiDevice) {
    tapText(uiDevice, EXIT_LESSON_LABEL)
    check(uiDevice.wait(Until.hasObject(By.text(HOME_TAB_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        "No se volvió a Home tras salir de la lección"
    }
}

internal fun openReview(uiDevice: UiDevice) {
    check(scrollToTop(uiDevice, REVIEW_CARD_TITLE)) {
        "No apareció la tarjeta de repaso en Home"
    }
    check(tapClickableAncestor(uiDevice, REVIEW_CARD_TITLE, REVIEW_SCREEN_TITLE)) {
        "No se abrió la pantalla de repaso"
    }
    tapText(uiDevice, SHOW_TRANSLATION_LABEL)
    check(uiDevice.wait(Until.hasObject(By.text(GOOD_RATING_LABEL)), DEFAULT_TIMEOUT_MILLIS)) {
        "No aparecieron las calificaciones FSRS"
    }
    tapText(uiDevice, GOOD_RATING_LABEL)
}

private fun tapClickableAncestor(
    uiDevice: UiDevice,
    text: String,
    expectedText: String,
): Boolean {
    repeat(3) {
        val label = uiDevice.findObject(By.text(text)) ?: return@repeat
        val target =
            generateSequence(label.parent) { node -> node.parent }
                .firstOrNull { node -> node.isClickable }
                ?: label
        target.click()
        if (uiDevice.wait(Until.hasObject(By.text(expectedText)), 5_000L)) return true
    }
    return false
}

private fun tapText(
    uiDevice: UiDevice,
    text: String,
) {
    check(uiDevice.wait(Until.hasObject(By.text(text)), DEFAULT_TIMEOUT_MILLIS)) {
        "No apareció el texto '$text'"
    }
    uiDevice.findObject(By.text(text)).click()
    uiDevice.waitForIdle()
}
