package com.trazavoz.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

class DragAndDropState {
    var isDragging by mutableStateOf(false)
    var dragItem by mutableStateOf<Any?>(null)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragPosition by mutableStateOf(Offset.Zero)
    var containerOffset by mutableStateOf(Offset.Zero)

    private val dropTargets = mutableMapOf<Any, Rect>()

    fun registerTarget(id: Any, bounds: Rect) {
        dropTargets[id] = bounds
    }

    fun unregisterTarget(id: Any) {
        dropTargets.remove(id)
    }

    fun onDrag(offset: Offset) {
        dragOffset += offset
    }

    fun onDragStart(item: Any, position: Offset) {
        isDragging = true
        dragItem = item
        dragPosition = position
        dragOffset = Offset.Zero
    }

    fun onDragEnd(): Any? {
        isDragging = false
        val absolutePos = dragPosition + dragOffset
        val target = dropTargets.entries.firstOrNull { it.value.contains(absolutePos) }?.key
        dragItem = null
        return target
    }

    val currentDragAbsolutePosition: Offset
        get() = dragPosition + dragOffset

    val currentDragLocalPosition: Offset
        get() = currentDragAbsolutePosition - containerOffset
}

val LocalDragAndDropState = compositionLocalOf { DragAndDropState() }

@Composable
fun DragAndDropContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(state: DragAndDropState) -> Unit
) {
    val state = remember { DragAndDropState() }
    CompositionLocalProvider(LocalDragAndDropState provides state) {
        Box(
            modifier = modifier.onGloballyPositioned {
                state.containerOffset = it.boundsInWindow().topLeft
            }
        ) {
            content(state)
        }
    }
}
