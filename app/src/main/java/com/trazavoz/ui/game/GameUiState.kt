package com.trazavoz.ui.game

import com.trazavoz.domain.model.Word

enum class GamePhase {
    LOADING,
    SYLLABLES,
    SYLLABLES_SUCCESS, // Confirmación parcial
    LETTERS,
    COMPLETED,
    ERROR
}

data class GameUiState(
    val word: Word? = null,
    val currentPhase: GamePhase = GamePhase.LOADING,
    val piecesToPlace: List<PieceItem> = emptyList(),
    val targetSlots: List<PieceSlot> = emptyList(),
    val errorsCount: Int = 0,
    val showCelebration: Boolean = false
)

data class PieceItem(
    val id: String,
    val text: String,
    val isPlaced: Boolean = false
)

data class PieceSlot(
    val id: String, // Identificador único por fase e índice para DragAndDrop
    val expectedText: String,
    val placedPiece: PieceItem? = null
)
