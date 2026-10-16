package dev.linguago.feature.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.components.OptionCard
import dev.linguago.core.ui.components.PrimaryButton3D
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/** Identidad original: iris de color como marca, sin arte de terceros (I5). */
@Composable
private fun BrandMark(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(LinguaTokens.Primary),
            )
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(LinguaTokens.Accent),
            )
            Box(
                modifier =
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(LinguaTokens.Warning),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "LinguaGo",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/** Estado de selección de curso para el flujo de inicio. */
data class CourseOption(
    val id: String,
    val title: String,
    val subtitle: String,
)

private val COURSE_OPTIONS =
    listOf(
        CourseOption("es-en", "Inglés", "Español → Inglés · A1–B2"),
        CourseOption("es-pt", "Portugués", "Español → Portugués · A1–B2"),
        CourseOption("es-fr", "Francés", "Español → Francés · A1–B2"),
        CourseOption("es-ja", "Japonés", "Español → Japonés · N5–N3"),
    )

/**
 * Bienvenida + selección de curso (Fase 0 esqueleto: la selección aún no
 * activa la carga de cursos — Fase 1 conecta el repositorio).
 */
@Composable
fun OnboardingScreen(
    onCourseSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            BrandMark()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Aprende un idioma, sin esperas y sin conexión.\nTodo el contenido ya viene instalado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            COURSE_OPTIONS.forEach { option ->
                OptionCard(
                    text = option.subtitle,
                    selected = selectedId == option.id,
                    onClick = { selectedId = option.id },
                    leading = {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(12.dp))
                    },
                )
            }
        }

        Column {
            PrimaryButton3D(
                text = if (selectedId == null) "Elige un curso para continuar" else "Comenzar",
                onClick = { selectedId?.let(onCourseSelected) },
                enabled = selectedId != null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    LinguaTheme {
        OnboardingScreen(onCourseSelected = {})
    }
}
