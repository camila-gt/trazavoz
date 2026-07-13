package com.trazavoz.ui.words

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trazavoz.ui.components.ScreenHeader
import com.trazavoz.ui.theme.rememberWindowInfo
import java.io.File

@Composable
fun WordListScreen(
    filterType: String,
    filterValue: String,
    viewModel: WordListViewModel,
    onWordClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val words by viewModel.getWords(filterType, filterValue).collectAsState(initial = emptyList())
    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight

    val title = if (filterType == "letter") "Palabras con $filterValue" else "Mi Tablero"

    val padding = if (isCompact) 8.dp else 16.dp
    val gridMinSize = if (windowInfo.isExpandedWidth) 200.dp else if (isCompact) 130.dp else 160.dp
    val cardHeight = if (isCompact) 130.dp else if (windowInfo.isExpandedWidth) 190.dp else 160.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
    ) {
        ScreenHeader(title = title, onBackClick = onBackClick, windowInfo = windowInfo)

        Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 16.dp))

        if (words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay palabras agregadas en este grupo.",
                    fontSize = if (isCompact) 16.sp else 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = gridMinSize),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(words) { word ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cardHeight)
                            .clickable { onWordClick(word.id) },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
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
                                    modifier = Modifier.fillMaxHeight(0.85f)
                                )
                            }
                            Text(
                                text = word.text,
                                fontSize = if (isCompact) 16.sp else 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
