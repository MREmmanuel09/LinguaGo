package dev.linguago.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.linguago.core.ui.components.FeedbackSheet
import dev.linguago.core.ui.components.GemsChip
import dev.linguago.core.ui.components.GradeBar
import dev.linguago.core.ui.components.MicListeningButton
import dev.linguago.core.ui.components.PillButton
import dev.linguago.core.ui.components.ReviewDeckStack
import dev.linguago.core.ui.components.StreakPill
import dev.linguago.core.ui.components.TaskBadgePill
import dev.linguago.core.ui.theme.LinguaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba instrumentada del prototipo «Warm Cream & Teal» (ADR-055): los
 * componentes nuevos se renderizan en un dispositivo real, responden al
 * toque y exponen sus textos a la jerarquía de accesibilidad (I8).
 */
@RunWith(AndroidJUnit4::class)
public class PrototypeComponentsTest {
    @get:Rule
    public val composeRule = createComposeRule()

    @Test
    public fun los_componentes_del_prototipo_se_renderizan_y_responden() {
        composeRule.setContent {
            LinguaTheme {
                Column {
                    TaskBadgePill(label = "Traduce esta oración")
                    StreakPill(count = 3)
                    PillButton(text = "Guía", onClick = {})
                    GemsChip(count = 2)
                    MicListeningButton(listening = false, onClick = {})
                    FeedbackSheet(
                        isCorrect = false,
                        message = "Revisa la respuesta",
                        correctAnswer = "It's seven o'clock",
                        diagnostic = "Usa It's para la hora y o'clock para la hora en punto.",
                        retryAvailable = true,
                        onRetry = {},
                        onReveal = {},
                        onContinue = {},
                    )
                    GradeBar(onRate = {})
                    ReviewDeckStack {
                        Text(text = "Monday")
                    }
                }
            }
        }

        composeRule.onNodeWithText("Traduce esta oración").assertExists()
        composeRule.onNodeWithText("3 seguidas").assertExists()
        composeRule.onNodeWithText("Guía").assertExists()
        composeRule.onNodeWithText("2").assertExists()
        composeRule.onNodeWithText("Continuar").assertExists()
        composeRule.onNodeWithText("Otra vez").assertExists()
        composeRule.onNodeWithText("Fácil").assertExists()
        composeRule.onNodeWithText("Monday").assertExists()
    }

    @Test
    public fun pillButton_y_mic_emiten_toques() {
        var pillTaps = 0
        var micTaps = 0
        composeRule.setContent {
            LinguaTheme {
                Column {
                    PillButton(text = "Guía", onClick = { pillTaps++ })
                    MicListeningButton(listening = false, onClick = { micTaps++ })
                }
            }
        }

        composeRule.onNodeWithText("Guía").performClick()
        composeRule.onNodeWithContentDescription("Grabar voz").performClick()

        composeRule.runOnIdle {
            org.junit.Assert.assertEquals(1, pillTaps)
            org.junit.Assert.assertEquals(1, micTaps)
        }
    }

    @Test
    public fun la_barra_de_calificacion_emite_cada_nivel() {
        val ratings = mutableListOf<dev.linguago.core.domain.logic.ReviewRating>()
        composeRule.setContent {
            LinguaTheme {
                GradeBar(onRate = { rating -> ratings += rating })
            }
        }

        composeRule.onNodeWithText("Otra vez").performClick()
        composeRule.onNodeWithText("Fácil").performClick()

        composeRule.runOnIdle {
            org.junit.Assert.assertEquals(2, ratings.size)
            org.junit.Assert.assertEquals(
                dev.linguago.core.domain.logic.ReviewRating.AGAIN,
                ratings.first(),
            )
            org.junit.Assert.assertEquals(
                dev.linguago.core.domain.logic.ReviewRating.EASY,
                ratings.last(),
            )
        }
    }
}
