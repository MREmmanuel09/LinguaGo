package dev.linguago.core.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.linguago.core.domain.logic.ClozePassageParser
import dev.linguago.core.domain.model.Exercise
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
    public fun semanticMatchBoard_rendersMultilineNuanceAndRegister() {
        compose.setContent {
            LinguaTheme {
                SemanticMatchBoard(
                    pairs = semanticPairs,
                    shuffledRights = listOf(0, 1),
                    selectedLeft = null,
                    matched = emptyMap(),
                    onSelectLeft = {},
                    onSelectRight = {},
                )
            }
        }

        compose.onNodeWithText("I'm knackered").assertIsDisplayed()
        compose.onNodeWithText("Coloquial").assertIsDisplayed()
        compose
            .onNodeWithText("Cansancio extremo, registro británico informal.")
            .assertIsDisplayed()
    }

    @Test
    public fun semanticMatchBoard_announcesMatchedAssociation() {
        compose.setContent {
            LinguaTheme {
                SemanticMatchBoard(
                    pairs = semanticPairs,
                    shuffledRights = listOf(1, 0),
                    selectedLeft = null,
                    matched = mapOf(0 to 0),
                    onSelectLeft = {},
                    onSelectRight = {},
                )
            }
        }

        compose
            .onNodeWithContentDescription("I'm knackered. Emparejado con Estoy agotado")
            .assertIsDisplayed()
    }

    @Test
    public fun semanticMatchBoard_survivesLargeFontScale() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = 2f),
            ) {
                LinguaTheme {
                    SemanticMatchBoard(
                        pairs = semanticPairs,
                        shuffledRights = listOf(0, 1),
                        selectedLeft = null,
                        matched = emptyMap(),
                        onSelectLeft = {},
                        onSelectRight = {},
                    )
                }
            }
        }

        compose.onNodeWithText("I'm knackered").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
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

    private val semanticPairs =
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
        )
}
