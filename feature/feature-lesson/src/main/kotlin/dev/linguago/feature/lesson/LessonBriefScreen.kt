package dev.linguago.feature.lesson

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.LinguaCard3D
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Micro-clase previa a los ejercicios (ADR-028): gramática/reglas de la
 * lección y mecánicas de los tipos de ejercicio presentes. Es saltable y no
 * consume vidas ni XP; se puede reabrir con "Guía".
 */
@Composable
fun LessonBriefScreen(
    brief: LessonBriefUi,
    startLabel: String,
    onStart: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val feedback = LocalInteractionFeedback.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Antes de empezar",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground,
                modifier =
                    Modifier
                        .weight(1f)
                        .semantics { heading() },
            )
            TextButton(
                onClick = {
                    feedback.play(FeedbackCue.TAP)
                    onExit()
                },
            ) {
                Text(text = "Salir")
            }
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = brief.title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = "Micro-clase de gramática y reglas",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
            )

            if (brief.goals.isNotEmpty()) {
                SectionTitle("Objetivos")
                LinguaCard3D {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        brief.goals.forEach { goal ->
                            Text(
                                text = "• $goal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface,
                            )
                        }
                    }
                }
            }

            if (brief.sections.isNotEmpty()) {
                SectionTitle("Reglas y ejemplos")
                brief.sections.forEach { section ->
                    LessonBriefSectionCard(section)
                }
            }

            if (brief.mechanics.isNotEmpty()) {
                SectionTitle("Cómo se juega")
                brief.mechanics.forEach { mechanic ->
                    LinguaCard3D {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = mechanic.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface,
                            )
                            Text(
                                text = mechanic.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Text(
                text = "Esta introducción no consume vidas ni XP. Puedes volver a abrirla con «Guía».",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(12.dp))
        PrimaryButton3D(
            text = startLabel,
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun LessonBriefSectionCard(section: LessonBriefSectionUi) {
    val colors = MaterialTheme.colorScheme
    LinguaCard3D {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = section.heading,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            Text(
                text = section.body,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
            section.examples.forEach { example ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "${example.source} → ${example.target}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary,
                    )
                    example.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonBriefScreenPreview() {
    LinguaTheme {
        LessonBriefScreen(
            brief =
                LessonBriefUi(
                    title = "Saludos y cortesía",
                    goals = listOf("Reconocer saludos básicos.", "Distinguir una despedida."),
                    sections =
                        listOf(
                            LessonBriefSectionUi(
                                heading = "Saludos",
                                body = "'Hello' es neutro; 'Good morning' se usa por la mañana.",
                                examples =
                                    listOf(
                                        LessonBriefExampleUi(
                                            source = "Buenas tardes",
                                            target = "Good afternoon",
                                            note = "Después del mediodía.",
                                        ),
                                    ),
                            ),
                        ),
                    mechanics =
                        listOf(
                            MechanicHint(
                                title = "Elegir la opción",
                                description = "Toca la opción correcta y revisa el porqué.",
                            ),
                        ),
                ),
            startLabel = "Empezar lección",
            onStart = {},
            onExit = {},
        )
    }
}
