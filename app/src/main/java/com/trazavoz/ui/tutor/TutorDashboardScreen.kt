package com.trazavoz.ui.tutor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.trazavoz.domain.model.Board
import com.trazavoz.domain.model.Word
import com.trazavoz.ui.components.ScreenHeader
import com.trazavoz.ui.components.minTouchTarget
import com.trazavoz.ui.theme.WindowInfo
import com.trazavoz.ui.theme.rememberWindowInfo
import java.io.File
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun TutorDashboardScreen(
    viewModel: TutorViewModel,
    onAddWordClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val allWords by viewModel.allWords.collectAsState()
    val allBoards by viewModel.allBoards.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight
    val padding = if (isCompact) 8.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
    ) {
        ScreenHeader(title = "Panel del tutor", onBackClick = onBackClick, windowInfo = windowInfo)

        Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Biblioteca", modifier = Modifier.padding(if (isCompact) 6.dp else 12.dp), fontWeight = FontWeight.Bold, fontSize = if (isCompact) 12.sp else 14.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Tableros", modifier = Modifier.padding(if (isCompact) 6.dp else 12.dp), fontWeight = FontWeight.Bold, fontSize = if (isCompact) 12.sp else 14.sp)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Estadísticas", modifier = Modifier.padding(if (isCompact) 6.dp else 12.dp), fontWeight = FontWeight.Bold, fontSize = if (isCompact) 12.sp else 14.sp)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("Ajustes PIN", modifier = Modifier.padding(if (isCompact) 6.dp else 12.dp), fontWeight = FontWeight.Bold, fontSize = if (isCompact) 12.sp else 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tutor-tabs"
            ) { tab ->
                when (tab) {
                    0 -> BibliotecaTab(allWords, onAddWordClick, onDeleteClick = { viewModel.deleteWord(it) }, viewModel = viewModel)
                    1 -> TablerosTab(allBoards, allWords, viewModel, windowInfo)
                    2 -> StatsTab(allLogs, allWords, onClearClick = { viewModel.clearStats() })
                    3 -> AjustesTab(onSavePin = { pin, callback -> viewModel.updatePin(pin, callback) })
                }
            }
        }
    }
}

@Composable
fun BibliotecaTab(
    words: List<Word>,
    onAddWordClick: () -> Unit,
    onDeleteClick: (Word) -> Unit,
    viewModel: TutorViewModel
) {
    var wordToEdit by remember { mutableStateOf<Word?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onAddWordClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Añadir nueva palabra", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (words.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay palabras en la biblioteca.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(words) { word ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                val imageSource = if (word.localImagePath != null) {
                                    File(word.localImagePath)
                                } else {
                                    word.imageUrl
                                }
                                AsyncImage(
                                    model = imageSource,
                                    contentDescription = word.text,
                                    modifier = Modifier.size(50.dp)
                                )
                                Column {
                                    Text(word.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    Text(word.syllables.joinToString(" - "), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { wordToEdit = word },
                                    modifier = Modifier.minTouchTarget(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Editar", color = MaterialTheme.colorScheme.onPrimary)
                                }
                                Button(
                                    onClick = { onDeleteClick(word) },
                                    modifier = Modifier.minTouchTarget(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    wordToEdit?.let { word ->
        EditWordDialog(
            word = word,
            viewModel = viewModel,
            onDismiss = { wordToEdit = null }
        )
    }
}

@Composable
fun TablerosTab(
    boards: List<Board>,
    allWords: List<Word>,
    viewModel: TutorViewModel,
    windowInfo: WindowInfo
) {
    var newBoardName by remember { mutableStateOf("") }
    var selectedBoardForEdit by remember { mutableStateOf<Board?>(null) }
    val boardWords = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(selectedBoardForEdit) {
        boardWords.clear()
        selectedBoardForEdit?.let { board ->
            viewModel.getBoardWithWords(board.id).firstOrNull()?.let { fullBoard ->
                fullBoard.words.forEach { word ->
                    boardWords[word.id] = true
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newBoardName,
                onValueChange = { newBoardName = it },
                label = { Text("Nombre del tablero") },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    viewModel.createBoard(newBoardName)
                    newBoardName = ""
                },
                modifier = Modifier.height(56.dp).minTouchTarget(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Crear", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val boardsListPane: @Composable () -> Unit = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(boards) { board ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBoardForEdit = board }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(board.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Button(
                                onClick = {
                                    if (selectedBoardForEdit?.id == board.id) {
                                        selectedBoardForEdit = null
                                    }
                                    viewModel.deleteBoard(board)
                                },
                                modifier = Modifier.minTouchTarget(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }
        }

        val wordsChecklistPane: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
                    .padding(12.dp)
            ) {
                val activeBoard = selectedBoardForEdit
                if (activeBoard == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Selecciona un tablero para editar sus palabras", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Palabras en: ${activeBoard.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (allWords.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Agrega palabras a la biblioteca primero", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(allWords) { word ->
                                    val isChecked = boardWords[word.id] == true
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .clickable {
                                                val nextState = !isChecked
                                                boardWords[word.id] = nextState
                                                viewModel.toggleWordInBoard(activeBoard.id, word.id, nextState)
                                            }
                                            .padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { nextState ->
                                                boardWords[word.id] = nextState == true
                                                viewModel.toggleWordInBoard(activeBoard.id, word.id, nextState == true)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(word.text, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (windowInfo.isExpandedWidth) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { boardsListPane() }
                Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) { wordsChecklistPane() }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) { boardsListPane() }
                Box(modifier = Modifier.weight(1.2f).fillMaxWidth()) { wordsChecklistPane() }
            }
        }
    }
}

@Composable
fun StatsTab(
    logs: List<com.trazavoz.domain.model.ProgressLog>,
    words: List<Word>,
    onClearClick: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    val windowInfo = rememberWindowInfo()
    val statNumberSize = when {
        windowInfo.isCompactHeight -> 22.sp
        windowInfo.isExpandedWidth -> 34.sp
        else -> 26.sp
    }
    val sectionTitleSize = when {
        windowInfo.isCompactHeight -> 16.sp
        windowInfo.isExpandedWidth -> 20.sp
        else -> 17.sp
    }

    val wordMap = remember(words) { words.associateBy { it.id } }

    val totalGames = logs.size
    val averageErrors = if (logs.isNotEmpty()) {
        logs.map { it.errorsCount }.sum().toFloat() / logs.size
    } else 0f

    val wordStats = remember(logs) {
        logs.groupBy { it.wordId }.map { (wordId, wordLogs) ->
            val playedCount = wordLogs.size
            val avgErrors = wordLogs.map { it.errorsCount }.sum().toFloat() / playedCount
            wordId to Pair(playedCount, avgErrors)
        }.sortedByDescending { it.second.second }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Partidas jugadas", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalGames", fontSize = statNumberSize, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Errores promedio", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format("%.1f", averageErrors), fontSize = statNumberSize, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Estadísticas por palabra (ordenadas por dificultad)", fontSize = sectionTitleSize, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(8.dp))

        if (wordStats.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay historial registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wordStats) { (wordId, stats) ->
                    val word = wordMap[wordId]
                    val playedCount = stats.first
                    val avgErrors = stats.second

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(word?.text ?: "Palabra eliminada", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text("Jugado: $playedCount veces", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = String.format("%.1f errores prom.", avgErrors),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (avgErrors > 2.0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (avgErrors > 2.0f) {
                                    Text("⚠️", fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Limpiar historial de progreso", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("¿Limpiar estadísticas?") },
                text = { Text("Se borrarán de forma permanente todos los registros del progreso del niño. Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearClick()
                            showConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sí, Borrar", color = MaterialTheme.colorScheme.onError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun AjustesTab(
    onSavePin: (String, (Boolean) -> Unit) -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Cambiar PIN del modo tutor", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        OutlinedTextField(
            value = newPin,
            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) newPin = it },
            label = { Text("Nuevo PIN (4 números)") },
            modifier = Modifier.width(260.dp),
            placeholder = { Text("0000") }
        )

        Button(
            onClick = {
                onSavePin(newPin) { success ->
                    if (success) {
                        message = "¡PIN guardado con éxito!"
                        newPin = ""
                    } else {
                        message = "El PIN debe tener 4 dígitos numéricos."
                    }
                }
            },
            modifier = Modifier.width(260.dp).height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Guardar cambios", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }

        if (message.isNotEmpty()) {
            Text(message, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EditWordDialog(
    word: Word,
    viewModel: TutorViewModel,
    onDismiss: () -> Unit
) {
    var editText by remember { mutableStateOf(word.text) }
    var editSyllables by remember { mutableStateOf(word.syllables.joinToString("-")) }
    var message by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Editar palabra",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedTextField(
                    value = editText,
                    onValueChange = {
                        editText = it
                        editSyllables = viewModel.getSuggestedSyllables(it)
                    },
                    label = { Text("Texto de la palabra") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Puedes cambiar el nombre si la traducción no es correcta para tu región",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = editSyllables,
                    onValueChange = { editSyllables = it },
                    label = { Text("Sílabas (separadas por guiones, ej: JU-GO)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateWord(word, editText, editSyllables) { success ->
                                if (success) {
                                    onDismiss()
                                } else {
                                    message = "Verifica que los campos no estén vacíos."
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Guardar", color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                    }
                }

                if (message.isNotEmpty()) {
                    Text(message, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
