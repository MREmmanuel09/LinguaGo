package dev.linguago.core.ui.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.linguago.core.ui.theme.LinguaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PuzzleMatchBoardTest {
    @get:Rule
    val compose = createComposeRule()

    private val pairs =
        listOf(
            PuzzlePairUi(left = "hola", right = "Hello"),
            PuzzlePairUi(left = "adiós", right = "Goodbye"),
        )

    @Test
    fun tocar_izquierda_y_derecha_encaja_la_pareja() {
        val selected = mutableStateOf<Int?>(null)
        val matched = mutableStateOf<Map<Int, Int>>(emptyMap())

        compose.setContent {
            LinguaTheme {
                PuzzleMatchBoard(
                    pairs = pairs,
                    shuffledRights = listOf(0, 1),
                    selectedLeft = selected.value,
                    matched = matched.value,
                    onSelectLeft = { left -> selected.value = left },
                    onMatchRight = { right ->
                        selected.value?.let { left ->
                            matched.value = matched.value + (left to right)
                            selected.value = null
                        }
                    },
                    onUnpair = { left -> matched.value = matched.value - left },
                )
            }
        }

        compose.onNodeWithText("hola").performClick()
        compose.onNodeWithText("Hello").performClick()

        compose.runOnIdle { assertEquals(mapOf(0 to 0), matched.value) }
        compose.onNodeWithText("hola").assertIsDisplayed()
        compose.onNodeWithText("Hello").assertIsDisplayed()
    }

    @Test
    fun tocar_una_pareja_encajada_la_separa() {
        val selected = mutableStateOf<Int?>(null)
        val matched = mutableStateOf(mapOf(0 to 0))

        compose.setContent {
            LinguaTheme {
                PuzzleMatchBoard(
                    pairs = pairs,
                    shuffledRights = listOf(0, 1),
                    selectedLeft = selected.value,
                    matched = matched.value,
                    onSelectLeft = { left -> selected.value = left },
                    onMatchRight = { right ->
                        selected.value?.let { left ->
                            matched.value = matched.value + (left to right)
                            selected.value = null
                        }
                    },
                    onUnpair = { left -> matched.value = matched.value - left },
                )
            }
        }

        compose.onNodeWithText("hola").performClick()

        compose.runOnIdle { assertEquals(emptyMap<Int, Int>(), matched.value) }
        compose.onNodeWithText("Hello").assertIsDisplayed()
    }
}
