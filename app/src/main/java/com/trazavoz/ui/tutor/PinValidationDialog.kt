package com.trazavoz.ui.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trazavoz.ui.menu.MenuViewModel
import com.trazavoz.ui.theme.CelestePastel
import com.trazavoz.ui.theme.CoralPastel
import kotlinx.coroutines.launch

@Composable
fun PinValidationDialog(
    onDismiss: () -> Unit,
    onPinVerified: () -> Unit,
    viewModel: MenuViewModel
) {
    var pinState by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val screenWidth = config.screenWidthDp.dp
    val isLandscape = screenWidth > screenHeight

    val dialogMaxHeight = if (isLandscape) screenHeight * 0.92f else screenHeight * 0.7f
    val dialogWidth = if (isLandscape) min(screenWidth * 0.4f, 320.dp) else min(screenWidth * 0.85f, 340.dp)
    val keySize = if (isLandscape) min(screenHeight * 0.16f, 52.dp) else 60.dp
    val keySpacing = if (isLandscape) 6.dp else 10.dp
    val titleSize = if (isLandscape) 18.sp else 22.sp
    val keyFontSize = if (isLandscape) 18.sp else 24.sp
    val dotSize = if (isLandscape) 18.dp else 24.dp
    val sectionSpacing = if (isLandscape) 8.dp else 16.dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ElevatedCard(
            modifier = Modifier
                .width(dialogWidth)
                .heightIn(max = dialogMaxHeight)
                .padding(horizontal = 8.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Acceso modo tutor",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val active = i < pinState.length
                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(
                                    if (isError) CoralPastel
                                    else if (active) CelestePastel
                                    else Color.LightGray.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isError) CoralPastel else Color.DarkGray,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(sectionSpacing))

                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "⌫")

                Column(
                    verticalArrangement = Arrangement.spacedBy(keySpacing)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(keySpacing, Alignment.CenterHorizontally)
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                val key = keys.getOrNull(index) ?: ""

                                Box(
                                    modifier = Modifier
                                        .size(keySize)
                                        .clip(CircleShape)
                                        .background(Color.LightGray.copy(alpha = 0.3f))
                                        .clickable {
                                            isError = false
                                            when (key) {
                                                "C" -> pinState = ""
                                                "⌫" -> {
                                                    if (pinState.isNotEmpty()) {
                                                        pinState = pinState.dropLast(1)
                                                    }
                                                }
                                                else -> {
                                                    if (pinState.length < 4) {
                                                        pinState += key
                                                        if (pinState.length == 4) {
                                                            coroutineScope.launch {
                                                                val valid = viewModel.verifyPin(pinState)
                                                                if (valid) {
                                                                    onPinVerified()
                                                                } else {
                                                                    isError = true
                                                                    pinState = ""
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = keyFontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralPastel
                    )
                }
            }
        }
    }
}
