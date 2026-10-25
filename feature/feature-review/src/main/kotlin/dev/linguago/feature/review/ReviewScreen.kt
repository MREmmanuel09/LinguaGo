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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.domain.model.WordProgress
import dev.linguago.core.ui.components.HeartsIndicator
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import java.time.Instant

/**
 * Repaso espaciado (ADR-016): tarjeta palabra→traducción con calificación
 * FSRS de 4 niveles. Invariante I3: no hay vidas ni penalización; se puede
 * salir en cualquier momento.
 */
@Composable
public fun ReviewScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewContent(
        state = state,
        onReveal = viewModel::reveal,
        onRate = viewModel::rate,
        onExit = onExit,
        modifier = modifier,
    )
}

@Composable
private fun ReviewContent(
    state: ReviewUiState,
    onReveal: () -> Unit,
    onRate: (ReviewRating) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Repaso",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground,
                modifier = Modifier.weight(1f),
            )
            HeartsIndicator(hearts = state.hearts)
        }

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

                state.current?.let { card ->
                    Text(
                        text = card.word,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onBackground,
                    )
                }

                when (state.revealed) {
                    false ->
                        PrimaryButton3D(
                            text = "Mostrar traducción",
                            onClick = onReveal,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    true -> {
                        state.current?.let { card ->
                            Text(
                                text = card.translation,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.primary,
                            )
                        }
                        RatingButton("Otra vez", ReviewRating.AGAIN, onRate)
                        RatingButton("Difícil", ReviewRating.HARD, onRate)
                        RatingButton("Bien", ReviewRating.GOOD, onRate)
                        RatingButton("Fácil", ReviewRating.EASY, onRate)
                    }
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

@Composable
private fun RatingButton(
    label: String,
    rating: ReviewRating,
    onRate: (ReviewRating) -> Unit,
) {
    PrimaryButton3D(
        text = label,
        onClick = { onRate(rating) },
        modifier = Modifier.fillMaxWidth(),
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
            onRate = {},
            onExit = {},
        )
    }
}
