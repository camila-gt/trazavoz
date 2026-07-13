package com.trazavoz.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Snapshot of the current window size, computed once per screen and shared
 * by every composable so breakpoints stay consistent across the app.
 */
data class WindowInfo(
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val isCompactHeight: Boolean,
    val isExpandedWidth: Boolean,
    val isLandscape: Boolean
)

@Composable
fun rememberWindowInfo(): WindowInfo {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val screenHeight = config.screenHeightDp.dp
    return remember(screenWidth, screenHeight) {
        WindowInfo(
            screenWidthDp = screenWidth,
            screenHeightDp = screenHeight,
            isCompactHeight = screenHeight < 480.dp,
            isExpandedWidth = screenWidth >= 600.dp,
            isLandscape = screenWidth > screenHeight
        )
    }
}
