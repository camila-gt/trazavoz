package com.trazavoz.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.trazavoz.ui.audio.TrazavozTtsManager
import com.trazavoz.ui.components.ConfettiOverlay
import com.trazavoz.ui.components.DragAndDropContainer
import com.trazavoz.ui.components.DragAndDropState
import com.trazavoz.ui.components.LocalDragAndDropState
import com.trazavoz.ui.components.LockLandscapeOrientation
import com.trazavoz.ui.components.ScreenHeader
import com.trazavoz.ui.theme.colorForLetter
import com.trazavoz.ui.theme.rememberWindowInfo
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    wordId: Int,
    viewModel: GameViewModel,
    ttsManager: TrazavozTtsManager,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LockLandscapeOrientation()

    val windowInfo = rememberWindowInfo()
    val screenHeight = windowInfo.screenHeightDp
    val isCompact = windowInfo.isCompactHeight

    // La pantalla está bloqueada en horizontal (ver LockLandscapeOrientation):
    // isCompact distingue teléfono (alto reducido) de tablet (alto amplio).
    val imageSize = if (isCompact) min(screenHeight * 0.4f, 160.dp) else 220.dp
    val slotSize = if (isCompact) min(screenHeight * 0.18f, 60.dp) else 80.dp
    val letterSize = if (isCompact) min(screenHeight * 0.18f, 60.dp) else 80.dp
    val letterTrayHeight = if (isCompact) min(screenHeight * 0.22f, 80.dp) else 110.dp
    val slotFontSize = if (isCompact) 24.sp else 36.sp
    val syllableBtnHeight = if (isCompact) 40.dp else 60.dp
    val syllableFontSize = if (isCompact) 16.sp else 22.sp
    val headerBtnHeight = if (isCompact) 44.dp else 60.dp
    val headerBtnWidth = if (isCompact) 90.dp else 120.dp
    val padding = if (isCompact) 8.dp else 16.dp

    LaunchedEffect(wordId) {
        viewModel.startNewGame(wordId)
    }

    DragAndDropContainer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { dragState ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScreenHeader(
                title = "¡Arma la palabra!",
                onBackClick = onBackClick,
                windowInfo = windowInfo,
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(height = headerBtnHeight, width = headerBtnWidth)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⭐ ${uiState.errorsCount}",
                            fontSize = if (isCompact) 16.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            )

            uiState.word?.let { word ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = if (isCompact) 4.dp else 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WordImageAndSyllables(word, imageSize, syllableBtnHeight, syllableFontSize, ttsManager, isCompact)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.targetSlots.forEachIndexed { idx, slot ->
                            DropSlotComposable(
                                slot = slot,
                                slotSize = slotSize,
                                fontSize = slotFontSize,
                                onItemDropped = { letter ->
                                    viewModel.onItemDropped(letter, slot.id)
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(letterTrayHeight)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(if (isCompact) 4.dp else 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.piecesToPlace.forEach { letter ->
                        DraggableLetterComposable(
                            piece = letter,
                            letterSize = letterSize,
                            fontSize = slotFontSize,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        if (dragState.isDragging && dragState.dragItem is PieceItem) {
            val draggedpiece = dragState.dragItem as PieceItem
            val density = LocalDensity.current
            val localPos = dragState.currentDragLocalPosition
            val halfSize = with(density) { letterSize.toPx() / 2f }

            ElevatedCard(
                modifier = Modifier
                    .size(letterSize)
                    .offset {
                        IntOffset(
                            (localPos.x - halfSize).roundToInt(),
                            (localPos.y - halfSize).roundToInt()
                        )
                    }
                    .alpha(0.85f),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.elevatedCardColors(containerColor = colorForLetter(draggedpiece.text))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = draggedpiece.text,
                        fontSize = slotFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (uiState.showCelebration) {
            CelebrationDialog(
                wordText = uiState.word?.text ?: "",
                onBackClick = onBackClick,
                onReplayClick = {
                    viewModel.startNewGame(uiState.word?.id ?: 0)
                }
            )
        }
    }
}

@Composable
fun WordImageAndSyllables(
    word: com.trazavoz.domain.model.Word,
    imageSize: androidx.compose.ui.unit.Dp,
    syllableBtnHeight: androidx.compose.ui.unit.Dp,
    syllableFontSize: androidx.compose.ui.unit.TextUnit,
    ttsManager: TrazavozTtsManager,
    isCompact: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .size(imageSize)
                .border(4.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val imageSource = if (word.localImagePath != null) {
                    File(word.localImagePath)
                } else {
                    word.imageUrl
                }
                AsyncImage(
                    model = imageSource,
                    contentDescription = word.text,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 12.dp)
        ) {
            word.syllables.forEach { syllable ->
                Button(
                    onClick = { ttsManager.hablarSilaba(syllable) },
                    modifier = Modifier.height(syllableBtnHeight),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = syllable,
                        fontSize = syllableFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun DropSlotComposable(
    slot: PieceSlot,
    slotSize: androidx.compose.ui.unit.Dp = 80.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 36.sp,
    onItemDropped: (PieceItem) -> Unit
) {
    val dragAndDropState = LocalDragAndDropState.current
    var bounds by remember { mutableStateOf(Rect.Zero) }

    DisposableEffect(slot.id) {
        onDispose {
            dragAndDropState.unregisterTarget(slot.id)
        }
    }

    Box(
        modifier = Modifier
            .size(slotSize)
            .onGloballyPositioned {
                bounds = it.boundsInWindow()
                dragAndDropState.registerTarget(slot.id, bounds)
            }
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (slot.placedPiece != null) colorForLetter(slot.placedPiece.char) else Color.LightGray.copy(
                    alpha = 0.4f
                )
            )
            .border(
                width = 3.dp,
                color = if (slot.placedPiece != null) colorForLetter(slot.placedPiece.char) else MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        if (slot.placedPiece != null) {
            Text(
                text = slot.placedPiece.char.toString(),
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Text(
                text = "_",
                fontSize = fontSize * 0.9f,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun DraggableLetterComposable(
    piece: PieceItem,
    letterSize: androidx.compose.ui.unit.Dp = 80.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 36.sp,
    viewModel: GameViewModel
) {
    val dragAndDropState = LocalDragAndDropState.current
    val coroutineScope = rememberCoroutineScope()
    var positionInWindow by remember { mutableStateOf(Offset.Zero) }

    val isPlaced = letter.isPlaced
    val isBeingDragged = dragAndDropState.isDragging && dragAndDropState.dragItem == letter

    ElevatedCard(
        modifier = Modifier
            .size(letterSize)
            .alpha(if (isPlaced || isBeingDragged) 0.2f else 1f)
            .onGloballyPositioned {
                positionInWindow = it.boundsInWindow().center
            }
            .pointerInput(isPlaced) {
                if (isPlaced) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        dragAndDropState.onDragStart(letter, positionInWindow)
                    },
                    onDragEnd = {
                        val target = dragAndDropState.onDragEnd()
                        if (target is String) {
                            viewModel.onItemDropped(letter, target)
                        }
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
        colors = CardDefaults.elevatedCardColors(containerColor = colorForLetter(piece.text.first()))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = piece.text,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CelebrationDialog(
    wordText: String,
    onBackClick: () -> Unit,
    onReplayClick: () -> Unit
) {
    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight
    val starIconSize = when {
        isCompact -> 26.dp
        windowInfo.isExpandedWidth -> 44.dp
        else -> 34.dp
    }
    val dialogTitleSize = when {
        isCompact -> 18.sp
        windowInfo.isExpandedWidth -> 26.sp
        else -> 21.sp
    }
    val dialogSubtitleSize = when {
        isCompact -> 14.sp
        windowInfo.isExpandedWidth -> 18.sp
        else -> 15.sp
    }
    val actionButtonSize = if (isCompact) 56.dp else 64.dp
    val actionIconSize = if (isCompact) 26.dp else 30.dp

    val entrance = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Confetti cayendo sobre toda la pantalla, detrás de la tarjeta.
            ConfettiOverlay(modifier = Modifier.fillMaxSize())

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(if (isCompact) 0.6f else 0.5f)
                    .widthIn(max = 520.dp)
                    .padding(if (isCompact) 8.dp else 16.dp)
                    .scale(entrance.value)
                    .alpha(entrance.value),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isCompact) 12.dp else 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = if (isCompact) 8.dp else 16.dp)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(starIconSize))
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(starIconSize * 1.25f))
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(starIconSize))
                    }

                    Text(
                        text = "¡Excelente trabajo!",
                        fontSize = dialogTitleSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

                    Text(
                        text = "Armaste la palabra: $wordText",
                        fontSize = dialogSubtitleSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilledIconButton(
                            onClick = onReplayClick,
                            modifier = Modifier.size(actionButtonSize),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Replay,
                                contentDescription = "Jugar de nuevo",
                                modifier = Modifier.size(actionIconSize)
                            )
                        }

                        FilledIconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(actionButtonSize),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Salir al menú",
                                modifier = Modifier.size(actionIconSize)
                            )
                        }
                    }
                }
            }
        }
    }
}
