package com.trazavoz.ui.game

import com.trazavoz.domain.model.Word

data class GameUiState(
    val word: Word? = null,
    val lettersToPlace: List<LetterItem> = emptyList(),
    val targetSlots: List<SlotItem> = emptyList(),
    val errorsCount: Int = 0,
    val isCompleted: Boolean = false,
    val showCelebration: Boolean = false
)

data class LetterItem(
    val id: String,
    val char: Char,
    val isPlaced: Boolean = false
)

data class SlotItem(
    val index: Int,
    val expectedChar: Char,
    val placedLetter: LetterItem? = null
)
