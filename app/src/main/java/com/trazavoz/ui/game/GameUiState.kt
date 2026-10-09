package com.trazavoz.ui.game

import com.trazavoz.domain.model.Word

enum class GamePhase {
    LOADING, SYLLABLES, SYLLABLES_SUCCESS, LETTERS, COMPLETED, ERROR
}

data class GameBoardKey(val sessionId: String, val phase: GamePhase)

data class GameUiState(
    val sessionId: String = "",
    val word: Word? = null,
    val currentPhase: GamePhase = GamePhase.LOADING,
    val piecesToPlace: List<PieceItem> = emptyList(),
    val targetSlots: List<PieceSlot> = emptyList(),
    val trailingSeparator: String = "",
    val errorsCount: Int = 0,
    val errorMessage: String? = null,
    val saveError: String? = null
) {
    val isInteractive: Boolean
        get() = currentPhase == GamePhase.SYLLABLES || currentPhase == GamePhase.LETTERS

    // Partial success keeps the same board, avoiding two copies of its drop targets.
    val boardKey: GameBoardKey
        get() = GameBoardKey(sessionId, if (currentPhase == GamePhase.SYLLABLES_SUCCESS) GamePhase.SYLLABLES else currentPhase)
}

data class PieceItem(
    val id: String,
    val text: String,
    val isPlaced: Boolean = false
)

data class PieceSlot(
    val id: String,
    val expectedText: String,
    val separatorBefore: String = "",
    val placedPiece: PieceItem? = null
)
