package dev.linguago.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Hoja de licencias y créditos (ADR-008/ADR-056/ADR-057): licencia comercial
 * propia y atribuciones de terceros, accesible desde Ajustes.
 */
@Composable
public fun LicensesSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Licencias y créditos") },
        text = {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text =
                        "LinguaGo — software propietario: todos los derechos " +
                            "reservados. El uso comercial, la redistribución y la " +
                            "modificación del código y del contenido requieren " +
                            "autorización previa del titular.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text =
                        "Voces TTS Piper empaquetadas (offline, fonemas precalculados): " +
                            "en_US-ljspeech-medium (LJ Speech, dominio público), " +
                            "fr_FR-siwis-medium (SIWIS, CC BY 4.0; atribución: Yamagishi et al. 2017, " +
                            "University of Edinburgh, DOI 10.7488/ds/1705; base Hi-Fi TTS CC BY 4.0), " +
                            "pt_BR-faber-medium (Faber, CC0 1.0; base Hi-Fi TTS CC BY 4.0). " +
                            "No se incluye espeak-ng en el APK.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text =
                        "Componentes de terceros: sherpa-onnx 2.0.0, Vosk/Kaldi, AndroidX, " +
                            "Jetpack Compose, Kotlin, kotlinx, OkHttp/Okio, Dagger Hilt (Apache-2.0), " +
                            "JNA (LGPL-2.1 o Apache-2.0), tipografía Nunito si aplica (SIL OFL 1.1). " +
                            "Textos completos en assets/licenses/ y en THIRD_PARTY_NOTICES.md del repositorio.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cerrar") }
        },
    )
}
