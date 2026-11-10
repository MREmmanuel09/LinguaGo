package dev.linguago.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.linguago.core.domain.logic.ClozePassageParser
import dev.linguago.core.domain.logic.FreeSpeakingFeedback
import dev.linguago.core.domain.logic.FreeSpeakingVerdict
import dev.linguago.core.ui.theme.LinguaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Contrato de accesibilidad del design system (invariante I8): roles de
 * TalkBack, estado de selección, live regions, progreso y touch targets.
 * Se ejecuta en dispositivo/emulador (instrumentado).
 */
@RunWith(AndroidJUnit4::class)
public class DesignSystemAccessibilityTest {
    @get:Rule
    public val compose = createComposeRule()

    @Test
    public fun primaryButton_hasButtonRoleAndMinTouchTarget() {
        var clicks = 0
        compose.setContent {
            LinguaTheme {
                PrimaryButton3D(text = "Continuar", onClick = { clicks++ })
            }
        }

        compose
            .onNodeWithText("Continuar")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertHeightIsAtLeast(48.dp)
            .performClick()

        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    public fun primaryButton_disabledIsNotClickable() {
        var clicks = 0
        compose.setContent {
            LinguaTheme {
                PrimaryButton3D(text = "Continuar", onClick = { clicks++ }, enabled = false)
            }
        }

        compose
            .onNodeWithText("Continuar")
            .assertIsNotEnabled()
            .performClick()

        compose.runOnIdle { assertEquals(0, clicks) }
    }

    @Test
    public fun optionCard_reportsRadioButtonAndSelection() {
        compose.setContent {
            LinguaTheme {
                OptionCard(text = "Hello", selected = true, onClick = {})
            }
        }

        compose
            .onNodeWithText("Hello")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    public fun tokenChip_reportsButtonRoleAndSelection() {
        compose.setContent {
            LinguaTheme {
                TokenChip(token = "hola", selected = true, onToggle = {})
            }
        }

        compose
            .onNodeWithText("hola")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    public fun flipCard_revealsBackAndReportsState() {
        val revealed = mutableStateOf(false)
        compose.setContent {
            LinguaTheme {
                FlipCard3D(
                    revealed = revealed.value,
                    onFlip = { revealed.value = true },
                    front = { Text("hermano") },
                    back = { Text("brother") },
                )
            }
        }

        compose.onNodeWithText("hermano").assertIsDisplayed()
        compose.onNodeWithText("brother").assertDoesNotExist()

        compose.onNodeWithText("hermano").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("brother").assertIsDisplayed()
        compose
            .onNode(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "Traducción visible",
                ),
            ).assertExists()
    }

    @Test
    public fun progressBar_exposesRangeInfo() {
        compose.setContent {
            LinguaTheme {
                LinguaProgressBar(progress = 0.5f)
            }
        }

        compose
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertRangeInfoEquals(ProgressBarRangeInfo(0.5f, 0f..1f))
    }

    @Test
    public fun streakIndicator_announcesDays() {
        compose.setContent {
            LinguaTheme {
                StreakIndicator(days = 7)
            }
        }

        compose
            .onNodeWithContentDescription("Racha de 7 días")
            .assertIsDisplayed()
    }

    @Test
    public fun feedbackBanner_isPoliteLiveRegion() {
        compose.setContent {
            LinguaTheme {
                FeedbackBanner(isCorrect = true, message = "¡Correcto!")
            }
        }

        compose
            .onNodeWithText("¡Correcto!")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    public fun infoBanner_isPoliteLiveRegion() {
        compose.setContent {
            LinguaTheme {
                InfoBanner(message = "Preparando motor de voz offline…")
            }
        }

        compose
            .onNodeWithText("Preparando motor de voz offline…")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    public fun wordGlossaryText_muestraTraduccionesAlMantenerPulsado() {
        compose.setContent {
            LinguaTheme {
                WordGlossaryText(
                    text = "bank",
                    glossary = mapOf("bank" to listOf("banco", "orilla")),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        compose.onNodeWithText("bank").performTouchInput { longClick() }

        compose.onNodeWithText("banco").assertIsDisplayed()
        compose.onNodeWithText("orilla").assertIsDisplayed()
    }

    @Test
    public fun wordGlossaryText_resuelveModismosPorFrase() {
        compose.setContent {
            LinguaTheme {
                WordGlossaryText(
                    text = "play it by ear",
                    glossary = mapOf("play it by ear" to listOf("improvisar según las circunstancias")),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        compose.onNodeWithText("play it by ear").performTouchInput { longClick() }

        compose.onNodeWithText("improvisar según las circunstancias").assertIsDisplayed()
    }

    @Test
    public fun clozeBoard_glossaryExposesTranslationsAction() {
        compose.setContent {
            LinguaTheme {
                ClozeDragDropBoard(
                    segments = ClozePassageParser.parse("I {{hueco}} tea."),
                    options = listOf("drink", "make"),
                    filled = emptyMap(),
                    onAssign = { _, _ -> },
                    onClearBlank = {},
                    touchExplorationOverride = true,
                    glossary = mapOf("tea" to listOf("té")),
                )
            }
        }

        compose
            .onNodeWithText(" tea.", useUnmergedTree = true)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions))
    }

    @Test
    public fun feedbackBanner_glossaryExposesTranslationsAction() {
        compose.setContent {
            LinguaTheme {
                FeedbackBanner(
                    isCorrect = false,
                    message = "Revisa la respuesta y sigue",
                    correctAnswer = "tea",
                    glossary = mapOf("tea" to listOf("té")),
                )
            }
        }

        compose
            .onNodeWithText("Respuesta correcta: tea", useUnmergedTree = true)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions))
    }

    @Test
    public fun feedbackBanner_expandsSpecificDiagnostic() {
        compose.setContent {
            LinguaTheme {
                FeedbackBanner(
                    isCorrect = false,
                    message = "Revisa la respuesta y sigue",
                    diagnostic = "Usa el pasado simple en la segunda frase.",
                )
            }
        }

        // Colapsado por defecto (ADR-037): el primer botón es "Ver por qué".
        compose.onNodeWithText("Usa el pasado simple en la segunda frase.").assertDoesNotExist()

        compose.onNodeWithText("Ver por qué").performClick()

        compose.onNodeWithText("Usa el pasado simple en la segunda frase.").assertIsDisplayed()

        compose.onNodeWithText("Ocultar detalle").performClick()

        compose.onNodeWithText("Usa el pasado simple en la segunda frase.").assertDoesNotExist()
    }

    @Test
    public fun feedbackBanner_revealsCulturalNoteOnDemand() {
        compose.setContent {
            LinguaTheme {
                FeedbackBanner(
                    isCorrect = true,
                    message = "¡Correcto!",
                    idiom = "play it by ear",
                    culturalNote = "Decidir sobre la marcha.",
                )
            }
        }

        compose.onNodeWithText("Modismo: play it by ear").assertDoesNotExist()

        compose.onNodeWithText("Ver nota cultural").performClick()

        compose.onNodeWithText("Modismo: play it by ear").assertIsDisplayed()
        compose.onNodeWithText("Decidir sobre la marcha.").assertIsDisplayed()
    }

    @Test
    public fun clozeBoard_tapModeAssignsAndClears() {
        val filledTarget = mutableStateOf<Map<String, String>>(emptyMap())
        compose.setContent {
            LinguaTheme {
                ClozeDragDropBoard(
                    segments = ClozePassageParser.parse("I {{hueco}} tea."),
                    options = listOf("drink", "make"),
                    filled = filledTarget.value,
                    onAssign = { blankId, option -> filledTarget.value = mapOf(blankId to option) },
                    onClearBlank = { filledTarget.value = emptyMap() },
                    touchExplorationOverride = true,
                )
            }
        }

        compose.onNodeWithText("drink").performClick()
        compose.onNodeWithContentDescription("Hueco 1 vacío").performClick()

        compose.onNodeWithContentDescription("Hueco 1: drink").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(setOf("hueco"), filledTarget.value.keys)
        }

        compose.onNodeWithContentDescription("Hueco 1: drink").performClick()

        compose.onNodeWithContentDescription("Hueco 1 vacío").assertIsDisplayed()
        compose.runOnIdle {
            assertTrue(filledTarget.value.isEmpty())
        }
    }

    @Test
    public fun primaryButton_survivesLargeFontScale() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = 2f),
            ) {
                LinguaTheme {
                    PrimaryButton3D(text = "Continuar", onClick = {})
                }
            }
        }

        compose.onNodeWithText("Continuar").assertIsDisplayed()
    }

    @Test
    public fun audioOnlyPanel_playsSlowlyWhenAllowed() {
        var slowPlays = 0
        compose.setContent {
            LinguaTheme {
                AudioOnlyPromptPanel(
                    audioAvailable = true,
                    allowSlowPlayback = true,
                    transcript = null,
                    revealTranscript = false,
                    onPlay = {},
                    onPlaySlow = { slowPlays++ },
                )
            }
        }

        compose.onNodeWithText("Reproducir más despacio (0.75x)").performClick()

        compose.runOnIdle { assertEquals(1, slowPlays) }
    }

    @Test
    public fun audioOnlyPanel_hidesTranscriptUntilRevealed() {
        val revealed = mutableStateOf(false)
        compose.setContent {
            LinguaTheme {
                AudioOnlyPromptPanel(
                    audioAvailable = true,
                    allowSlowPlayback = false,
                    transcript = "The weather is nice today.",
                    revealTranscript = revealed.value,
                    onPlay = {},
                    onPlaySlow = {},
                )
            }
        }

        compose.onNodeWithText("The weather is nice today.").assertDoesNotExist()

        revealed.value = true
        compose.waitForIdle()

        compose.onNodeWithText("The weather is nice today.").assertIsDisplayed()
    }

    @Test
    public fun audioOnlyPanel_hidesSlowButtonWhenNotAllowed() {
        compose.setContent {
            LinguaTheme {
                AudioOnlyPromptPanel(
                    audioAvailable = true,
                    allowSlowPlayback = false,
                    transcript = null,
                    revealTranscript = false,
                    onPlay = {},
                    onPlaySlow = {},
                )
            }
        }

        compose.onNodeWithText("Reproducir más despacio (0.75x)").assertDoesNotExist()
    }

    @Test
    public fun freeSpeakingPanel_showsWaveformAndLiveKeywords() {
        compose.setContent {
            LinguaTheme {
                FreeSpeakingPanel(
                    prompt = "Describe una decisión.",
                    speechAvailable = true,
                    recording = true,
                    waveform = listOf(0.2f, 0.8f),
                    liveKeywords = listOf("deadline"),
                    partialTranscript = "the deadline is",
                    transcript = null,
                    durationMillis = 0L,
                    feedback = null,
                    detectedGrammarLabels = emptyList(),
                    modelAnswer = "Modelo",
                    criteria = listOf("Fluidez"),
                    selfAssessment = emptyMap(),
                    audioAvailable = true,
                    onStart = {},
                    onStop = {},
                    onRate = { _, _ -> },
                    onPlayModel = {},
                    onSkip = {},
                )
            }
        }

        compose.onNodeWithContentDescription("Visualizador de energía de voz").assertIsDisplayed()
        compose.onNodeWithText("deadline").assertIsDisplayed()
        compose.onNodeWithText("Detener grabación").assertIsDisplayed()
    }

    @Test
    public fun freeSpeakingPanel_reviewShowsHeuristicBreakdown() {
        compose.setContent {
            LinguaTheme {
                FreeSpeakingPanel(
                    prompt = "Describe una decisión.",
                    speechAvailable = true,
                    recording = false,
                    waveform = emptyList(),
                    liveKeywords = emptyList(),
                    partialTranscript = "",
                    transcript = "Last year I changed jobs.",
                    durationMillis = 8_000L,
                    feedback = freeSpeakingFeedback,
                    detectedGrammarLabels = listOf("Conditionals"),
                    modelAnswer = "Modelo",
                    criteria = listOf("Fluidez"),
                    selfAssessment = emptyMap(),
                    audioAvailable = true,
                    onStart = {},
                    onStop = {},
                    onRate = { _, _ -> },
                    onPlayModel = {},
                    onSkip = {},
                )
            }
        }

        compose.onNodeWithContentDescription("Palabras: 14").assertIsDisplayed()
        compose.onNodeWithContentDescription("Ideas clave: 2/2").assertIsDisplayed()
        compose.onNodeWithContentDescription("Gramática detectada: Conditionals").assertIsDisplayed()
        compose.onNodeWithContentDescription("Ritmo: 108 palabras/min").assertIsDisplayed()
    }

    @Test
    public fun freeSpeakingPanel_selfAssessmentHasThreeLevels() {
        var rated: Pair<Int, Int>? = null
        compose.setContent {
            LinguaTheme {
                FreeSpeakingPanel(
                    prompt = "Describe una decisión.",
                    speechAvailable = true,
                    recording = false,
                    waveform = emptyList(),
                    liveKeywords = emptyList(),
                    partialTranscript = "",
                    transcript = "Last year I changed jobs.",
                    durationMillis = 8_000L,
                    feedback = freeSpeakingFeedback,
                    detectedGrammarLabels = emptyList(),
                    modelAnswer = "Modelo",
                    criteria = listOf("Fluidez"),
                    selfAssessment = emptyMap(),
                    audioAvailable = true,
                    onStart = {},
                    onStop = {},
                    onRate = { criterion, level -> rated = criterion to level },
                    onPlayModel = {},
                    onSkip = {},
                )
            }
        }

        compose.onNodeWithContentDescription("Criterio 1: Aún no").assertIsDisplayed()
        compose.onNodeWithContentDescription("Criterio 1: Casi").assertIsDisplayed()
        compose.onNodeWithContentDescription("Criterio 1: Sí").performClick()

        compose.runOnIdle { assertEquals(0 to 2, rated) }
    }

    @Test
    public fun freeSpeakingPanel_modelAnswerPlaybackIsExposed() {
        var plays = 0
        compose.setContent {
            LinguaTheme {
                FreeSpeakingPanel(
                    prompt = "Describe una decisión.",
                    speechAvailable = true,
                    recording = false,
                    waveform = emptyList(),
                    liveKeywords = emptyList(),
                    partialTranscript = "",
                    transcript = "Last year I changed jobs.",
                    durationMillis = 8_000L,
                    feedback = freeSpeakingFeedback,
                    detectedGrammarLabels = emptyList(),
                    modelAnswer = "Modelo",
                    criteria = listOf("Fluidez"),
                    selfAssessment = emptyMap(),
                    audioAvailable = true,
                    onStart = {},
                    onStop = {},
                    onRate = { _, _ -> },
                    onPlayModel = { plays++ },
                    onSkip = {},
                )
            }
        }

        compose.onNodeWithText("Escuchar respuesta modelo").performClick()

        compose.runOnIdle { assertEquals(1, plays) }
    }

    @Test
    public fun freeSpeakingPanel_withoutSpeechOffersSkip() {
        var skips = 0
        compose.setContent {
            LinguaTheme {
                FreeSpeakingPanel(
                    prompt = "Describe una decisión.",
                    speechAvailable = false,
                    recording = false,
                    waveform = emptyList(),
                    liveKeywords = emptyList(),
                    partialTranscript = "",
                    transcript = null,
                    durationMillis = 0L,
                    feedback = null,
                    detectedGrammarLabels = emptyList(),
                    modelAnswer = "Modelo",
                    criteria = listOf("Fluidez"),
                    selfAssessment = emptyMap(),
                    audioAvailable = true,
                    onStart = {},
                    onStop = {},
                    onRate = { _, _ -> },
                    onPlayModel = {},
                    onSkip = { skips++ },
                )
            }
        }

        compose.onNodeWithText("Grabar respuesta").assertIsNotEnabled()
        compose.onNodeWithText("No puedo hablar ahora").performClick()

        compose.runOnIdle { assertEquals(1, skips) }
    }

    @Test
    public fun freeSpeakingPanel_survivesLargeFontScale() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = 2f),
            ) {
                LinguaTheme {
                    FreeSpeakingPanel(
                        prompt = "Describe una decisión.",
                        speechAvailable = true,
                        recording = false,
                        waveform = emptyList(),
                        liveKeywords = emptyList(),
                        partialTranscript = "",
                        transcript = "Last year I changed jobs.",
                        durationMillis = 8_000L,
                        feedback = freeSpeakingFeedback,
                        detectedGrammarLabels = emptyList(),
                        modelAnswer = "Modelo",
                        criteria = listOf("Fluidez"),
                        selfAssessment = emptyMap(),
                        audioAvailable = true,
                        onStart = {},
                        onStop = {},
                        onRate = { _, _ -> },
                        onPlayModel = {},
                        onSkip = {},
                    )
                }
            }
        }

        compose.onNodeWithText("Last year I changed jobs.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Criterio 1: Sí").assertIsDisplayed()
    }

    private val freeSpeakingFeedback =
        FreeSpeakingFeedback(
            passes = true,
            tokenCount = 14,
            coveredClusters = 2,
            totalClusters = 2,
            detectedGrammarRefs = emptyList(),
            wpm = 108.0,
            verdict = FreeSpeakingVerdict.Pass,
        )
}
