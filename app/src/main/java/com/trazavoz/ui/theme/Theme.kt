package com.trazavoz.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Fixed fallback for devices without Material You (API < 31). */
private val FixedLightColorScheme = lightColorScheme(
    primary = CelestePastel,
    secondary = CoralPastel,
    tertiary = VerdeManzanaPastel,
    background = FondoClaro,
    surface = SuperficieNeutra,
    onPrimary = TextoPrimarioClaro,
    onSecondary = TextoPrimarioClaro,
    onTertiary = TextoPrimarioClaro,
    onBackground = TextoPrimarioClaro,
    onSurface = TextoPrimarioClaro
)

@Composable
fun TrazavozTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(context)
    } else {
        FixedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = TrazavozShapes,
        typography = TrazavozTypography,
        content = content
    )
}
