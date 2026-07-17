package com.trazavoz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
    val btnSize = if (windowInfo.isCompactHeight) 44.dp else 56.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (trailing != null) Arrangement.SpaceBetween else Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onClick = onBackClick, size = btnSize)

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
    size: Dp = 56.dp
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(size),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver"
        )
    }
}

/** Ensures a composable respects the app's minimum touch target height. */
fun Modifier.minTouchTarget(): Modifier = this.heightIn(min = MinTouchTarget)
