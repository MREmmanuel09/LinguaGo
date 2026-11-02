package dev.linguago.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.theme.LinguaMotion
import dev.linguago.core.ui.theme.LinguaTheme
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors
import dev.linguago.core.ui.util.rememberAnimationsEnabled

/**
 * Ficha de palabra del tablero de respuesta (ADR-031, ADR-036): cuerpo con
 * borde inferior 3D, bisel de luz y hundimiento al presionar. Comparte la
 * física de `PrimaryButton3D`/`OptionCard` y emite `SELECT`/`DESELECT` según su
 * estado. Si [translations] existe, mantener pulsada la ficha muestra las
 * traducciones en un popup encima, sin tapar la palabra; se oculta al soltar.
 * Semántica de botón con estado y acción “Ver traducciones” para TalkBack (I8).
 */
@Composable
public fun TokenChip(
    token: String,
    selected: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    translations: List<String>? = null,
    status: OptionStatus = OptionStatus.NEUTRAL,
) {
    val colors = MaterialTheme.colorScheme
    val extended = linguaExtendedColors()
    val feedback = LocalInteractionFeedback.current
    val animations = rememberAnimationsEnabled()
    val background by animateColorAsState(
        targetValue =
            when (status) {
                OptionStatus.CORRECT -> LinguaTokens.Accent.copy(alpha = 0.12f)
                OptionStatus.INCORRECT -> LinguaTokens.Destructive.copy(alpha = 0.12f)
                OptionStatus.NEUTRAL -> if (selected) colors.primary else colors.surface
            },
        animationSpec = if (animations) tween(durationMillis = 160) else snap(),
        label = "tokenBackground",
    )
    val edgeColor by animateColorAsState(
        targetValue =
            when (status) {
                OptionStatus.CORRECT -> LinguaTokens.Accent.copy(alpha = 0.62f)
                OptionStatus.INCORRECT -> LinguaTokens.Destructive.copy(alpha = 0.62f)
                OptionStatus.NEUTRAL ->
                    if (selected) LinguaTokens.PrimaryEdge else colors.outline.copy(alpha = 0.55f)
            },
        animationSpec = if (animations) tween(durationMillis = 160) else snap(),
        label = "tokenEdge",
    )
    val contentColor by animateColorAsState(
        targetValue =
            when (status) {
                OptionStatus.CORRECT -> extended.accentInk
                OptionStatus.INCORRECT -> extended.destructiveInk
                OptionStatus.NEUTRAL -> if (selected) colors.onPrimary else colors.onSurface
            },
        animationSpec = if (animations) tween(durationMillis = 160) else snap(),
        label = "tokenContent",
    )
    var pressed by remember { mutableStateOf(false) }
    var showTranslation by remember { mutableStateOf(false) }
    LaunchedEffect(token, translations) {
        pressed = false
        showTranslation = false
    }
    val press by animateFloatAsState(
        targetValue = if (pressed && animations) 1f else 0f,
        animationSpec = if (animations) LinguaMotion.SpringBouncy else snap(),
        label = "tokenPress",
    )
    val shape = RoundedCornerShape(12.dp)

    Box(modifier = modifier.padding(bottom = LinguaTokens.PRESS_DEPTH_DP.dp)) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .offset(y = LinguaTokens.PRESS_DEPTH_DP.dp)
                    .clip(shape)
                    .background(edgeColor),
        )
        Text(
            text = token,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            textDecoration = if (status == OptionStatus.INCORRECT) TextDecoration.LineThrough else null,
            modifier =
                Modifier
                    .offset(y = (press * LinguaTokens.PRESS_DEPTH_DP).dp)
                    .clip(shape)
                    .background(background)
                    .topHighlight()
                    .pointerInput(enabled, token, translations) {
                        detectTapGestures(
                            onPress = {
                                if (enabled) {
                                    pressed = true
                                    tryAwaitRelease()
                                    pressed = false
                                    showTranslation = false
                                }
                            },
                            onLongPress = {
                                if (enabled && translations != null) showTranslation = true
                            },
                            onTap = {
                                if (enabled) {
                                    feedback.play(if (selected) FeedbackCue.DESELECT else FeedbackCue.SELECT)
                                    onToggle(token)
                                }
                            },
                        )
                    }.semantics {
                        role = Role.Button
                        this.selected = selected
                        if (enabled) {
                            onClick(label = if (selected) "Quitar" else "Elegir") {
                                feedback.play(if (selected) FeedbackCue.DESELECT else FeedbackCue.SELECT)
                                onToggle(token)
                                true
                            }
                        } else {
                            disabled()
                        }
                        if (translations != null) {
                            customActions =
                                listOf(
                                    CustomAccessibilityAction("Ver traducciones") {
                                        showTranslation = true
                                        true
                                    },
                                )
                        }
                    }.padding(horizontal = 12.dp, vertical = 14.dp),
        )
        if (translations != null) {
            TranslationTooltip(
                translations = translations,
                visible = showTranslation,
                onDismiss = { showTranslation = false },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TokenChipPreview() {
    LinguaTheme {
        TokenChip(token = "hola", selected = false, onToggle = {}, translations = listOf("hello"))
    }
}
