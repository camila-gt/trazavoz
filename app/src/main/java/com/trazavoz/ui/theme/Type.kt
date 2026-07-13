package com.trazavoz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.trazavoz.R

/**
 * Nunito is a single variable font (wght axis); each entry below points at the
 * same file with a different [FontVariation.Settings] so Compose picks a real
 * rendered weight instead of faux-bold. On API < 26 the OS falls back to the
 * font's default static instance (Regular) since variable axes require O+.
 */
@OptIn(ExperimentalTextApi::class)
val NunitoFontFamily = FontFamily(
    Font(R.font.nunito, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.nunito, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.nunito, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.nunito, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.nunito, weight = FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800)))
)

private val baseline = Typography()

val TrazavozTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = NunitoFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = NunitoFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = NunitoFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = NunitoFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = NunitoFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = NunitoFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = NunitoFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = NunitoFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = NunitoFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = NunitoFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = NunitoFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = NunitoFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = NunitoFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = NunitoFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = NunitoFontFamily)
)
