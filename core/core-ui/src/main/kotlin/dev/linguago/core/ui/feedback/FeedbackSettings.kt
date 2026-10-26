package dev.linguago.core.ui.feedback

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Preferencias de feedback sensorial (ADR-027). Se proveen desde el
 * composition root y se persisten en DataStore.
 */
@Immutable
public data class FeedbackSettings(
    public val soundEffectsEnabled: Boolean = true,
    public val hapticsEnabled: Boolean = true,
)

/** Ajustes activos para los componentes de feedback. */
public val LocalFeedbackSettings: ProvidableCompositionLocal<FeedbackSettings> =
    staticCompositionLocalOf { FeedbackSettings() }
