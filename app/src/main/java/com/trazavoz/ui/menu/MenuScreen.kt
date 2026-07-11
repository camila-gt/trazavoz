package com.trazavoz.ui.menu

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

    val alphabet = remember {
        ("ABCDEFGHIJKLMNÑOPQRSTUVWXYZ").map { it.toString() }
    }

    val pastelColors = remember {
        listOf(CoralPastel, CelestePastel, VerdeManzanaPastel, PurpuraSuave, AmarilloCrema)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trazavoz",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = { showPinDialog = true },
                    modifier = Modifier.height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpuraSuave)
                ) {
                    Text(
                        text = "Modo tutor",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Abecedario", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mis tableros", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 85.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(alphabet) { letter ->
                            val colorIndex = alphabet.indexOf(letter) % pastelColors.size
                            val color = pastelColors[colorIndex]

                            ElevatedCard(
                                modifier = Modifier
                                    .size(85.dp)
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
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        color = Color.Black
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
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(boards) { board ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clickable { onBoardClick(board.id) },
                                    shape = MaterialTheme.shapes.large,
                                    colors = CardDefaults.elevatedCardColors(containerColor = AmarilloCrema)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = board.name,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
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
