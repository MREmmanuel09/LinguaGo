package dev.linguago.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.domain.model.WordProgress
import dev.linguago.core.ui.components.AudioPlayerCard
import dev.linguago.core.ui.components.FlipCard3D
import dev.linguago.core.ui.components.HeartsCounter
import dev.linguago.core.ui.components.LinguaTopBar
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import java.time.Instant

/**
 * Repaso espaciado (ADR-016, ADR-034): flashcard 3D palabra→traducción con
 * calificación FSRS de 4 niveles. Invariante I3: no hay bloqueo; se puede
 * salir en cualquier momento.
 */
@Composable
public fun ReviewScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    ReviewContent(
        state = state,
        onReveal = viewModel::reveal,
        onSpeak = { viewModel.speakTranslation(slow = false) },
        onSpeakSlow = { viewModel.speakTranslation(slow = true) },
        onRate = viewModel::rate,
        onExit = onExit,
        modifier = modifier,
    )
}

@Composable
private fun ReviewContent(
    state: ReviewUiState,
    onReveal: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakSlow: () -> Unit,
    onRate: (ReviewRating) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current
    LaunchedEffect(state.revealed) {
        if (state.revealed) feedback.play(FeedbackCue.CARD_FLIP)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LinguaTopBar(
            title = "Repaso",
            onBack = onExit,
            actions = { HeartsCounter(hearts = state.hearts) },
        )

        when {
            state.loading ->
                Text(
                    text = "Cargando tarjetas…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )

            state.empty ->
                FinishPanel(
                    title = "Nada pendiente por hoy",
                    detail = "Completa lecciones para agregar vocabulario a tu repaso.",
                    onExit = onExit,
                )

            state.finished ->
                FinishPanel(
                    title = "¡Repaso completado!",
                    detail = "Vuelve mañana: FSRS te mostrará las tarjetas que toquen.",
                    onExit = onExit,
                )

            else -> {
                Text(
                    text = "Tarjeta ${state.index + 1} de ${state.total}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "Marcar «Otra vez» cuesta 1 vida. Sé honesto: el repaso se adapta a ti.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                if (state.heartProgress > 0) {
                    Text(
                        text = "${state.heartProgress}/5 tarjetas para recuperar 1 ❤",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                    )
                }

                state.current?.let { card ->
                    FlipCard3D(
                        revealed = state.revealed,
                        onFlip = onReveal,
                        modifier = Modifier.fillMaxWidth(),
                        front = {
                            Text(
                                text = "Palabra",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = card.word,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = "Toca la tarjeta para ver la traducción",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        back = {
                            Text(
                                text = "Traducción",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = card.translation,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(12.dp))
                            AudioPlayerCard(
                                title = "Pronunciación",
                                playing = state.speaking,
                                enabled = state.speechAvailable,
                                onPlay = {
                                    feedback.play(FeedbackCue.TAP)
                                    onSpeak()
                                },
                                onPlaySlow = {
                                    feedback.play(FeedbackCue.TAP)
                                    onSpeakSlow()
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            state.culturalNote?.let { note ->
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Nota cultural: $note",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                                            .background(colors.surfaceVariant)
                                            .padding(12.dp),
                                )
                            }
                        },
                    )
                }

                if (!state.revealed) {
                    PrimaryButton3D(
                        text = "Mostrar traducción",
                        onClick = onReveal,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = "¿Qué tal te salió?",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant,
                    )
                    RatingRow(onRate = onRate)
                }
            }
        }

        state.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.error,
            )
        }
    }
}

/** Fila (o cuadrícula 2×2 en fuente grande) de calificación FSRS (ADR-034/048). */
@Composable
private fun RatingRow(onRate: (ReviewRating) -> Unit) {
    val largeFont = LocalDensity.current.fontScale >= 1.5f
    val specs =
        listOf(
            RatingSpec("Otra vez", ReviewRating.AGAIN, LinguaTokens.Destructive, LinguaTokens.Destructive.copy(alpha = 0.72f), Color.White),
            RatingSpec("Difícil", ReviewRating.HARD, LinguaTokens.Warning, Color(LinguaTokens.CHEST_EDGE_HEX), LinguaTokens.Fg),
            RatingSpec("Bien", ReviewRating.GOOD, LinguaTokens.Accent, LinguaTokens.Accent.copy(alpha = 0.72f), Color.White),
            RatingSpec("Fácil", ReviewRating.EASY, LinguaTokens.Primary, LinguaTokens.PrimaryEdge, Color.White),
        )
    val rows = if (largeFont) specs.chunked(2) else listOf(specs)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { spec ->
                    RatingButton(
                        label = spec.label,
                        rating = spec.rating,
                        bodyColor = spec.bodyColor,
                        edgeColor = spec.edgeColor,
                        contentColor = spec.contentColor,
                        onRate = onRate,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private data class RatingSpec(
    val label: String,
    val rating: ReviewRating,
    val bodyColor: Color,
    val edgeColor: Color,
    val contentColor: Color,
)

@Composable
private fun RatingButton(
    label: String,
    rating: ReviewRating,
    bodyColor: Color,
    edgeColor: Color,
    contentColor: Color,
    onRate: (ReviewRating) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryButton3D(
        text = label,
        onClick = { onRate(rating) },
        modifier = modifier,
        bodyColor = bodyColor,
        edgeColor = edgeColor,
        contentColor = contentColor,
        compact = true,
    )
}

@Composable
private fun FinishPanel(
    title: String,
    detail: String,
    onExit: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(colors.surfaceVariant)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        PrimaryButton3D(
            text = "Volver",
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewScreenPreview() {
    LinguaTheme {
        ReviewContent(
            state =
                ReviewUiState(
                    loading = false,
                    total = 3,
                    index = 0,
                    current =
                        WordProgress(
                            word = "hermano",
                            translation = "brother",
                            courseId = "es-en",
                            reviewCount = 0,
                            lastReview = Instant.EPOCH,
                            nextReview = Instant.EPOCH,
                            stability = 3.7f,
                            difficulty = 5.2f,
                        ),
                ),
            onReveal = {},
            onSpeak = {},
            onSpeakSlow = {},
            onRate = {},
            onExit = {},
        )
    }
}
