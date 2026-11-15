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
 * Hoja de licencias y créditos (ADR-008/ADR-056): la licencia comercial
 * propia del proyecto y las atribuciones de terceros (OFL, Apache-2.0,
 * MIT/CC0), accesible desde Ajustes.
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
                            "autorización previa del titular. El contenido " +
                            "pedagógico tiene licencia comercial propia.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text =
                        "Componentes de terceros con licencias abiertas: " +
                            "tipografía Nunito (SIL OFL 1.1), síntesis de voz " +
                            "Piper y sherpa-onnx (MIT/CC0/Apache-2.0), " +
                            "reconocimiento de voz Vosk y modelos Kaldi " +
                            "(Apache-2.0), JNA (LGPL-2.1 o Apache-2.0), " +
                            "Jetpack Compose, Kotlin y AndroidX (Apache-2.0). " +
                            "Consulta LICENSE, LICENSE-CONTENT y " +
                            "THIRD_PARTY_NOTICES.md del repositorio para los " +
                            "textos completos.",
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
