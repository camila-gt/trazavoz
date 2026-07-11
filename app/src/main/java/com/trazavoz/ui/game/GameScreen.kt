package com.trazavoz.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.trazavoz.ui.audio.TrazavozTtsManager
import com.trazavoz.ui.components.DragAndDropContainer
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

    LaunchedEffect(wordId) {
        viewModel.startNewGame(wordId)
    }

    DragAndDropContainer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.size(width = 120.dp, height = 60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
                ) {
                    Text("Volver", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "¡Arma la Palabra!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier
                        .size(height = 60.dp, width = 120.dp)
                        .clip(CircleShape)
                        .background(VerdeManzanaPastel),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⭐ ${uiState.errorsCount}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

            uiState.word?.let { word ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .size(220.dp)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            word.syllables.forEach { syllable ->
                                Button(
                                    onClick = { ttsManager.hablarSilaba(syllable) },
                                    modifier = Modifier.height(60.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CelestePastel)
                                ) {
                                    Text(
                                        text = syllable,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.targetSlots.forEachIndexed { idx, slot ->
                            DropSlotComposable(slot = slot, onLetterDropped = { letter ->
                                viewModel.onLetterDropped(letter, idx)
                            })
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(uiState.lettersToPlace, key = { it.id }) { letter ->
                        DraggableLetterComposable(letter = letter)
                    }
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
fun DropSlotComposable(
    slot: SlotItem,
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
            .size(80.dp)
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
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        } else {
            Text(
                text = "_",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun DraggableLetterComposable(
    letter: LetterItem
) {
    val dragAndDropState = LocalDragAndDropState.current
    val coroutineScope = rememberCoroutineScope()
    var positionInWindow by remember { mutableStateOf(Offset.Zero) }

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val isPlaced = letter.isPlaced

    ElevatedCard(
        modifier = Modifier
            .size(80.dp)
            .alpha(if (isPlaced) 0.2f else 1f)
            .onGloballyPositioned {
                positionInWindow = it.boundsInWindow().center
            }
            .offset {
                IntOffset(
                    offsetX.value.roundToInt(),
                    offsetY.value.roundToInt()
                )
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
                            coroutineScope.launch {
                                offsetX.snapTo(0f)
                                offsetY.snapTo(0f)
                            }
                            onLetterDropped(dragAndDropState, letter, target)
                        } else {
                            coroutineScope.launch {
                                launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                                launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        dragAndDropState.isDragging = false
                        dragAndDropState.dragItem = null
                        coroutineScope.launch {
                            offsetX.animateTo(0f)
                            offsetY.animateTo(0f)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAndDropState.onDrag(dragAmount)
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
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
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun onLetterDropped(
    state: DragAndDropState,
    letter: LetterItem,
    slotIndex: Int
) {
    state.isDragging = false
    state.dragItem = null
}

@Composable
fun CelebrationDialog(
    wordText: String,
    onBackClick: () -> Unit,
    onReplayClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {}
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⭐⭐⭐",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "¡Excelente Trabajo!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Armaste la palabra: $wordText",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onReplayClick,
                        modifier = Modifier.height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CelestePastel)
                    ) {
                        Text(
                            text = "Jugar de nuevo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
                    ) {
                        Text(
                            text = "Salir al Menú",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
