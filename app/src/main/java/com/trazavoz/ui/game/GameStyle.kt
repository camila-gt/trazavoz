package com.trazavoz.ui.game

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.trazavoz.R

internal val VowelColor = Color(0xFFC92A2A)
internal val ConsonantColor = Color(0xFF1E6091)
internal val SuccessColor = Color(0xFF2B8A3E)
internal val TargetFill = Color(0xFFF0EDE4)
internal val TargetBorder = Color(0xFFC4BCAF)
internal val ActiveTargetFill = Color(0xFFEBF3F8)
internal val TileBorder = Color(0xFFE8E3D8)

private val Lexend = FontFamily(
    Font(R.font.lexend_regular, FontWeight.Normal),
    Font(R.font.lexend_semibold, FontWeight.SemiBold),
    Font(R.font.lexend_bold, FontWeight.Bold)
)

/** Scope these tokens to the game; the rest of the application's theme stays unchanged. */
@Composable
internal fun LiteracyGameTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.typography
    val type = base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = Lexend),
        displayMedium = base.displayMedium.copy(fontFamily = Lexend),
        displaySmall = base.displaySmall.copy(fontFamily = Lexend),
        headlineLarge = base.headlineLarge.copy(fontFamily = Lexend),
        headlineMedium = base.headlineMedium.copy(fontFamily = Lexend),
        headlineSmall = base.headlineSmall.copy(fontFamily = Lexend),
        titleLarge = base.titleLarge.copy(fontFamily = Lexend),
        titleMedium = base.titleMedium.copy(fontFamily = Lexend),
        titleSmall = base.titleSmall.copy(fontFamily = Lexend),
        bodyLarge = base.bodyLarge.copy(fontFamily = Lexend),
        bodyMedium = base.bodyMedium.copy(fontFamily = Lexend),
        bodySmall = base.bodySmall.copy(fontFamily = Lexend),
        labelLarge = base.labelLarge.copy(fontFamily = Lexend),
        labelMedium = base.labelMedium.copy(fontFamily = Lexend),
        labelSmall = base.labelSmall.copy(fontFamily = Lexend)
    )
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = ConsonantColor,
            onPrimary = Color.White,
            secondary = VowelColor,
            tertiary = SuccessColor,
            background = Color(0xFFFAF8F5),
            onBackground = Color(0xFF1A1D20),
            surface = Color.White,
            onSurface = Color(0xFF1A1D20),
            surfaceVariant = Color(0xFFF5F2EB),
            onSurfaceVariant = Color(0xFF495057),
            outline = Color(0xFFD9D2C5)
        ),
        typography = type,
        content = content
    )
}

fun buildPieceAnnotatedString(text: String): AnnotatedString = buildAnnotatedString {
    text.forEach { char ->
        val vowel = char.uppercaseChar() in "AEIOUÁÉÍÓÚÜ"
        withStyle(SpanStyle(color = if (vowel) VowelColor else ConsonantColor)) {
            append(char)
        }
    }
}
