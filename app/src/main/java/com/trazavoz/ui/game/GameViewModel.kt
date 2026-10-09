package com.trazavoz.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trazavoz.domain.model.Word
import com.trazavoz.domain.repository.ProgressRepository
import com.trazavoz.domain.repository.WordRepository
import com.trazavoz.ui.audio.TrazavozTtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
            _uiState.value = GameUiState(currentPhase = GamePhase.LOADING)
            val word = wordRepository.getWordById(wordId)
            
            if (word == null || word.syllables.isEmpty()) {
                _uiState.update { it.copy(currentPhase = GamePhase.ERROR) }
                return@launch
            }
            
            // Fase inicial: Sílabas
            val targetSlots = word.syllables.mapIndexed { index, syllable ->
                PieceSlot(id = "syl_$index", expectedText = syllable.uppercase())
            }
            
            val pieces = word.syllables.map { syllable ->
                PieceItem(id = UUID.randomUUID().toString(), text = syllable.uppercase())
            }.shuffled()
            
            _uiState.value = GameUiState(
                word = word,
                currentPhase = GamePhase.SYLLABLES,
                piecesToPlace = pieces,
                targetSlots = targetSlots
            )
            
            ttsManager.hablarPalabra(word.text)
        }
    }

    fun onItemDropped(piece: PieceItem, slotId: String) {
        val currentState = _uiState.value
        // Ignorar eventos si no estamos en fase activa de juego
        if (currentState.currentPhase != GamePhase.SYLLABLES && currentState.currentPhase != GamePhase.LETTERS) return
        
        val slot = currentState.targetSlots.find { it.id == slotId } ?: return
        
        if (slot.expectedText == piece.text && slot.placedPiece == null) {
            val updatedSlots = currentState.targetSlots.map { s ->
                if (s.id == slotId) s.copy(placedPiece = piece) else s
            }
            
            val updatedPieces = currentState.piecesToPlace.map { p ->
                if (p.id == piece.id) p.copy(isPlaced = true) else p
            }
            
            // Feedback de audio según tipo de pieza
            if (piece.text.length == 1) {
                ttsManager.hablarLetra(piece.text.first())
            } else {
                ttsManager.hablarSilaba(piece.text)
            }
            
            _uiState.update { it.copy(targetSlots = updatedSlots, piecesToPlace = updatedPieces) }
            checkPhaseCompletion()
        } else {
            // Error
            if (piece.text.length == 1) {
                ttsManager.hablarLetra(piece.text.first())
            } else {
                ttsManager.hablarSilaba(piece.text)
            }
            _uiState.update { it.copy(errorsCount = it.errorsCount + 1) }
        }
    }

    private fun checkPhaseCompletion() {
        val currentState = _uiState.value
        val allSlotsFilled = currentState.targetSlots.all { it.placedPiece != null }
        
        if (allSlotsFilled) {
            when (currentState.currentPhase) {
                GamePhase.SYLLABLES -> {
                    viewModelScope.launch {
                        _uiState.update { it.copy(currentPhase = GamePhase.SYLLABLES_SUCCESS) }
                        ttsManager.hablarPalabra(currentState.word?.text ?: "")
                        delay(1200) // Esperar 1.2s para la transición suave
                        prepareLettersPhase()
                    }
                }
                GamePhase.LETTERS -> {
                    _uiState.update { it.copy(currentPhase = GamePhase.COMPLETED, showCelebration = true) }
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
                else -> {}
            }
        }
    }

    private fun prepareLettersPhase() {
        val word = _uiState.value.word ?: return
        
        // Excluimos espacios del array de piezas arrastrables, pero mantenemos su índice para UI si fuera necesario
        // Para simplificar según el plan: validamos el texto jugable
        val textNoSpaces = word.text.replace(" ", "")
        
        val targetSlots = textNoSpaces.mapIndexed { index, char ->
            PieceSlot(id = "let_$index", expectedText = char.uppercase())
        }
        
        val pieces = textNoSpaces.map { char ->
            PieceItem(id = UUID.randomUUID().toString(), text = char.uppercase())
        }.shuffled()
        
        _uiState.update {
            it.copy(
                currentPhase = GamePhase.LETTERS,
                targetSlots = targetSlots,
                piecesToPlace = pieces
            )
        }
    }
}
