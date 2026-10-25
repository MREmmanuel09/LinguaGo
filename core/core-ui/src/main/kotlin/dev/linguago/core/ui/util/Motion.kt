package dev.linguago.core.ui.util

import android.animation.ValueAnimator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * `true` si el sistema permite animaciones. Respeta la escala de animación del
 * desarrollador y las opciones de accesibilidad (I8): cuando está desactivada,
 * los componentes muestran el estado final sin movimiento decorativo.
 */
@Composable
public fun rememberAnimationsEnabled(): Boolean = remember { ValueAnimator.areAnimatorsEnabled() }
