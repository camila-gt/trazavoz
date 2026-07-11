package com.trazavoz.ui.tutor

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trazavoz.domain.model.Board
import com.trazavoz.domain.model.Word
import com.trazavoz.ui.theme.CelestePastel
import com.trazavoz.ui.theme.CoralPastel
import com.trazavoz.ui.theme.VerdeManzanaPastel
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.size(width = 120.dp, height = 60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
            ) {
                Text("Volver", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Text(
                text = "Panel del tutor",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Biblioteca", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Tableros", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Estadísticas", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("Ajustes PIN", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> BibliotecaTab(allWords, onAddWordClick, onDeleteClick = { viewModel.deleteWord(it) })
                1 -> TablerosTab(allBoards, allWords, viewModel)
                2 -> StatsTab(allLogs, allWords, onClearClick = { viewModel.clearStats() })
                3 -> AjustesTab(onSavePin = { pin, callback -> viewModel.updatePin(pin, callback) })
            }
        }
    }
}

@Composable
fun BibliotecaTab(
    words: List<Word>,
    onAddWordClick: () -> Unit,
    onDeleteClick: (Word) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onAddWordClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CelestePastel)
        ) {
            Text("Añadir nueva palabra", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (words.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay palabras en la biblioteca.", color = Color.Gray)
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
                                verticalAlignment = Alignment.CenterVertically
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
                                    Text(word.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(word.syllables.joinToString(" - "), fontSize = 14.sp, color = Color.Gray)
                                }
                            }

                            Button(
                                onClick = { onDeleteClick(word) },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
                            ) {
                                Text("Eliminar", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TablerosTab(
    boards: List<Board>,
    allWords: List<Word>,
    viewModel: TutorViewModel
) {
    var newBoardName by remember { mutableStateOf("") }
    var selectedBoardForEdit by remember { mutableStateOf<Board?>(null) }
    val boardWords = remember { mutableStateMapOf<Int, Boolean>() }

    val coroutineScope = rememberCoroutineScope()

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
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CelestePastel)
            ) {
                Text("Crear", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
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
                            Text(board.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Button(
                                onClick = {
                                    if (selectedBoardForEdit?.id == board.id) {
                                        selectedBoardForEdit = null
                                    }
                                    viewModel.deleteBoard(board)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
                            ) {
                                Text("Eliminar", color = Color.Black)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .border(2.dp, Color.LightGray, MaterialTheme.shapes.large)
                    .padding(12.dp)
            ) {
                val activeBoard = selectedBoardForEdit
                if (activeBoard == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Selecciona un tablero para editar sus palabras", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Palabras en: ${activeBoard.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (allWords.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Agrega palabras a la biblioteca primero", color = Color.Gray, textAlign = TextAlign.Center)
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
                                        Text(word.text, fontSize = 16.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
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
                    Text("Partidas jugadas", fontSize = 14.sp, color = Color.Gray)
                    Text("$totalGames", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
            ElevatedCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Errores promedio", fontSize = 14.sp, color = Color.Gray)
                    Text(String.format("%.1f", averageErrors), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Estadísticas por palabra (ordenadas por dificultad)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Spacer(modifier = Modifier.height(8.dp))

        if (wordStats.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay historial registrado.", color = Color.Gray)
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
                                Text(word?.text ?: "Palabra eliminada", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Jugado: $playedCount veces", fontSize = 14.sp, color = Color.Gray)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = String.format("%.1f errores prom.", avgErrors),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (avgErrors > 2.0f) CoralPastel else Color.DarkGray
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
            colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
        ) {
            Text("Limpiar historial de progreso", color = Color.Black, fontWeight = FontWeight.Bold)
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
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPastel)
                    ) {
                        Text("Sí, Borrar", color = Color.Black)
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
        Text("Cambiar PIN del modo tutor", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

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
            colors = ButtonDefaults.buttonColors(containerColor = CelestePastel)
        ) {
            Text("Guardar cambios", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        if (message.isNotEmpty()) {
            Text(message, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        }
    }
}
