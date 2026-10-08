package com.trazavoz.ui.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trazavoz.domain.usecase.SearchResult
import com.trazavoz.ui.components.ScreenHeader
import com.trazavoz.ui.components.minTouchTarget
import com.trazavoz.ui.theme.WindowInfo
import com.trazavoz.ui.theme.rememberWindowInfo

@Composable
fun AddWordScreen(
    viewModel: TutorViewModel,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

    var selectedPic by remember { mutableStateOf<SearchResult?>(null) }
    var wordText by remember { mutableStateOf("") }
    var syllablesInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight
    val padding = if (isCompact) 8.dp else 16.dp

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSearchResults()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
    ) {
        ScreenHeader(title = "Buscar y añadir palabra", onBackClick = onBackClick, windowInfo = windowInfo)

        Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar en ARASAAC (ej: mesa, perro)") },
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    selectedPic = null
                    viewModel.searchWord(searchQuery)
                },
                modifier = Modifier.height(56.dp).minTouchTarget(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Buscar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 16.dp))

        val resultsPane: @Composable () -> Unit = {
            SearchResultsPane(
                isSearching = isSearching,
                hasSearched = hasSearched,
                searchResults = searchResults,
                searchError = searchError,
                selectedPic = selectedPic,
                windowInfo = windowInfo,
                onPicSelected = { pic ->
                    selectedPic = pic
                    wordText = pic.name
                    syllablesInput = viewModel.getSuggestedSyllables(pic.name)
                    statusMessage = ""
                }
            )
        }

        val formPane: @Composable () -> Unit = {
            NewWordFormPane(
                selectedPic = selectedPic,
                wordText = wordText,
                onWordTextChange = {
                    wordText = it
                    syllablesInput = viewModel.getSuggestedSyllables(it)
                },
                syllablesInput = syllablesInput,
                onSyllablesChange = { syllablesInput = it },
                statusMessage = statusMessage,
                onSaveClick = {
                    val pic = selectedPic ?: return@NewWordFormPane
                    viewModel.addWord(
                        text = wordText,
                        arasaacId = pic.id,
                        syllablesInput = syllablesInput,
                        imageUrl = pic.imageUrl
                    ) { success ->
                        if (success) {
                            statusMessage = "¡Palabra guardada con éxito!"
                            selectedPic = null
                        } else {
                            statusMessage = "Error al guardar. Verifica los campos."
                        }
                    }
                }
            )
        }

        if (windowInfo.isExpandedWidth) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { resultsPane() }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) { formPane() }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) { resultsPane() }
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) { formPane() }
            }
        }
    }
}

@Composable
private fun SearchResultsPane(
    isSearching: Boolean,
    hasSearched: Boolean,
    searchResults: List<SearchResult>,
    searchError: String?,
    selectedPic: SearchResult?,
    windowInfo: WindowInfo,
    onPicSelected: (SearchResult) -> Unit
) {
    when {
        isSearching -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        searchError != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(searchError, color = MaterialTheme.colorScheme.error)
            }
        }
        searchResults.isEmpty() && hasSearched -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron resultados para esta búsqueda.", color = MaterialTheme.colorScheme.error)
            }
        }
        searchResults.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Escribe y busca para ver pictogramas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = if (windowInfo.isCompactHeight) 120.dp else 150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { pic ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable { onPicSelected(pic) }
                            .border(
                                width = if (selectedPic?.id == pic.id) 3.dp else 0.dp,
                                color = if (selectedPic?.id == pic.id) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = MaterialTheme.shapes.large
                            ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            AsyncImage(
                                model = pic.imageUrl,
                                contentDescription = pic.name,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                            Text(pic.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewWordFormPane(
    selectedPic: SearchResult?,
    wordText: String,
    onWordTextChange: (String) -> Unit,
    syllablesInput: String,
    onSyllablesChange: (String) -> Unit,
    statusMessage: String,
    onSaveClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
            .padding(16.dp)
    ) {
        if (selectedPic == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Selecciona un pictograma para continuar",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Guardar nueva palabra", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

                AsyncImage(
                    model = selectedPic.imageUrl,
                    contentDescription = selectedPic.name,
                    modifier = Modifier.size(120.dp)
                )

                OutlinedTextField(
                    value = wordText,
                    onValueChange = onWordTextChange,
                    label = { Text("Texto de la palabra") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Puedes cambiar el nombre si la traducción no es correcta para tu región",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = syllablesInput,
                    onValueChange = onSyllablesChange,
                    label = { Text("Sílabas (separadas por guiones, ej: ME-SA)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp).minTouchTarget(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Guardar en biblioteca", color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
                }

                if (statusMessage.isNotEmpty()) {
                    Text(statusMessage, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
