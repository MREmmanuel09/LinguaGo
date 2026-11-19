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
 * preferencia del usuario (invariante I8). El cuerpo usa 17sp SemiBold y los
 * textos secundarios 15sp para mejorar la lectura en pantallas pequeñas y en
 * modo oscuro sin recurrir a fuentes externas.
 */
val LinguaTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp,
                lineHeight = 52.sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                lineHeight = 48.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                lineHeight = 44.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                lineHeight = 42.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 30.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 26.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 26.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 26.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
    )
