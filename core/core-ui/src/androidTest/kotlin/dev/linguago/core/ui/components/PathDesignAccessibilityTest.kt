package dev.linguago.core.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.linguago.core.ui.components.path.ChestNode
import dev.linguago.core.ui.components.path.PathNode
import dev.linguago.core.ui.components.path.PathNodeState
import dev.linguago.core.ui.components.path.PathNodeUi
import dev.linguago.core.ui.components.path.UnitHeader
import dev.linguago.core.ui.theme.LinguaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Contrato de accesibilidad de los componentes del camino (ADR-026, I8):
 * roles, estados, objetivo táctil, HUD de vidas y progreso de XP.
 */
@RunWith(AndroidJUnit4::class)
public class PathDesignAccessibilityTest {
    @get:Rule
    public val compose = createComposeRule()

    @Test
    public fun pathNode_availableIsButtonWithStateAndClick() {
        var clicks = 0
        compose.setContent {
            LinguaTheme {
                PathNode(
                    node = PathNodeUi("n1", "Plazos y concesiones", PathNodeState.AVAILABLE),
                    onClick = { clicks++ },
                )
            }
        }

        compose
            .onNodeWithContentDescription("Lección: Plazos y concesiones. Disponible")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertHeightIsAtLeast(48.dp)
            .performClick()

        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    public fun pathNode_lockedIsDisabledAndDoesNotClick() {
        var clicks = 0
        compose.setContent {
            LinguaTheme {
                PathNode(
                    node = PathNodeUi("n2", "Presupuesto bajo presión", PathNodeState.LOCKED),
                    onClick = { clicks++ },
                )
            }
        }

        compose
            .onNodeWithContentDescription("Lección: Presupuesto bajo presión. Bloqueada")
            .assertIsNotEnabled()
            .performClick()

        compose.runOnIdle { assertEquals(0, clicks) }
    }

    @Test
    public fun chestNode_isIndependentAndAnnounced() {
        compose.setContent {
            LinguaTheme {
                ChestNode(id = "u1", label = "Primeros pasos", onClick = {})
            }
        }

        compose.onNodeWithContentDescription("Cofre: Primeros pasos").assertIsDisplayed()
        compose.onNodeWithText("Cofre").assertIsDisplayed()
    }

    @Test
    public fun pathNode_lockedChestIsDisabled() {
        var clicks = 0
        compose.setContent {
            LinguaTheme {
                PathNode(
                    node = PathNodeUi("chest:u1", "Primeros pasos", PathNodeState.CHEST_LOCKED),
                    onClick = { clicks++ },
                )
            }
        }

        compose
            .onNodeWithContentDescription("Cofre: Primeros pasos")
            .assertIsNotEnabled()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "Cofre bloqueado",
                ),
            ).performClick()

        compose.runOnIdle { assertEquals(0, clicks) }
    }

    @Test
    public fun heartsIndicator_isSingleAccessibilityNode() {
        compose.setContent {
            LinguaTheme {
                HeartsIndicator(hearts = 7)
            }
        }

        compose.onNodeWithContentDescription("7 de 10 vidas").assertIsDisplayed()
        compose.onAllNodesWithContentDescription("7 de 10 vidas").assertCountEquals(1)
    }

    @Test
    public fun liquidXpBar_exposesRangeInfo() {
        compose.setContent {
            LinguaTheme {
                LiquidXpBar(progress = 0.5f)
            }
        }

        compose
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertRangeInfoEquals(ProgressBarRangeInfo(0.5f, 0f..1f))
    }

    @Test
    public fun unitHeader_isHeadingWithProgress() {
        compose.setContent {
            LinguaTheme {
                UnitHeader(title = "Negociación y decisiones", completed = 2, total = 5)
            }
        }

        compose
            .onNodeWithText("Negociación y decisiones")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        compose.onNodeWithText("2 de 5 lecciones").assertIsDisplayed()
    }

    @Test
    public fun pathNode_survivesLargeFontScale() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = 2f),
            ) {
                LinguaTheme {
                    PathNode(
                        node = PathNodeUi("n3", "Quién decide qué", PathNodeState.IN_PROGRESS),
                        onClick = {},
                    )
                }
            }
        }

        compose
            .onNodeWithContentDescription("Lección: Quién decide qué. En progreso")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    public fun rewardSheet_showsXpAndContinues() {
        var continued = 0
        compose.setContent {
            LinguaTheme {
                RewardSheet(xpEarned = 80, onContinue = { continued++ })
            }
        }

        compose.onNodeWithText("¡Lección completada!").assertIsDisplayed()
        compose.mainClock.advanceTimeBy(1_000)
        compose.onNodeWithText("+80 XP").assertIsDisplayed()
        compose.onNodeWithText("Volver al camino").performClick()

        compose.runOnIdle { assertEquals(1, continued) }
    }

    @Test
    public fun cancelledSheet_explainsLostXpAndExits() {
        var exits = 0
        compose.setContent {
            LinguaTheme {
                CancelledLessonSheet(xpLost = 40, onExit = { exits++ })
            }
        }

        compose.onNodeWithText("Te quedaste sin vidas").assertIsDisplayed()
        compose.onNodeWithText("La lección se canceló y no se guardaron los 40 XP.").assertIsDisplayed()
        compose.onNodeWithText("Volver al camino").performClick()

        compose.runOnIdle { assertEquals(1, exits) }
    }

    @Test
    public fun chestSheet_celebratesCompletedUnit() {
        var dismissed = 0
        compose.setContent {
            LinguaTheme {
                ChestRewardSheet(unitTitle = "Primeros pasos", onDismiss = { dismissed++ })
            }
        }

        compose.onNodeWithText("¡Unidad completada!").assertIsDisplayed()
        compose.onNodeWithText("Primeros pasos").assertIsDisplayed()
        compose.onNodeWithText("Seguir").performClick()

        compose.runOnIdle { assertEquals(1, dismissed) }
    }
}
