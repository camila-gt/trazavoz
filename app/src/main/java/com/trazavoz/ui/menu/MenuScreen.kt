package com.trazavoz.ui.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.trazavoz.ui.components.minTouchTarget
import com.trazavoz.ui.theme.*
import com.trazavoz.ui.tutor.PinValidationDialog

@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    onLetterClick: (String) -> Unit,
    onBoardClick: (Int) -> Unit,
    onTutorAuthenticated: () -> Unit
) {
    val boards by viewModel.boards.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPinDialog by remember { mutableStateOf(false) }

    val windowInfo = rememberWindowInfo()
    val isCompact = windowInfo.isCompactHeight

    val alphabet = remember {
        ("ABCDEFGHIJKLMNÑOPQRSTUVWXYZ").map { it.toString() }
    }

    val titleSize = if (isCompact) 22.sp else if (windowInfo.isExpandedWidth) 40.sp else 28.sp
    val tabFontSize = if (isCompact) 14.sp else if (windowInfo.isExpandedWidth) 20.sp else 16.sp
    val gridMinSize = if (isCompact) 65.dp else if (windowInfo.isExpandedWidth) 100.dp else 80.dp
    val cardSize = if (isCompact) 65.dp else if (windowInfo.isExpandedWidth) 100.dp else 80.dp
    val letterFontSize = if (isCompact) 22.sp else if (windowInfo.isExpandedWidth) 32.sp else 26.sp
    val padding = if (isCompact) 8.dp else 16.dp
    val tutorBtnHeight = if (isCompact) 48.dp else 56.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trazavoz",
                    fontSize = titleSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = { showPinDialog = true },
                    modifier = Modifier
                        .height(tutorBtnHeight)
                        .minTouchTarget(),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Modo tutor",
                        fontSize = if (isCompact) 14.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Abecedario", fontSize = tabFontSize, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mis tableros", fontSize = tabFontSize, fontWeight = FontWeight.Bold) }
                )
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
                    label = "menu-tabs"
                ) { tab ->
                    if (tab == 0) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = gridMinSize),
                            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 12.dp),
                            verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(alphabet) { letter ->
                                val color = colorForLetter(letter.first())

                                ElevatedCard(
                                    modifier = Modifier
                                        .size(cardSize)
                                        .clickable { onLetterClick(letter) },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.elevatedCardColors(containerColor = color)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = letter,
                                            fontSize = letterFontSize,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        if (boards.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay tableros creados. Activa el modo tutor para agregar uno.",
                                    fontSize = if (isCompact) 16.sp else 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = if (isCompact) 150.dp else 180.dp),
                                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 16.dp),
                                verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(boards) { board ->
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(if (isCompact) 70.dp else 110.dp)
                                            .clickable { onBoardClick(board.id) },
                                        shape = MaterialTheme.shapes.large,
                                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(if (isCompact) 8.dp else 16.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = board.name,
                                                fontSize = if (isCompact) 18.sp else 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPinDialog) {
            PinValidationDialog(
                onDismiss = { showPinDialog = false },
                onPinVerified = {
                    showPinDialog = false
                    onTutorAuthenticated()
                },
                viewModel = viewModel
            )
        }
    }
}
