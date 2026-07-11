package com.trazavoz.ui.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trazavoz.domain.usecase.SearchResult
import com.trazavoz.ui.theme.CelestePastel
import com.trazavoz.ui.theme.CoralPastel
import com.trazavoz.ui.theme.VerdeManzanaPastel

@Composable
fun AddWordScreen(
    viewModel: TutorViewModel,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var selectedPic by remember { mutableStateOf<SearchResult?>(null) }
    var wordText by remember { mutableStateOf("") }
    var syllablesInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSearchResults()
        }
    }

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
                text = "Buscar y añadir palabra",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CelestePastel)
            ) {
                Text("Buscar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CelestePastel)
                    }
                } else if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Escribe y busca para ver pictogramas.", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults) { pic ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clickable {
                                        selectedPic = pic
                                        wordText = pic.name
                                        syllablesInput = viewModel.getSuggestedSyllables(pic.name)
                                        statusMessage = ""
                                    }
                                    .border(
                                        width = if (selectedPic?.id == pic.id) 3.dp else 0.dp,
                                        color = if (selectedPic?.id == pic.id) CelestePastel else Color.Transparent,
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
                                    Text(pic.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(2.dp, Color.LightGray, MaterialTheme.shapes.large)
                    .padding(16.dp)
            ) {
                val pic = selectedPic
                if (pic == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Selecciona un pictograma de la izquierda para continuar", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Guardar nueva palabra", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                        AsyncImage(
                            model = pic.imageUrl,
                            contentDescription = pic.name,
                            modifier = Modifier.size(120.dp)
                        )

                        OutlinedTextField(
                            value = wordText,
                            onValueChange = {
                                wordText = it
                                syllablesInput = viewModel.getSuggestedSyllables(it)
                            },
                            label = { Text("Texto de la palabra") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = syllablesInput,
                            onValueChange = { syllablesInput = it },
                            label = { Text("Sílabas (separadas por guiones, ej: ME-SA)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
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
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeManzanaPastel)
                        ) {
                            Text("Guardar en biblioteca", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        if (statusMessage.isNotEmpty()) {
                            Text(statusMessage, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}
