package com.trazavoz.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trazavoz.domain.model.Word
import com.trazavoz.domain.repository.ProgressRepository
import com.trazavoz.domain.repository.WordRepository
import com.trazavoz.ui.audio.TrazavozTtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository,
    private val ttsManager: TrazavozTtsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun startNewGame(wordId: Int) {
        viewModelScope.launch {
            val word = wordRepository.getWordById(wordId) ?: return@launch
            
            val targetSlots = word.text.mapIndexed { index, char ->
                SlotItem(index = index, expectedChar = char)
            }
            
            val letters = word.text.map { char ->
                LetterItem(id = UUID.randomUUID().toString(), char = char)
            }.shuffled()
            
            _uiState.value = GameUiState(
                word = word,
                lettersToPlace = letters,
                targetSlots = targetSlots
            )
            
            ttsManager.hablarPalabra(word.text)
        }
    }

    fun onLetterDropped(letter: LetterItem, slotIndex: Int) {
        val currentState = _uiState.value
        val slot = currentState.targetSlots.getOrNull(slotIndex) ?: return
        
        if (slot.expectedChar == letter.char && slot.placedLetter == null) {
            val updatedSlots = currentState.targetSlots.map { s ->
                if (s.index == slotIndex) s.copy(placedLetter = letter) else s
            }
            
            val updatedLetters = currentState.lettersToPlace.map { l ->
                if (l.id == letter.id) l.copy(isPlaced = true) else l
            }
            
            ttsManager.hablarLetra(letter.char)
            _uiState.update { it.copy(targetSlots = updatedSlots, lettersToPlace = updatedLetters) }
            checkGameCompletion()
        } else {
            ttsManager.hablarLetra(letter.char)
            _uiState.update { it.copy(errorsCount = it.errorsCount + 1) }
        }
    }

    private fun checkGameCompletion() {
        val currentState = _uiState.value
        val allSlotsFilled = currentState.targetSlots.all { it.placedLetter != null }
        
        if (allSlotsFilled) {
            _uiState.update { it.copy(isCompleted = true, showCelebration = true) }
            
            viewModelScope.launch {
                ttsManager.hablarPalabra(currentState.word?.text ?: "")
                currentState.word?.let { word ->
                    progressRepository.insertLog(
                        wordId = word.id,
                        errorsCount = currentState.errorsCount,
                        isCompleted = true
                    )
                }
            }
        }
    }
}
