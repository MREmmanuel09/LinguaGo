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
 * Hoja de licencias y créditos (ADR-008): atribuciones MIT/CC BY-SA/OFL/Apache
 * del código y del contenido pedagógico, accesible desde Ajustes.
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
                        "LinguaGo — código bajo licencia MIT. " +
                            "Contenido pedagógico bajo CC BY-SA 4.0. " +
                            "Tipografía Nunito bajo SIL Open Font License. " +
                            "Motor de síntesis y reconocimiento de voz bajo sus " +
                            "respectivas licencias abiertas (Vosk, Piper, sherpa-onnx, " +
                            "JNA y espeak-ng). Las bibliotecas que lo requieran " +
                            "pueden estar licenciadas bajo Apache-2.0. Consulta el " +
                            "archivo LICENSE del repositorio para el texto completo.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text =
                        "Los modelos de voz no se redistribuyen con la app; se " +
                            "descargan on-demand y se verifican por SHA-256 (I7).",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cerrar") }
        },
    )
}
