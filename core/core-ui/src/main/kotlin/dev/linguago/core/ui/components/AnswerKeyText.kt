package dev.linguago.core.ui.components

import dev.linguago.core.domain.logic.AnswerKey

/** Presentación legible de la respuesta canónica para el banner de feedback. */
public fun AnswerKey.displayText(): String =
    when (this) {
        is AnswerKey.Options -> text
        is AnswerKey.Tokens -> tokens.joinToString(" ")
        is AnswerKey.Pairs -> pairs.joinToString("\n") { (left, right) -> "$left → $right" }
        is AnswerKey.Text -> text
        is AnswerKey.Speaking -> text
        is AnswerKey.Cloze -> assignments.entries.joinToString("\n") { (blank, answer) -> "$blank: $answer" }
    }
