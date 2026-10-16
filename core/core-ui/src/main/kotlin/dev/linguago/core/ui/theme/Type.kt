package dev.linguago.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.linguago.core.ui.R

/**
 * Nunito (SIL OFL) como tipografía de identidad (RFC §7.1), pesos
 * 400/500/600/700/800 desde una fuente variable con ejes de peso.
 */
val Nunito =
    FontFamily(
        Font(
            resId = R.font.nunito_variable,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
        Font(
            resId = R.font.nunito_variable,
            weight = FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
        Font(
            resId = R.font.nunito_variable,
            weight = FontWeight.SemiBold,
            variationSettings = FontVariation.Settings(FontVariation.weight(600)),
        ),
        Font(
            resId = R.font.nunito_variable,
            weight = FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        ),
        Font(
            resId = R.font.nunito_variable,
            weight = FontWeight.ExtraBold,
            variationSettings = FontVariation.Settings(FontVariation.weight(800)),
        ),
    )

/**
 * Escala tipográfica original de LinguaGo. Tamaños en sp para respetar la
 * preferencia del usuario (invariante I8).
 */
val LinguaTypography =
    Typography(
        headlineLarge =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                lineHeight = 34.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 28.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 24.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
    )
