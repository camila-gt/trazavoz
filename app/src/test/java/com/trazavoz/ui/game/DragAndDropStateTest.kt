package com.trazavoz.ui.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.trazavoz.ui.components.DragAndDropState
import org.junit.Assert.*
import org.junit.Test

class DragAndDropStateTest {
    @Test fun `outgoing owner cannot unregister replacement target`() {
        val state = DragAndDropState()
        val outgoing = Any()
        val incoming = Any()
        state.registerTarget("slot", Rect(0f, 0f, 50f, 50f), outgoing)
        state.registerTarget("slot", Rect(100f, 0f, 150f, 50f), incoming)
        state.unregisterTarget("slot", outgoing)
        state.onDragStart("piece", Offset(120f, 20f))
        assertEquals("slot", state.onDragEnd())
        state.unregisterTarget("slot", incoming)
        state.onDragStart("piece", Offset(120f, 20f))
        assertNull(state.onDragEnd())
    }

    @Test fun `cancel clears piece position and displacement`() {
        val state = DragAndDropState()
        state.onDragStart("piece", Offset(50f, 50f))
        state.onDrag(Offset(30f, 20f))
        state.cancelDrag()
        assertFalse(state.isDragging)
        assertNull(state.dragItem)
        assertEquals(Offset.Zero, state.dragOffset)
        assertEquals(Offset.Zero, state.dragPosition)
    }

    @Test fun `drop uses window coordinates and clears gesture`() {
        val state = DragAndDropState()
        state.containerOffset = Offset(10f, 10f)
        state.registerTarget("slot", Rect(50f, 50f, 100f, 100f))
        state.onDragStart("piece", Offset(30f, 30f))
        state.onDrag(Offset(40f, 40f))
        assertEquals(Offset(60f, 60f), state.currentDragLocalPosition)
        assertEquals("slot", state.onDragEnd())
        assertNull(state.dragItem)
        assertEquals(Offset.Zero, state.dragOffset)
    }
}
