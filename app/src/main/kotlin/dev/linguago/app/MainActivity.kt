package dev.linguago.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.linguago.core.ui.theme.LinguaTheme

/**
 * Única Activity del sistema: toda la navegación es Compose (RFC §2.1).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LinguaTheme {
                LinguaApp()
            }
        }
    }
}
