package com.trazavoz.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Pastel Infantil
val CoralPastel = Color(0xFFFF8B8B)
val CelestePastel = Color(0xFF8FD6E1)
val AmarilloCrema = Color(0xFFF9F1C1)
val VerdeManzanaPastel = Color(0xFFC1F1C5)
val PurpuraSuave = Color(0xFFD6C3F3)

val FondoClaro = Color(0xFFFAF8F5)
val FondoOscuro = Color(0xFF1E1B18)
val SuperficieNeutra = Color(0xFFF3F1EC)

val TextoPrimarioClaro = Color(0xFF2C2520)
val TextoSecundarioClaro = Color(0xFF5E544D)

/**
 * The only place in the app that stays multi-color regardless of theme: each
 * individual letter (A-Z grid, draggable tiles, filled slots) gets a stable
 * hue from this palette so the same letter always reads the same color.
 */
val LetterPalette = listOf(CoralPastel, CelestePastel, VerdeManzanaPastel, PurpuraSuave, AmarilloCrema)

private const val LetterColorAlphabet = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ"

fun colorForLetter(letter: Char): Color {
    val index = LetterColorAlphabet.indexOf(letter.uppercaseChar()).coerceAtLeast(0)
    return LetterPalette[index % LetterPalette.size]
}
