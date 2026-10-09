package com.trazavoz.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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

internal data class GameDragPiece(val piece: PieceItem, val width: Dp, val height: Dp)

@Composable
fun GameScreen(
    wordId: Int,
    viewModel: GameViewModel,
    @Suppress("UNUSED_PARAMETER") ttsManager: TrazavozTtsManager,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    LockLandscapeOrientation()
    val window = rememberWindowInfo()
    val compact = window.isCompactHeight
    val back = { viewModel.leaveGame(); onBackClick() }
    BackHandler(onBack = back)
    LaunchedEffect(wordId) { viewModel.startNewGame(wordId) }
    DisposableEffect(viewModel) { onDispose { viewModel.leaveGame() } }

    LiteracyGameTheme {
        DragAndDropContainer(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { drag ->
            LaunchedEffect(state.boardKey, state.currentPhase) { drag.cancelDrag() }
            Column(Modifier.fillMaxSize()) {
                ScreenHeader(
                    title = phaseTitle(state.currentPhase),
                    onBackClick = back,
                    windowInfo = window
                )
                Row(
                    Modifier.fillMaxWidth().weight(1f).padding(if (compact) 8.dp else 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 24.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(if (compact) 0.32f else 0.28f).fillMaxHeight(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        state.word?.let { word ->
                            LeftAnchorPanel(word, compact, state.currentPhase != GamePhase.SYLLABLES_SUCCESS, viewModel::speakReference)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(if (compact) 0.68f else 0.72f).fillMaxHeight(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        GameBoardPanel(state, viewModel, compact, { viewModel.startNewGame(wordId) }, back)
                    }
                }
            }
            (drag.dragItem as? GameDragPiece)?.takeIf { drag.isDragging }?.let { lifted ->
                val density = LocalDensity.current
                val halfWidth = with(density) { lifted.width.toPx() / 2 }
                val halfHeight = with(density) { lifted.height.toPx() / 2 }
                val position = drag.currentDragLocalPosition
                PieceTile(
                    lifted.piece.text,
                    Modifier.size(lifted.width, lifted.height).offset {
                        IntOffset((position.x - halfWidth).roundToInt(), (position.y - halfHeight).roundToInt())
                    }.alpha(0.9f).clearAndSetSemantics { },
                    selected = true
                )
            }
        }
    }
}

@Composable
internal fun GameBoardPanel(state: GameUiState, viewModel: GameViewModel, compact: Boolean, onReplay: () -> Unit, onBack: () -> Unit) {
    val transition = updateTransition(state, label = "game_board")
    // Immutable snapshots keep outgoing content on its original board.
    // Partial success shares its key with syllables, avoiding duplicate targets.
    transition.AnimatedContent(
        contentKey = { it.boardKey },
        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
        modifier = Modifier.fillMaxSize()
    ) { snapshot ->
        val active = snapshot.isInteractive && snapshot.boardKey == state.boardKey &&
            transition.currentState.boardKey == transition.targetState.boardKey
        when (snapshot.currentPhase) {
            GamePhase.SYLLABLES, GamePhase.SYLLABLES_SUCCESS, GamePhase.LETTERS ->
                ActionAreaPanel(snapshot, viewModel, compact, active)
            GamePhase.COMPLETED -> CompletionPanel(snapshot, compact, onReplay, onBack)
            GamePhase.ERROR -> MessagePanel(snapshot.errorMessage.orEmpty(), onReplay, onBack)
            GamePhase.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Preparando la palabra…", Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        }
    }
}

private fun phaseTitle(phase: GamePhase): String = when (phase) {
    GamePhase.SYLLABLES, GamePhase.SYLLABLES_SUCCESS -> "FASE 1: SÍLABAS"
    GamePhase.LETTERS, GamePhase.COMPLETED -> "FASE 2: LETRAS INDIVIDUALES"
    GamePhase.ERROR -> "Preparar palabra"
    GamePhase.LOADING -> "Cargando…"
}

@Composable
private fun LeftAnchorPanel(word: Word, compact: Boolean, audioEnabled: Boolean, onListen: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(if (compact) 8.dp else 20.dp)) {
        val imageSize = minOf(maxWidth, if (compact) 120.dp else 220.dp)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(imageSize).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .clickable(enabled = audioEnabled, role = Role.Button, onClickLabel = "Escuchar ${word.text}", onClick = onListen)
                    .semantics(mergeDescendants = true) { contentDescription = "Imagen de ${word.text}. Escuchar palabra" }
            ) {
                AsyncImage(
                    model = word.localImagePath?.let(::File) ?: word.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(12.dp)
                )
                IconButton(
                    onClick = onListen,
                    enabled = audioEnabled,
                    modifier = Modifier.align(Alignment.BottomEnd).size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Escuchar ${word.text}")
                }
            }
            Spacer(Modifier.height(if (compact) 8.dp else 24.dp))
            Text(word.text, fontSize = if (compact) 28.sp else 44.sp, lineHeight = if (compact) 36.sp else 56.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Toca la imagen para escuchar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ActionAreaPanel(state: GameUiState, viewModel: GameViewModel, compact: Boolean, enabled: Boolean) {
    val syllables = state.boardKey.phase == GamePhase.SYLLABLES
    BoxWithConstraints(Modifier.fillMaxSize().padding(if (compact) 8.dp else 20.dp)) {
        val fontScale = LocalDensity.current.fontScale
        val longest = state.piecesToPlace.maxOfOrNull { it.text.length } ?: 1
        val textWidth = longest * 26 * fontScale
        val tileWidth = minOf(maxWidth, maxOf(if (compact) 64.dp else 88.dp, (textWidth + 24).dp))
        val lines = kotlin.math.ceil(textWidth / maxOf(1f, tileWidth.value - 8)).toInt().coerceAtLeast(1)
        val tileHeight = maxOf(if (compact) 64.dp else 88.dp, (lines * 40 * fontScale + 16).dp)
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (syllables) "Arrastra las sílabas a los espacios vacíos" else "Ordena las letras para formar la palabra",
                fontSize = if (compact) 16.sp else 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            Text(
                if (state.currentPhase == GamePhase.SYLLABLES_SUCCESS) "¡Muy bien! Ahora vamos con las letras." else "Arrastra una ficha y suéltala en su espacio.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = if (state.currentPhase == GamePhase.SYLLABLES_SUCCESS) SuccessColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp).semantics { liveRegion = LiveRegionMode.Polite }
            )
            var viewport by remember(state.boardKey) { mutableStateOf(Rect.Zero) }
            Box(
                Modifier.fillMaxWidth().weight(1f).onGloballyPositioned { viewport = it.boundsInWindow() }
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                FlowRow(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.targetSlots.forEachIndexed { index, slot ->
                        key(slot.id) {
                            if (slot.separatorBefore.isNotEmpty()) Separator(slot.separatorBefore, tileHeight)
                            DropSlotComposable(
                                slot, tileWidth, tileHeight, (index + 1).toString(), enabled, viewport
                            )
                        }
                    }
                    if (state.trailingSeparator.isNotEmpty()) Separator(state.trailingSeparator, tileHeight)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth().weight(0.85f),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TileBorder)
            ) {
                FlowRow(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.piecesToPlace.forEach { piece ->
                        key(piece.id) {
                            DraggablePieceComposable(piece, tileWidth, tileHeight, enabled, viewModel)
                        }
                    }
                }
            }
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = ConsonantColor)) { append("● Consonantes (Azul)") }
                    append("   ")
                    withStyle(SpanStyle(color = VowelColor)) { append("● Vocales (Rojo)") }
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun Separator(text: String, height: Dp) {
    Box(Modifier.height(height).widthIn(min = 12.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 22.sp, modifier = Modifier.semantics { contentDescription = "Separador" })
    }
}

@Composable
internal fun DropSlotComposable(
    slot: PieceSlot,
    width: Dp,
    height: Dp,
    label: String,
    enabled: Boolean,
    viewport: Rect
) {
    val drag = LocalDragAndDropState.current
    val owner = remember(slot.id) { Any() }
    var bounds by remember(slot.id) { mutableStateOf(Rect.Zero) }
    SideEffect {
        if (enabled && bounds.overlaps(viewport)) drag.registerTarget(slot.id, bounds.intersect(viewport), owner)
        else drag.unregisterTarget(slot.id, owner)
    }
    DisposableEffect(slot.id, owner) { onDispose { drag.unregisterTarget(slot.id, owner) } }
    val placed = slot.placedPiece
    val focused = enabled && drag.isDragging && bounds.intersect(viewport).contains(drag.currentDragAbsolutePosition)
    val border = when { placed != null -> SuccessColor; focused -> ConsonantColor; else -> TargetBorder }
    Box(
        Modifier.size(width, height).testTag("slot_${slot.id}").onGloballyPositioned { bounds = it.boundsInWindow() }
            .background(if (focused) ActiveTargetFill else TargetFill, RoundedCornerShape(16.dp))
            .drawBehind {
                val stroke = 2.dp.toPx()
                drawRoundRect(
                    color = border,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(stroke, pathEffect = if (placed == null && !focused) PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx())) else null)
                )
            }
            .semantics(mergeDescendants = true) {
                contentDescription = if (placed == null) "Espacio $label vacío" else "Espacio $label: ${placed.text}, completado"
            },
        contentAlignment = Alignment.Center
    ) {
        if (placed != null) {
            Text(buildPieceAnnotatedString(placed.text), fontSize = 30.sp, lineHeight = 40.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("_", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DraggablePieceComposable(
    piece: PieceItem,
    width: Dp,
    height: Dp,
    enabled: Boolean,
    viewModel: GameViewModel
) {
    val drag = LocalDragAndDropState.current
    var center by remember(piece.id) { mutableStateOf(Offset.Zero) }
    val lifted = (drag.dragItem as? GameDragPiece)?.piece?.id == piece.id && drag.isDragging
    val available = enabled && !piece.isPlaced
    PieceTile(
        piece.text,
        Modifier.size(width, height).testTag("piece_${piece.id}").alpha(if (piece.isPlaced || lifted) 0f else 1f)
            .onGloballyPositioned { center = it.boundsInWindow().center }
            .pointerInput(piece.id, available, width, height) {
                if (!available) return@pointerInput
                detectDragGestures(
                    onDragStart = { drag.onDragStart(GameDragPiece(piece, width, height), center) },
                    onDragEnd = { (drag.onDragEnd() as? String)?.let { viewModel.onItemDropped(piece, it) } },
                    onDragCancel = drag::cancelDrag,
                    onDrag = { change, amount -> change.consume(); drag.onDrag(amount) }
                )
            }
            .semantics {
                if (piece.isPlaced) invisibleToUser()
                contentDescription = "Ficha ${piece.text}"
                stateDescription = if (available) "Arrastra la ficha a un espacio" else "No disponible"
            },
        selected = false
    )
}

@Composable
private fun PieceTile(text: String, modifier: Modifier, selected: Boolean) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, if (selected) ConsonantColor else TileBorder),
        shadowElevation = if (selected) 6.dp else 2.dp
    ) {
        Box(Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
            Text(buildPieceAnnotatedString(text), fontSize = 30.sp, lineHeight = 40.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompletionPanel(state: GameUiState, compact: Boolean, onReplay: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Excelente!", fontSize = if (compact) 26.sp else 36.sp, fontWeight = FontWeight.Bold, color = SuccessColor,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Text("Armaste la palabra: ${state.word?.text.orEmpty()}", textAlign = TextAlign.Center)
        state.saveError?.let { Text(it, Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite }, textAlign = TextAlign.Center) }
        Spacer(Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onReplay, modifier = Modifier.heightIn(min = 48.dp)) { Text("Jugar de nuevo") }
            OutlinedButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Salir") }
        }
    }
}

@Composable
private fun MessagePanel(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message, textAlign = TextAlign.Center, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Button(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) { Text("Reintentar") }
        OutlinedButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Volver") }
    }
}
