package dev.linguago.core.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.linguago.core.domain.logic.LessonGlossary

/**
 * Texto con glosario por palabra (ADR-040): mantener pulsada una palabra
 * muestra una burbuja con una o varias traducciones. Para TalkBack expone la
 * acción "Ver traducciones", que abre la lista de palabras traducibles del
 * texto. Las palabras sin entrada en el glosario no muestran burbuja.
 */
@Composable
public fun WordGlossaryText(
    text: String,
    glossary: Map<String, List<String>>,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    var layout by remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    var tooltip by remember(text) { mutableStateOf<WordTooltipState?>(null) }
    var showDialog by remember(text) { mutableStateOf(false) }
    val entries = remember(text, glossary) { glossaryEntriesIn(text, glossary) }
    val spans = remember(text) { wordSpans(text) }

    Box(modifier = modifier) {
        Text(
            text = text,
            style = style,
            color = color,
            onTextLayout = { result -> layout = result },
            modifier =
                Modifier
                    .pointerInput(text, glossary) {
                        detectTapGestures(
                            onLongPress = { position ->
                                val result = layout ?: return@detectTapGestures
                                val offset = result.getOffsetForPosition(position)
                                val index = spans.indexOfFirst { span -> offset in span.start..span.end }
                                if (index < 0) return@detectTapGestures
                                val match =
                                    phraseMatch(spans, index, glossary)
                                        ?: singleWordMatch(spans[index].text, glossary)
                                if (match != null) {
                                    val box = result.getBoundingBox(offset)
                                    tooltip =
                                        WordTooltipState(
                                            term = match.first,
                                            translations = match.second,
                                            x = box.left.toInt(),
                                            y = box.top.toInt(),
                                            width = box.width.toInt(),
                                        )
                                }
                            },
                        )
                    }.semantics {
                        if (entries.isNotEmpty()) {
                            customActions =
                                listOf(
                                    CustomAccessibilityAction("Ver traducciones") {
                                        showDialog = true
                                        true
                                    },
                                )
                        }
                    },
        )
        tooltip?.let { state ->
            TranslationTooltip(
                translations = state.translations,
                term = state.term,
                visible = true,
                onDismiss = { tooltip = null },
                anchorOffset = IntOffset(state.x, state.y),
                anchorWidth = state.width,
                dismissible = true,
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Traducciones") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    entries.forEach { (word, translations) ->
                        Text(
                            text = "$word: ${translations.joinToString(" · ")}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text(text = "Cerrar") }
            },
            modifier = Modifier.padding(8.dp),
        )
    }
}

private data class WordTooltipState(
    val term: String,
    val translations: List<String>,
    val x: Int,
    val y: Int,
    val width: Int,
)

/** Palabras del texto (letras, dígitos o apóstrofo) con su rango de índices. */
private data class WordSpan(
    val text: String,
    val start: Int,
    val end: Int,
)

private fun wordSpans(text: String): List<WordSpan> =
    WORD_PATTERN
        .findAll(text)
        .map { match ->
            WordSpan(text = match.value, start = match.range.first, end = match.range.last)
        }.toList()

private val WORD_PATTERN = Regex("[\\p{L}\\p{N}']+")
private const val MAX_PHRASE_WORDS = 8

/** Modismo o colocación: ventana de 2..4 palabras que contenga la pulsada. */
private fun phraseMatch(
    spans: List<WordSpan>,
    index: Int,
    glossary: Map<String, List<String>>,
): Pair<String, List<String>>? {
    for (length in MAX_PHRASE_WORDS downTo 2) {
        for (start in (index - length + 1)..index) {
            val end = start + length
            val phrase =
                if (start < 0 || end > spans.size) {
                    null
                } else {
                    spans.subList(start, end).joinToString(" ") { span -> span.text }
                }
            val translations =
                phrase?.let { candidate -> LessonGlossary.lookupExact(glossary, candidate) }
            if (phrase != null && translations != null) return phrase to translations
        }
    }
    return null
}

private fun singleWordMatch(
    word: String,
    glossary: Map<String, List<String>>,
): Pair<String, List<String>>? = LessonGlossary.lookup(glossary, word)?.let { translations -> word to translations }

/** Palabras y modismos del texto que tienen traducción, en orden de aparición. */
private fun glossaryEntriesIn(
    text: String,
    glossary: Map<String, List<String>>,
): List<Pair<String, List<String>>> {
    val spans = wordSpans(text)
    val normalizedText = LessonGlossary.normalize(text)
    val seen = LinkedHashSet<String>()
    val result = mutableListOf<Pair<String, List<String>>>()
    for ((key, translations) in glossary) {
        if (key.contains(' ') && normalizedText.contains(key) && seen.add(key)) {
            result += key to translations
        }
    }
    for (span in spans) {
        val key = LessonGlossary.normalize(span.text)
        val translations =
            if (key.isEmpty() || !seen.add(key)) {
                null
            } else {
                LessonGlossary.lookup(glossary, span.text)
            }
        if (translations != null) {
            result += span.text to translations
        }
    }
    return result
}
