package dev.linguago.app

/** Rutas de navegación (Fase 1): lesson lleva argumentos curso/lección. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val LESSON = "lesson/{courseId}/{lessonId}"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val SYNC = "sync"
    const val REVIEW = "review?courseId={courseId}"

    /** Ruta base de la pestaña Repaso (sin argumento: usa el curso activo). */
    const val REVIEW_TAB = "review"

    /** Examen de nivelación por curso/idioma (ADR-058). */
    const val PLACEMENT = "placement/{courseId}"

    /** Construye la ruta parametrizada de lección. */
    fun lesson(
        courseId: String,
        lessonId: String,
    ): String = "lesson/$courseId/$lessonId"

    /** Construye la ruta del repaso espaciado para el curso dado. */
    fun review(courseId: String): String = "review?courseId=$courseId"

    /** Construye la ruta del examen de nivelación para el curso dado. */
    fun placement(courseId: String): String = "placement/$courseId"
}
