package com.trazavoz.ui.syllables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trazavoz.ui.components.BackButton
import com.trazavoz.ui.components.DragAndDropContainer
import com.trazavoz.ui.components.LocalDragAndDropState
import com.trazavoz.ui.components.LockLandscapeOrientation
import com.trazavoz.ui.theme.AmarilloCrema
import com.trazavoz.ui.theme.CelestePastel
import com.trazavoz.ui.theme.CoralPastel
import com.trazavoz.ui.theme.PurpuraSuave
import com.trazavoz.ui.theme.VerdeManzanaPastel
import com.trazavoz.ui.theme.colorForLetter
import com.trazavoz.ui.theme.rememberWindowInfo
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun SyllablePracticeScreen(
    letter: String,
    viewModel: SyllablePracticeViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Esta actividad siempre va en horizontal: da el ancho necesario para las
    // dos palabras y el banco sin que los recuadros queden diminutos.
    LockLandscapeOrientation()

    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight

    // Solo los controles (tamaños de toque) usan dp fijos; el contenido
    // (recuadros y sílabas) se dimensiona por fracción del espacio disponible.
    val padding = if (isCompact) 8.dp else 16.dp
    val ghostWidth = if (isCompact) 96.dp else 120.dp
    val bankTileHeight = if (isCompact) 44.dp else 58.dp
    val controlHeight = if (isCompact) 44.dp else 52.dp
    val controlIconSize = if (isCompact) 20.dp else 24.dp
    val backButtonSize = if (isCompact) 44.dp else 52.dp

    LaunchedEffect(letter) {
        viewModel.init(letter)
    }

    DragAndDropContainer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { dragState ->
        // La pantalla está bloqueada en horizontal: tutor/niño apilados a la
        // izquierda y banco vertical a la derecha.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.66f)
                    .fillMaxHeight()
            ) {
                BackButton(onBackClick, backButtonSize)
                WordSection(weightModifier = Modifier.weight(1f)) {
                    TutorWord(
                        slots = uiState.tutorSlots,
                        onSpeakWord = viewModel::speakTutorWord,
                        modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.55f)
                    )
                }
                WordSection(weightModifier = Modifier.weight(1f)) {
                    ChildWord(
                        slots = uiState.childSlots,
                        onTapSyllable = viewModel::onChildSlotTap,
                        modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.55f)
                    )
                }
            }

            BankPanel(modifier = Modifier.weight(0.34f).fillMaxHeight(), inset = isCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    uiState.bank.forEach { tile ->
                        DraggableSyllableComposable(
                            tile = tile,
                            modifier = Modifier.fillMaxWidth().height(bankTileHeight),
                            onDropped = { targetId -> routeDrop(targetId, tile.syllable, viewModel) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 10.dp))
                ControlButtons(
                    onRandom = viewModel::onRandom,
                    onClear = viewModel::onClearAll,
                    height = controlHeight,
                    iconSize = controlIconSize,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Sílaba "fantasma" que sigue al dedo mientras se arrastra.
        if (dragState.isDragging && dragState.dragItem is SyllableTile) {
            val tile = dragState.dragItem as SyllableTile
            val density = LocalDensity.current
            val localPos = dragState.currentDragLocalPosition
            val halfW = with(density) { ghostWidth.toPx() / 2f }
            val halfH = with(density) { bankTileHeight.toPx() / 2f }

            ElevatedCard(
                modifier = Modifier
                    .size(width = ghostWidth, height = bankTileHeight)
                    .offset {
                        IntOffset(
                            (localPos.x - halfW).roundToInt(),
                            (localPos.y - halfH).roundToInt()
                        )
                    }
                    .alpha(0.85f),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorForLetter(tile.syllable.first())
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = tile.syllable,
                        fontSize = (bankTileHeight.value * 0.4f).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Celebración superpuesta y NO bloqueante (el Canvas no captura toques).
        if (uiState.showCelebration) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}

/** Sección vertical que centra una palabra (tutor o niño) y le reserva su propio espacio. */
@Composable
private fun ColumnScope.WordSection(
    weightModifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = weightModifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** Contenedor con fondo y borde que agrupa el banco de sílabas y los controles. */
@Composable
private fun BankPanel(
    modifier: Modifier,
    inset: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
            .padding(if (inset) 6.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/** Palabra del tutor: dos cajas contiguas; tocarla la pronuncia completa. */
@Composable
private fun TutorWord(
    slots: List<SyllableSlot>,
    onSpeakWord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSpeakWord() },
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        slots.forEach { slot ->
            SyllableSlotComposable(
                slot = slot,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onTap = null
            )
        }
    }
}

/** Palabra del niño: dos cajas separadas con borde punteado; cada sílaba suena al tocarla. */
@Composable
private fun ChildWord(
    slots: List<SyllableSlot>,
    onTapSyllable: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        slots.forEach { slot ->
            SyllableSlotComposable(
                slot = slot,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onTap = { onTapSyllable(slot.index) }
            )
        }
    }
}

@Composable
private fun ControlButtons(
    onRandom: () -> Unit,
    onClear: () -> Unit,
    height: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onRandom,
            modifier = Modifier.weight(1f).height(height),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Filled.Casino,
                contentDescription = "Palabra aleatoria",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(iconSize)
            )
        }
        Button(
            onClick = onClear,
            modifier = Modifier.weight(1f).height(height),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Limpiar todo",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

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

/**
 * Lluvia de confetti que cae con gravedad: cada pieza parte del borde superior
 * con una velocidad inicial y deriva, y va acelerando hacia abajo. Se dibuja en
 * un Canvas a pantalla completa que NO intercepta toques, así el niño y el tutor
 * pueden seguir interactuando durante la celebración.
 */
@Composable
private fun ConfettiOverlay(modifier: Modifier = Modifier) {
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

private const val CONFETTI_LIFETIME_SECONDS = 5f
private const val CONFETTI_GRAVITY = 0.10f // fracción del alto por segundo²

/** Enruta un drop al hueco correspondiente según el id del target ("tutor-0", "child-1"). */
private fun routeDrop(targetId: String, syllable: String, viewModel: SyllablePracticeViewModel) {
    val parts = targetId.split("-")
    if (parts.size != 2) return
    val index = parts[1].toIntOrNull() ?: return
    when (parts[0]) {
        "tutor" -> viewModel.onDropOnTutor(index, syllable)
        "child" -> viewModel.onDropOnChild(index, syllable)
    }
}

@Composable
private fun SyllableSlotComposable(
    slot: SyllableSlot,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)?
) {
    val dragAndDropState = LocalDragAndDropState.current
    val shape = RoundedCornerShape(14.dp)

    DisposableEffect(slot.id) {
        onDispose { dragAndDropState.unregisterTarget(slot.id) }
    }

    val neutralFill = Color.LightGray.copy(alpha = 0.35f)
    val fill = when {
        slot.row == SlotRow.CHILD -> when (slot.validation) {
            SlotValidation.CORRECT -> VerdeManzanaPastel
            SlotValidation.INCORRECT -> CoralPastel
            SlotValidation.NONE -> neutralFill
        }
        else -> MaterialTheme.colorScheme.surface
    }

    val outline = MaterialTheme.colorScheme.outline
    val borderModifier = if (slot.row == SlotRow.CHILD) {
        Modifier.dashedBorder(color = outline, strokeWidth = 3.dp, cornerRadius = 14.dp)
    } else {
        Modifier.border(width = 3.dp, color = outline, shape = shape)
    }

    BoxWithConstraints(
        modifier = modifier
            .onGloballyPositioned {
                dragAndDropState.registerTarget(slot.id, it.boundsInWindow())
            }
            .clip(shape)
            .background(fill)
            .then(borderModifier)
            .then(if (onTap != null) Modifier.clickable { onTap() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // La tipografía se adapta al tamaño del recuadro (responsive por construcción).
        val fontSize = (maxHeight.value * 0.42f).coerceAtMost(64f).sp
        Text(
            text = slot.syllable ?: "",
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun DraggableSyllableComposable(
    tile: SyllableTile,
    modifier: Modifier = Modifier,
    onDropped: (String) -> Unit
) {
    val dragAndDropState = LocalDragAndDropState.current
    var positionInWindow by remember { mutableStateOf(Offset.Zero) }

    ElevatedCard(
        modifier = modifier
            .onGloballyPositioned {
                positionInWindow = it.boundsInWindow().center
            }
            .pointerInput(tile) {
                detectDragGestures(
                    onDragStart = {
                        dragAndDropState.onDragStart(tile, positionInWindow)
                    },
                    onDragEnd = {
                        val target = dragAndDropState.onDragEnd()
                        if (target is String) onDropped(target)
                    },
                    onDragCancel = {
                        dragAndDropState.isDragging = false
                        dragAndDropState.dragItem = null
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAndDropState.onDrag(dragAmount)
                    }
                )
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(containerColor = colorForLetter(tile.syllable.first()))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val fontSize = (maxHeight.value * 0.4f).coerceAtMost(28f).sp
            Text(
                text = tile.syllable,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Borde punteado redondeado para los huecos del niño (permite pintar el relleno). */
private fun Modifier.dashedBorder(color: Color, strokeWidth: Dp, cornerRadius: Dp): Modifier =
    this.drawBehind {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )
    }
