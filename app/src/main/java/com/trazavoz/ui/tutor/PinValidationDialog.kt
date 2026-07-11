package com.trazavoz.ui.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Acceso modo tutor",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val active = i < pinState.length
                        Box(
                            modifier = Modifier
                                .size(24.dp)
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

                Spacer(modifier = Modifier.height(24.dp))

                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "⌫")

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                val key = keys.getOrNull(index) ?: ""

                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
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
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralPastel
                    )
                }
            }
        }
    }
}
