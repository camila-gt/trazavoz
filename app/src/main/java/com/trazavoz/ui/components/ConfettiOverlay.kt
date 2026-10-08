package com.trazavoz.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.trazavoz.ui.theme.AmarilloCrema
import com.trazavoz.ui.theme.CelestePastel
import com.trazavoz.ui.theme.CoralPastel
import com.trazavoz.ui.theme.PurpuraSuave
import com.trazavoz.ui.theme.VerdeManzanaPastel
import kotlin.random.Random

/** Una pieza de confetti; su trayectoria se calcula analíticamente por tiempo. */
private data class ConfettiPiece(
    val startXFraction: Float,
    val startYFraction: Float,
    val velocityX: Float,       // fracción del ancho por segundo
    val velocityY: Float,       // fracción del alto por segundo (inicial)
    val widthPx: Float,
    val heightPx: Float,
    val color: Color,
    val startRotation: Float,
    val rotationSpeed: Float     // grados por segundo
)

private const val CONFETTI_LIFETIME_SECONDS = 5f
private const val CONFETTI_GRAVITY = 0.10f // fracción del alto por segundo²

/**
 * Lluvia de confetti que cae con gravedad: cada pieza parte del borde superior
 * con una velocidad inicial y deriva, y va acelerando hacia abajo mientras se
 * desvanece de forma suave. Se dibuja en un Canvas que NO intercepta toques, así
 * que quien esté usando la app puede seguir interactuando durante la celebración.
 */
@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val colors = listOf(CoralPastel, CelestePastel, AmarilloCrema, VerdeManzanaPastel, PurpuraSuave)

    val pieces = remember {
        List(90) {
            ConfettiPiece(
                startXFraction = Random.nextFloat(),
                startYFraction = -Random.nextFloat() * 0.2f,
                velocityX = (Random.nextFloat() - 0.5f) * 0.15f,
                velocityY = 0.02f + Random.nextFloat() * 0.08f,
                widthPx = with(density) { (6 + Random.nextInt(8)).dp.toPx() },
                heightPx = with(density) { (8 + Random.nextInt(10)).dp.toPx() },
                color = colors[Random.nextInt(colors.size)],
                startRotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    var elapsedSeconds by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val startNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                elapsedSeconds = (now - startNanos) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = modifier) {
        val t = elapsedSeconds
        // Desvanecido suave (smoothstep) a lo largo de toda la caída.
        val progress = (t / CONFETTI_LIFETIME_SECONDS).coerceIn(0f, 1f)
        val fade = 1f - (progress * progress * (3f - 2f * progress)) // smoothstep invertido
        if (fade <= 0f) return@Canvas

        pieces.forEach { piece ->
            val yFraction = piece.startYFraction +
                piece.velocityY * t + 0.5f * CONFETTI_GRAVITY * t * t
            if (yFraction > 1.15f) return@forEach

            val cx = (piece.startXFraction + piece.velocityX * t) * size.width
            val cy = yFraction * size.height
            val angle = piece.startRotation + piece.rotationSpeed * t

            rotate(degrees = angle, pivot = Offset(cx, cy)) {
                drawRect(
                    color = piece.color,
                    topLeft = Offset(cx - piece.widthPx / 2f, cy - piece.heightPx / 2f),
                    size = Size(piece.widthPx, piece.heightPx),
                    alpha = fade
                )
            }
        }
    }
}
