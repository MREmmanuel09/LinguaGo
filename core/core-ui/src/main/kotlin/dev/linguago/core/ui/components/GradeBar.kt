package dev.linguago.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.ui.theme.LinguaTokens

/** Especificación de color de un botón de calificación (ADR-055). */
private data class GradeSpec(
    val label: String,
    val rating: ReviewRating,
    val body: Color,
    val edge: Color,
    val content: Color,
)

/**
 * Barra de calificación FSRS del prototipo «Warm Cream & Teal» (ADR-055):
 * Otra vez (coral), Difícil (ámbar), Bien (sage) y Fácil (teal), con la
 * física 3D compartida y tintas AA por botón. En fuente grande se parte en
 * 2×2 para no romper el objetivo táctil (I8). La lógica FSRS vive en el
 * dominio; aquí solo se califica.
 */
@Composable
public fun GradeBar(
    onRate: (ReviewRating) -> Unit,
    modifier: Modifier = Modifier,
) {
    val specs =
        listOf(
            GradeSpec("Otra vez", ReviewRating.AGAIN, LinguaTokens.Destructive, LinguaTokens.DestructiveInk, LinguaTokens.DestructiveInk),
            GradeSpec("Difícil", ReviewRating.HARD, LinguaTokens.Warning, LinguaTokens.WarningInk, LinguaTokens.WarningInk),
            GradeSpec("Bien", ReviewRating.GOOD, LinguaTokens.Accent, LinguaTokens.AccentInk, LinguaTokens.AccentInk),
            GradeSpec("Fácil", ReviewRating.EASY, LinguaTokens.Primary, LinguaTokens.PrimaryEdge, LinguaTokens.Bg),
        )
    val largeFont = LocalDensity.current.fontScale >= 1.5f
    val rows = if (largeFont) specs.chunked(2) else listOf(specs)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { spec ->
                    PrimaryButton3D(
                        text = spec.label,
                        onClick = { onRate(spec.rating) },
                        modifier = Modifier.weight(1f),
                        bodyColor = spec.body,
                        edgeColor = spec.edge,
                        contentColor = spec.content,
                        compact = true,
                    )
                }
            }
        }
    }
}
