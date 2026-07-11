package com.trazavoz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = CoralPastel,
    secondary = CelestePastel,
    tertiary = VerdeManzanaPastel,
    background = FondoClaro,
    surface = AmarilloCrema,
    onPrimary = TextoPrimarioClaro,
    onSecondary = TextoPrimarioClaro,
    onBackground = TextoPrimarioClaro,
    onSurface = TextoPrimarioClaro
)

@Composable
fun TrazavozTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        shapes = TrazavozShapes,
        content = content
    )
}
