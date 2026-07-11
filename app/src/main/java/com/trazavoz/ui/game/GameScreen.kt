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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.trazavoz.ui.audio.TrazavozTtsManager
import com.trazavoz.ui.components.DragAndDropContainer
import com.trazavoz.ui.components.DragAndDropState
import com.trazavoz.ui.components.LocalDragAndDropState
import com.trazavoz.ui.theme.CelestePastel
import com.trazavoz.ui.theme.CoralPastel
import com.trazavoz.ui.theme.VerdeManzanaPastel
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

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val screenWidth = config.screenWidthDp.dp
    val isCompact = screenHeight < 400.dp
    val isLandscape = screenWidth > screenHeight

    val imageSize = if (isCompact) min(screenHeight * 0.4f, 160.dp) else if (!isLandscape) min(screenWidth * 0.4f, 180.dp) else 220.dp
    val slotSize = if (isCompact) min(screenHeight * 0.18f, 60.dp) else if (!isLandscape) 60.dp else 80.dp
    val letterSize = if (isCompact) min(screenHeight * 0.18f, 60.dp) else if (!isLandscape) 60.dp else 80.dp
    val letterTrayHeight = if (isCompact) min(screenHeight * 0.22f, 80.dp) else if (!isLandscape) 90.dp else 110.dp
    val titleFontSize = if (isCompact) 22.sp else if (!isLandscape) 24.sp else 32.sp
    val slotFontSize = if (isCompact) 24.sp else if (!isLandscape) 28.sp else 36.sp
    val syllableBtnHeight = if (isCompact) 40.dp else if (!isLandscape) 44.dp else 60.dp
    val syllableFontSize = if (isCompact) 16.sp else if (!isLandscape) 18.sp else 22.sp
    val headerBtnHeight = if (isCompact) 44.dp else if (!isLandscape) 48.dp else 60.dp
    val headerBtnWidth = if (isCompact) 90.dp else if (!isLandscape) 100.dp else 120.dp
    val padding = if (isCompact) 8.dp else if (!isLandscape) 12.dp else 16.dp

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.size(width = headerBtnWidth, height = headerBtnHeight),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
                ) {
                    Text("Volver", fontSize = if (isCompact) 14.sp else 18.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "¡Arma la palabra!",
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier
                        .size(height = headerBtnHeight, width = headerBtnWidth)
                        .clip(CircleShape)
                        .background(VerdeManzanaPastel),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⭐ ${uiState.errorsCount}",
                        fontSize = if (isCompact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

            uiState.word?.let { word ->
                if (isLandscape) {
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
                                    onLetterDropped = { letter ->
                                        viewModel.onLetterDropped(letter, idx)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WordImageAndSyllables(word, imageSize, syllableBtnHeight, syllableFontSize, ttsManager, isCompact)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            uiState.targetSlots.forEachIndexed { idx, slot ->
                                DropSlotComposable(
                                    slot = slot,
                                    slotSize = slotSize,
                                    fontSize = slotFontSize,
                                    onLetterDropped = { letter ->
                                        viewModel.onLetterDropped(letter, idx)
                                    }
                                )
                            }
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
                    uiState.lettersToPlace.forEach { letter ->
                        DraggableLetterComposable(
                            letter = letter,
                            letterSize = letterSize,
                            fontSize = slotFontSize,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        if (dragState.isDragging && dragState.dragItem is LetterItem) {
            val draggedLetter = dragState.dragItem as LetterItem
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
                colors = CardDefaults.elevatedCardColors(containerColor = CelestePastel)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = draggedLetter.char.toString(),
                        fontSize = slotFontSize,
                        fontWeight = FontWeight.ExtraBold,
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
                .border(4.dp, CelestePastel, MaterialTheme.shapes.large),
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
                    colors = ButtonDefaults.buttonColors(containerColor = CelestePastel)
                ) {
                    Text(
                        text = syllable,
                        fontSize = syllableFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DropSlotComposable(
    slot: SlotItem,
    slotSize: androidx.compose.ui.unit.Dp = 80.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 36.sp,
    onLetterDropped: (LetterItem) -> Unit
) {
    val dragAndDropState = LocalDragAndDropState.current
    var bounds by remember { mutableStateOf(Rect.Zero) }

    DisposableEffect(slot.index) {
        onDispose {
            dragAndDropState.unregisterTarget(slot.index)
        }
    }

    Box(
        modifier = Modifier
            .size(slotSize)
            .onGloballyPositioned {
                bounds = it.boundsInWindow()
                dragAndDropState.registerTarget(slot.index, bounds)
            }
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (slot.placedLetter != null) CelestePastel else Color.LightGray.copy(
                    alpha = 0.4f
                )
            )
            .border(
                width = 3.dp,
                color = if (slot.placedLetter != null) CelestePastel else Color.Gray,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        if (slot.placedLetter != null) {
            Text(
                text = slot.placedLetter.char.toString(),
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        } else {
            Text(
                text = "_",
                fontSize = fontSize * 0.9f,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun DraggableLetterComposable(
    letter: LetterItem,
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
                        if (target is Int) {
                            viewModel.onLetterDropped(letter, target)
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
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter.char.toString(),
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
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
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val isCompact = screenHeight < 400.dp

    Dialog(
        onDismissRequest = {}
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(if (isCompact) 0.85f else 1f)
                .padding(if (isCompact) 8.dp else 16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompact) 12.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⭐⭐⭐",
                    fontSize = if (isCompact) 32.sp else 48.sp,
                    modifier = Modifier.padding(bottom = if (isCompact) 8.dp else 16.dp)
                )

                Text(
                    text = "¡Excelente trabajo!",
                    fontSize = if (isCompact) 22.sp else 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

                Text(
                    text = "Armaste la palabra: $wordText",
                    fontSize = if (isCompact) 16.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onReplayClick,
                        modifier = Modifier.height(if (isCompact) 44.dp else 60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CelestePastel)
                    ) {
                        Text(
                            text = "Jugar de nuevo",
                            fontSize = if (isCompact) 14.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.height(if (isCompact) 44.dp else 60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
                    ) {
                        Text(
                            text = "Salir al menú",
                            fontSize = if (isCompact) 14.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
