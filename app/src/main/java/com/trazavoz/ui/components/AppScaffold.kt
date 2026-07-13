package com.trazavoz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trazavoz.ui.theme.WindowInfo

/** Minimum recommended touch target per the project's own design guidelines. */
val MinTouchTarget = 48.dp

/**
 * Consistent "back button + title" row used by every non-root screen
 * (WordList, AddWord, TutorDashboard, Game). [trailing] lets a screen add
 * an extra element (e.g. a score badge) while keeping the title centered.
 */
@Composable
fun ScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    windowInfo: WindowInfo,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val titleSize = when {
        windowInfo.isCompactHeight -> 20.sp
        windowInfo.isExpandedWidth -> 34.sp
        windowInfo.isLandscape -> 26.sp
        else -> 23.sp
    }
    val btnHeight = if (windowInfo.isCompactHeight) 44.dp else 56.dp
    val btnWidth = if (windowInfo.isCompactHeight) 90.dp else 120.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (trailing != null) Arrangement.SpaceBetween else Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onBackClick, btnWidth, btnHeight, windowInfo.isCompactHeight)

        Text(
            text = title,
            fontSize = titleSize,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = if (trailing != null) Modifier.weight(1f) else Modifier
        )

        trailing?.invoke()
    }
}

@Composable
fun BackButton(
    onClick: () -> Unit,
    width: Dp = 120.dp,
    height: Dp = 56.dp,
    isCompact: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(width = width, height = height),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text(
            text = "Volver",
            fontSize = if (isCompact) 14.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

/** Ensures a composable respects the app's minimum touch target height. */
fun Modifier.minTouchTarget(): Modifier = this.heightIn(min = MinTouchTarget)
