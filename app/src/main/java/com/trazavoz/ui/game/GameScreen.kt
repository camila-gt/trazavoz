package com.trazavoz.ui.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trazavoz.domain.model.Word
import com.trazavoz.ui.audio.TrazavozTtsManager
import com.trazavoz.ui.components.DragAndDropContainer
import com.trazavoz.ui.components.LocalDragAndDropState
import com.trazavoz.ui.components.LockLandscapeOrientation
import com.trazavoz.ui.components.ScreenHeader
import com.trazavoz.ui.theme.rememberWindowInfo
import java.io.File
import kotlin.math.roundToInt

// Colores fijos para vocales y consonantes (texto)
val VowelColor = Color(0xFFD32F2F)
val ConsonantColor = Color(0xFF1976D2)

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
    val isCompact = windowInfo.isCompactHeight

    LaunchedEffect(wordId) {
        viewModel.startNewGame(wordId)
    }

    DragAndDropContainer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { dragState ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ScreenHeader(
                title = when (uiState.currentPhase) {
                    GamePhase.SYLLABLES, GamePhase.SYLLABLES_SUCCESS -> "FASE 1: SÍLABAS"
                    GamePhase.LETTERS, GamePhase.COMPLETED -> "FASE 2: LETRAS INDIVIDUALES"
                    else -> "Cargando..."
                },
                onBackClick = onBackClick,
                windowInfo = windowInfo
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Panel Izquierdo: Ancla visual (Imagen y Palabra estática)
                Box(
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    uiState.word?.let { word ->
                        LeftAnchorPanel(word = word, ttsManager = ttsManager, isCompact = isCompact)
                    }
                }

                // Panel Derecho: Zona de acción (Huecos y Fichas)
                Box(
                    modifier = Modifier
                        .weight(0.72f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = uiState.currentPhase,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                        },
                        label = "game_phase_transition"
                    ) { phase ->
                        when (phase) {
                            GamePhase.SYLLABLES, GamePhase.SYLLABLES_SUCCESS, GamePhase.LETTERS -> {
                                ActionAreaPanel(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    ttsManager = ttsManager,
                                    isCompact = isCompact,
                                    phase = phase
                                )
                            }
                            GamePhase.COMPLETED -> {
                                CompletionPanel(
                                    wordText = uiState.word?.text ?: "",
                                    onReplayClick = { viewModel.startNewGame(wordId) },
                                    onBackClick = onBackClick
                                )
                            }
                            GamePhase.ERROR -> {
                                Text("Error al cargar la palabra.", fontSize = 24.sp, color = MaterialTheme.colorScheme.error)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // Ficha flotante al arrastrar
        if (dragState.isDragging && dragState.dragItem is PieceItem) {
            val draggedPiece = dragState.dragItem as PieceItem
            val density = LocalDensity.current
            val localPos = dragState.currentDragLocalPosition
            val pieceWidth = if (uiState.currentPhase == GamePhase.SYLLABLES) 120.dp else 80.dp
            val pieceHeight = 80.dp
            
            val halfWidth = with(density) { pieceWidth.toPx() / 2f }
            val halfHeight = with(density) { pieceHeight.toPx() / 2f }

            ElevatedCard(
                modifier = Modifier
                    .size(width = pieceWidth, height = pieceHeight)
                    .offset {
                        IntOffset(
                            (localPos.x - halfWidth).roundToInt(),
                            (localPos.y - halfHeight).roundToInt()
                        )
                    }
                    .alpha(0.85f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = buildPieceAnnotatedString(draggedPiece.text),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun LeftAnchorPanel(
    word: Word,
    ttsManager: TrazavozTtsManager,
    isCompact: Boolean
) {
    val imageSize = if (isCompact) 140.dp else 200.dp
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { ttsManager.hablarPalabra(word.text) },
                contentAlignment = Alignment.Center
            ) {
                val imageSource = if (word.localImagePath != null) File(word.localImagePath) else word.imageUrl
                AsyncImage(
                    model = imageSource,
                    contentDescription = "Toca para escuchar",
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
                // Ícono de altavoz sutil
                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = word.text.uppercase(),
                fontSize = if (isCompact) 32.sp else 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Toca la imagen para escuchar",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ActionAreaPanel(
    uiState: GameUiState,
    viewModel: GameViewModel,
    ttsManager: TrazavozTtsManager,
    isCompact: Boolean,
    phase: GamePhase
) {
    val isSyllablePhase = phase == GamePhase.SYLLABLES || phase == GamePhase.SYLLABLES_SUCCESS
    val titleText = if (isSyllablePhase) "Arrastra las sílabas a los espacios vacíos" else "Ordena las letras para formar la palabra"
    
    val slotWidth = if (isSyllablePhase) (if(isCompact) 100.dp else 140.dp) else (if(isCompact) 60.dp else 80.dp)
    val slotHeight = if (isCompact) 70.dp else 90.dp

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = titleText,
            fontSize = if (isCompact) 18.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // ZONA DE HUECOS
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            uiState.targetSlots.forEachIndexed { index, slot ->
                DropSlotComposable(
                    slot = slot,
                    width = slotWidth,
                    height = slotHeight,
                    label = (index + 1).toString(),
                    onItemDropped = { piece -> viewModel.onItemDropped(piece, slot.id) }
                )
            }
        }
        
        // ZONA DE FICHAS (BANDEJA)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(slotHeight + 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.piecesToPlace.forEach { piece ->
                    DraggablePieceComposable(
                        piece = piece,
                        width = slotWidth,
                        height = slotHeight,
                        viewModel = viewModel,
                        ttsManager = ttsManager
                    )
                }
            }
        }
        
        // Leyenda de colores
        Row(
            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(ConsonantColor))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Consonantes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(VowelColor))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Vocales", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun DropSlotComposable(
    slot: PieceSlot,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    label: String,
    onItemDropped: (PieceItem) -> Unit
) {
    val dragAndDropState = LocalDragAndDropState.current
    var bounds by remember { mutableStateOf(Rect.Zero) }

    DisposableEffect(slot.id) {
        onDispose { dragAndDropState.unregisterTarget(slot.id) }
    }

    val isFilled = slot.placedPiece != null
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .onGloballyPositioned {
                bounds = it.boundsInWindow()
                dragAndDropState.registerTarget(slot.id, bounds)
            }
            .background(if (isFilled) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(12.dp))
            .then(
                if (isFilled) Modifier.border(2.dp, Color.LightGray.copy(alpha=0.5f), RoundedCornerShape(12.dp))
                else Modifier.border(2.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Text(
                text = buildPieceAnnotatedString(slot.placedPiece!!.text),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
        } else {
            // Dibujar el borde punteado y la etiqueta si está vacío
            Box(
                modifier = Modifier.matchParentSize().border(2.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            )
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Divider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 24.dp)
                    .width(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                thickness = 2.dp
            )
        }
    }
}

@Composable
fun DraggablePieceComposable(
    piece: PieceItem,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    viewModel: GameViewModel,
    ttsManager: TrazavozTtsManager
) {
    val dragAndDropState = LocalDragAndDropState.current
    var positionInWindow by remember { mutableStateOf(Offset.Zero) }

    val isPlaced = piece.isPlaced
    val isBeingDragged = dragAndDropState.isDragging && dragAndDropState.dragItem == piece

    ElevatedCard(
        modifier = Modifier
            .size(width = width, height = height)
            .alpha(if (isPlaced || isBeingDragged) 0f else 1f)
            .onGloballyPositioned { positionInWindow = it.boundsInWindow().center }
            .pointerInput(isPlaced) {
                if (isPlaced) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        dragAndDropState.onDragStart(piece, positionInWindow)
                    },
                    onDragEnd = {
                        val target = dragAndDropState.onDragEnd()
                        if (target is String) {
                            viewModel.onItemDropped(piece, target)
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
            }
            .clickable(enabled = !isPlaced) {
                // Alternativa de accesibilidad o para escuchar
                if (piece.text.length == 1) ttsManager.hablarLetra(piece.text.first())
                else ttsManager.hablarSilaba(piece.text)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = buildPieceAnnotatedString(piece.text),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CompletionPanel(
    wordText: String,
    onReplayClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Excelente!",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Armaste la palabra: $wordText",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Button(onClick = onReplayClick) {
                Icon(Icons.Filled.Replay, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Jugar de nuevo")
            }
            OutlinedButton(onClick = onBackClick) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Volver al inicio")
            }
        }
    }
}

fun buildPieceAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        text.forEach { char ->
            val isVowel = char.uppercaseChar() in listOf('A', 'E', 'I', 'O', 'U', 'Á', 'É', 'Í', 'Ó', 'Ú', 'Ü')
            withStyle(SpanStyle(color = if (isVowel) VowelColor else ConsonantColor)) {
                append(char.toString())
            }
        }
    }
}
