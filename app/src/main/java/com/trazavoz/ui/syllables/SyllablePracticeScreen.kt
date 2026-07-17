package com.trazavoz.ui.syllables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trazavoz.ui.components.LocalDragAndDropState
import com.trazavoz.ui.components.DragAndDropContainer
import com.trazavoz.ui.theme.CoralPastel
import com.trazavoz.ui.theme.VerdeManzanaPastel
import com.trazavoz.ui.theme.colorForLetter
import com.trazavoz.ui.theme.rememberWindowInfo
import kotlin.math.roundToInt

@Composable
fun SyllablePracticeScreen(
    letter: String,
    viewModel: SyllablePracticeViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight

    val padding = if (isCompact) 8.dp else 16.dp
    val slotWidth = when {
        windowInfo.isExpandedWidth -> 180.dp
        windowInfo.isLandscape -> 150.dp
        else -> 140.dp
    }
    val slotHeight = when {
        windowInfo.isExpandedWidth -> 150.dp
        windowInfo.isLandscape -> 104.dp
        isCompact -> 92.dp
        else -> 120.dp
    }
    val slotFontSize = when {
        windowInfo.isExpandedWidth -> 46.sp
        windowInfo.isLandscape -> 38.sp
        isCompact -> 30.sp
        else -> 40.sp
    }
    val bankTileHeight = if (isCompact) 44.dp else 58.dp
    val bankFontSize = if (isCompact) 16.sp else 22.sp
    val controlHeight = if (isCompact) 44.dp else 52.dp

    LaunchedEffect(letter) {
        viewModel.init(letter)
    }

    DragAndDropContainer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { dragState ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(padding)
        ) {
            // Región izquierda: filas tutor (arriba) y niño (abajo)
            Column(
                modifier = Modifier
                    .weight(0.68f)
                    .fillMaxHeight()
            ) {
                FilledIconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(if (isCompact) 44.dp else 52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }

                Spacer(modifier = Modifier.weight(0.25f))

                // Fila superior (Tutor): dos cajas contiguas + botón para oír la palabra
                Column(horizontalAlignment = Alignment.Start) {
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        uiState.tutorSlots.forEach { slot ->
                            SyllableSlotComposable(
                                slot = slot,
                                width = slotWidth,
                                height = slotHeight,
                                fontSize = slotFontSize,
                                onTap = { viewModel.onTutorSlotTap(slot.index) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.speakTutorWord() },
                        modifier = Modifier.height(controlHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(
                            text = "🔊 palabra",
                            fontSize = if (isCompact) 14.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Fila inferior (Niño): dos cajas separadas con borde punteado
                Row(horizontalArrangement = Arrangement.spacedBy(if (isCompact) 24.dp else 48.dp)) {
                    uiState.childSlots.forEach { slot ->
                        SyllableSlotComposable(
                            slot = slot,
                            width = slotWidth,
                            height = slotHeight,
                            fontSize = slotFontSize,
                            onTap = null
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.5f))
            }

            // Región derecha: banco de sílabas (arriba) + controles (abajo)
            Column(
                modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
                    .padding(if (isCompact) 6.dp else 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    uiState.bank.forEach { tile ->
                        DraggableSyllableComposable(
                            tile = tile,
                            height = bankTileHeight,
                            fontSize = bankFontSize,
                            onDropped = { targetId -> routeDrop(targetId, tile.syllable, viewModel) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.onRandom() },
                        modifier = Modifier
                            .weight(1f)
                            .height(controlHeight),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "🎲", fontSize = if (isCompact) 18.sp else 22.sp)
                    }
                    Button(
                        onClick = { viewModel.onClearAll() },
                        modifier = Modifier
                            .weight(1f)
                            .height(controlHeight),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(text = "🔄", fontSize = if (isCompact) 18.sp else 22.sp)
                    }
                }
            }
        }

        // Sílaba "fantasma" que sigue al dedo mientras se arrastra
        if (dragState.isDragging && dragState.dragItem is SyllableTile) {
            val tile = dragState.dragItem as SyllableTile
            val density = LocalDensity.current
            val localPos = dragState.currentDragLocalPosition
            val halfW = with(density) { slotWidth.toPx() / 2f }
            val halfH = with(density) { bankTileHeight.toPx() / 2f }

            ElevatedCard(
                modifier = Modifier
                    .size(width = slotWidth, height = bankTileHeight)
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
                        fontSize = bankFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Celebración superpuesta y NO bloqueante (no captura toques)
        if (uiState.showCelebration) {
            val entrance = remember { Animatable(0.6f) }
            LaunchedEffect(Unit) {
                entrance.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(entrance.value)
            ) {
                Text(
                    text = "⭐🎉⭐",
                    fontSize = if (isCompact) 48.sp else 72.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

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
    width: Dp,
    height: Dp,
    fontSize: TextUnit,
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

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .onGloballyPositioned {
                dragAndDropState.registerTarget(slot.id, it.boundsInWindow())
            }
            .clip(shape)
            .background(fill)
            .then(borderModifier)
            .then(if (onTap != null) Modifier.clickable { onTap() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = slot.syllable ?: "",
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun DraggableSyllableComposable(
    tile: SyllableTile,
    height: Dp,
    fontSize: TextUnit,
    onDropped: (String) -> Unit
) {
    val dragAndDropState = LocalDragAndDropState.current
    var positionInWindow by remember { mutableStateOf(Offset.Zero) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = tile.syllable,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
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
