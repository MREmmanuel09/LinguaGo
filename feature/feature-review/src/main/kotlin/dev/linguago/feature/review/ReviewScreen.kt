package dev.linguago.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.linguago.core.domain.logic.ReviewRating
import dev.linguago.core.domain.model.WordProgress
import dev.linguago.core.ui.components.AudioPlayerCard
import dev.linguago.core.ui.components.GradeBar
import dev.linguago.core.ui.components.HeartsCounter
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.components.ReviewDeckStack
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors
import java.time.Instant

/**
 * Repaso espaciado (ADR-016, ADR-034) con el prototipo «Warm Cream & Teal»
 * (ADR-055): baraja de tarjetas, onda bicolor y calificación FSRS de 4
 * niveles con la barra coral/ámbar/sage/teal. Invariante I3: no hay bloqueo;
 * se puede salir en cualquier momento. La lógica FSRS vive en el dominio.
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
        onSpeak = { viewModel.speakTranslation() },
        onSpeakAtSpeed = { speed -> viewModel.speakTranslation(speed) },
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
    onSpeakAtSpeed: (Float) -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BackButton(onClick = onExit)
            Spacer(Modifier.weight(1f))
            if (state.total > 0) {
                DeckCounterPill(text = "Tarjeta ${state.index + 1} de ${state.total}")
            }
            Spacer(Modifier.weight(1f))
            HeartsCounter(hearts = state.hearts)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Repaso",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.onBackground,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
            )
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
                state.current?.let { card ->
                    ReviewDeckStack(modifier = Modifier.fillMaxWidth()) {
                        if (state.revealed) {
                            DeckBadge(text = "Traducción")
                        } else {
                            DeckBadge(text = "Palabra")
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = card.word,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (state.revealed) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = card.translation,
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.onBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(12.dp))
                            AudioPlayerCard(
                                title = "",
                                playing = state.speaking,
                                enabled = state.speechAvailable,
                                onPlay = {
                                    feedback.play(FeedbackCue.TAP)
                                    onSpeak()
                                },
                                onPlayAtSpeed = { speed ->
                                    feedback.play(FeedbackCue.TAP)
                                    onSpeakAtSpeed(speed)
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
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.surfaceVariant)
                                            .padding(12.dp),
                                )
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Descubre la traducción y califica tu recuerdo",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (state.heartProgress > 0) {
                    Text(
                        text = "${state.heartProgress}/5 tarjetas para recuperar 1 ❤",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "Marcar «Otra vez» cuesta 1 vida. Sé honesto: el repaso se adapta a ti.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )

                if (!state.revealed) {
                    PrimaryButton3D(
                        text = "Mostrar traducción",
                        onClick = onReveal,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = "¿Qué tal te salió?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    GradeBar(onRate = onRate)
                }
            }
        }

        state.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** Botón de retorno del repaso (icono ← con objetivo táctil de 44dp). */
@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val feedback = LocalInteractionFeedback.current
    Icon(
        painter = painterResource(dev.linguago.core.ui.components.LinguaIcons.ArrowBackRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier =
            modifier
                .size(24.dp)
                .clickable {
                    feedback.play(FeedbackCue.TAP)
                    onClick()
                }.semantics {
                    role = Role.Button
                    contentDescription = "Volver"
                }.padding(0.dp),
    )
}

/** Píldora tan del contador de tarjetas del prototipo (ADR-055). */
@Composable
private fun DeckCounterPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = linguaExtendedColors()
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = colors.onSurface,
        modifier =
            modifier
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_PILL_DP.dp))
                .background(extended.tan)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Etiqueta tan de la tarjeta activa de la baraja (ADR-055). */
@Composable
private fun DeckBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = linguaExtendedColors()
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = colors.onSurface,
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(extended.tan)
                .padding(horizontal = 12.dp, vertical = 6.dp),
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
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_LARGE_DP.dp))
                .background(colors.surfaceVariant)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
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
                    revealed = true,
                ),
            onReveal = {},
            onSpeak = {},
            onSpeakAtSpeed = {},
            onRate = {},
            onExit = {},
        )
    }
}
