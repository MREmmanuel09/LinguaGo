package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.domain.model.Exercise
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Emparejamiento semántico B2 (F2.4): tarjetas multilínea con registro y
 * matiz pragmático para distinguir expresiones equivalentes. Cada tarjeta
 * conserva un objetivo táctil de 48dp y se adapta a `fontScale = 2.0`
 * (I8); la selección y la asociación son 1:1 por toque para TalkBack.
 */
@Composable
public fun SemanticMatchBoard(
    pairs: List<Exercise.SemanticPair>,
    shuffledRights: List<Int>,
    selectedLeft: Int?,
    matched: Map<Int, Int>,
    onSelectLeft: (Int) -> Unit,
    onSelectRight: (Int) -> Unit,
    modifier: Modifier = Modifier,
    instructions: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = instructions ?: "Empareja cada expresión con su equivalente",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        pairs.forEachIndexed { left, pair ->
            val association = matched[left]?.let { right -> pairs[right].right }
            SemanticTile(
                label = pair.left,
                register = pair.register?.let(::registerLabel),
                nuance = pair.nuance,
                association = association,
                selected = selectedLeft == left,
                enabled = true,
                onClick = { onSelectLeft(left) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = "Equivalentes",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        shuffledRights.forEach { right ->
            val taken = matched.containsValue(right)
            SemanticTile(
                label = pairs.getOrNull(right)?.right.orEmpty(),
                register = null,
                nuance = null,
                association = null,
                selected = false,
                enabled = !taken,
                onClick = { onSelectRight(right) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Tarjeta multilínea: etiqueta, chip de registro, matiz y asociación. */
@Composable
private fun SemanticTile(
    label: String,
    register: String?,
    nuance: String?,
    association: String?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val borderColor =
        when {
            selected -> colors.primary
            enabled -> colors.outline
            else -> colors.outlineVariant
        }
    val background =
        when {
            !enabled -> colors.surfaceVariant.copy(alpha = 0.5f)
            selected -> colors.primary.copy(alpha = 0.06f)
            else -> colors.surface
        }
    Column(
        modifier =
            modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(background)
                .border(2.dp, borderColor, RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    this.selected = selected
                    if (association != null) {
                        contentDescription = "$label. Emparejado con $association"
                    }
                }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (enabled) colors.onSurface else colors.onSurfaceVariant,
            )
            if (register != null) {
                RegisterBadge(register)
            }
        }
        if (nuance != null) {
            Text(
                text = nuance,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        if (association != null) {
            Text(
                text = "→ $association",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary,
            )
        }
    }
}

/** Chip del registro de la expresión (formal, informal, literario, coloquial). */
@Composable
private fun RegisterBadge(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

private fun registerLabel(register: Exercise.SemanticRegister): String =
    when (register) {
        Exercise.SemanticRegister.NEUTRAL -> "Neutro"
        Exercise.SemanticRegister.FORMAL -> "Formal"
        Exercise.SemanticRegister.INFORMAL -> "Informal"
        Exercise.SemanticRegister.LITERARY -> "Literario"
        Exercise.SemanticRegister.COLLOQUIAL -> "Coloquial"
    }

@Preview(showBackground = true)
@Composable
private fun SemanticMatchBoardPreview() {
    LinguaTheme {
        SemanticMatchBoard(
            pairs =
                listOf(
                    Exercise.SemanticPair(
                        left = "I'm knackered",
                        right = "Estoy agotado",
                        register = Exercise.SemanticRegister.COLLOQUIAL,
                        nuance = "Cansancio extremo, registro británico informal.",
                    ),
                    Exercise.SemanticPair(
                        left = "I am exhausted",
                        right = "Estoy exhausto",
                        register = Exercise.SemanticRegister.FORMAL,
                        nuance = "Registro neutro y formal; válido por escrito.",
                    ),
                ),
            shuffledRights = listOf(1, 0),
            selectedLeft = null,
            matched = emptyMap(),
            onSelectLeft = {},
            onSelectRight = {},
        )
    }
}
