package dev.linguago.core.ui.components

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.linguago.core.domain.model.ThemeMode
import dev.linguago.core.ui.components.path.PathNode
import dev.linguago.core.ui.components.path.PathNodeState
import dev.linguago.core.ui.components.path.PathNodeUi
import dev.linguago.core.ui.components.path.UnitHeader
import dev.linguago.core.ui.theme.LinguaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Galería visual de la Etapa 3 (ADR-026). Renderiza todos los componentes
 * nuevos en claro y oscuro y guarda un PNG en el almacenamiento externo de la
 * app de test para revisión de diseño:
 * `adb pull /sdcard/Android/data/dev.linguago.core.ui.test/files/screenshots`.
 */
@RunWith(AndroidJUnit4::class)
public class DesignGalleryScreenshotTest {
    @get:Rule
    public val compose = createComposeRule()

    @Test
    public fun captura_galeria_clara() {
        capture(name = "feather-phase3-light.png", mode = ThemeMode.LIGHT)
    }

    @Test
    public fun captura_galeria_oscura() {
        capture(name = "feather-phase3-dark.png", mode = ThemeMode.DARK)
    }

    @Test
    public fun captura_recompensa() {
        capture(name = "feather-phase6-reward.png", mode = ThemeMode.LIGHT) {
            RewardSheet(xpEarned = 80, onContinue = {})
        }
    }

    @Test
    public fun captura_leccion_cancelada() {
        capture(name = "feather-phase6-cancel.png", mode = ThemeMode.LIGHT) {
            CancelledLessonSheet(xpLost = 40, onExit = {})
        }
    }

    @Test
    public fun captura_cofre() {
        capture(name = "feather-phase6-chest.png", mode = ThemeMode.LIGHT) {
            ChestRewardSheet(unitTitle = "Primeros pasos", onDismiss = {})
        }
    }

    private fun capture(
        name: String,
        mode: ThemeMode,
        content: @Composable () -> Unit = { Gallery() },
    ) {
        compose.setContent {
            LinguaTheme(themeMode = mode) {
                content()
            }
        }
        compose.waitForIdle()
        // Deja terminar contadores y confeti antes de capturar.
        compose.mainClock.advanceTimeBy(1_500)
        compose.waitForIdle()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        val file = File(dir, name)
        FileOutputStream(file).use { output ->
            compose
                .onRoot()
                .captureToImage()
                .asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        // AGP desinstala el APK de test al terminar y borra /Android/data:
        // se copia a /data/local/tmp (propiedad de shell) para poder pullearlo.
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.executeShellCommand("mkdir -p $REMOTE_DIR").close()
        automation.executeShellCommand("cp ${file.absolutePath} $REMOTE_DIR/$name").close()
        Log.i(TAG, "captura=$REMOTE_DIR/$name bytes=${file.length()}")
    }

    @Composable
    private fun Gallery() {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UnitHeader(title = "Negociación y decisiones", completed = 2, total = 5)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PathNode(PathNodeUi("n1", "Bloqueada", PathNodeState.LOCKED), onClick = {})
                PathNode(PathNodeUi("n2", "Disponible", PathNodeState.AVAILABLE), onClick = {})
                PathNode(PathNodeUi("n3", "En progreso", PathNodeState.IN_PROGRESS), onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PathNode(PathNodeUi("n4", "Completada", PathNodeState.COMPLETED), onClick = {})
                PathNode(PathNodeUi("n5", "Cofre", PathNodeState.CHEST), onClick = {})
                PathNode(PathNodeUi("n6", "Cofre bloqueado", PathNodeState.CHEST_LOCKED), onClick = {})
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                HeartsIndicator(hearts = 7)
                StreakIndicator(days = 5)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Nivel 3 · 40 / 100 XP", style = MaterialTheme.typography.labelLarge)
                LiquidXpBar(progress = 0.40f)
            }

            LinguaCard3D {
                Text(text = "Lección completada", style = MaterialTheme.typography.titleLarge)
                Text(text = "+80 XP", style = MaterialTheme.typography.bodyMedium)
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(72.dp),
            ) {
                ConfettiBurst(playKey = 1, modifier = Modifier.fillMaxSize())
            }

            PrimaryButton3D(text = "Continuar", onClick = {})
        }
    }

    private companion object {
        private const val TAG = "FeatherScreenshots"
        private const val REMOTE_DIR = "/data/local/tmp/feather-screenshots"
    }
}
