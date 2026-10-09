package com.trazavoz.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trazavoz.domain.repository.ProgressRepository
import com.trazavoz.domain.repository.WordRepository
import com.trazavoz.domain.usecase.PrepareReadingWordUseCase
import com.trazavoz.domain.usecase.ReadingUnit
import com.trazavoz.domain.usecase.ReadingWord
import com.trazavoz.ui.audio.GameAudio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
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
    private val audio: GameAudio,
    private val prepareWord: PrepareReadingWordUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private var gameJob: Job? = null
    private var readingWord: ReadingWord? = null

    fun startNewGame(wordId: Int) {
        gameJob?.cancel()
        audio.stop()
        readingWord = null
        val session = UUID.randomUUID().toString()
        _uiState.value = GameUiState(sessionId = session)
        gameJob = viewModelScope.launch {
            try {
                val word = wordRepository.getWordById(wordId)
                val prepared = word?.let(prepareWord::invoke)
                if (_uiState.value.sessionId != session) return@launch
                if (word == null || prepared == null) {
                    _uiState.update { it.copy(currentPhase = GamePhase.ERROR, errorMessage = "No pudimos preparar esta palabra. Puedes volver o reintentar.") }
                    return@launch
                }
                readingWord = prepared
                _uiState.value = GameUiState(
                    sessionId = session,
                    word = word.copy(text = prepared.displayText),
                    trailingSeparator = prepared.trailingSeparator
                ).withBoard(GamePhase.SYLLABLES, prepared.syllables)
                audio.speakWord(prepared.displayText)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_uiState.value.sessionId == session) {
                    _uiState.update { it.copy(currentPhase = GamePhase.ERROR, errorMessage = "No pudimos cargar esta palabra. Puedes volver o reintentar.") }
                }
            }
        }
    }

    fun speakReference() {
        val state = _uiState.value
        // Do not interrupt the sequence that controls the partial-success transition.
        if (state.currentPhase != GamePhase.SYLLABLES_SUCCESS) state.word?.let { audio.speakWord(it.text) }
    }

    fun onItemDropped(piece: PieceItem, slotId: String) {
        val state = _uiState.value
        if (!state.isInteractive) return
        val available = state.piecesToPlace.find { it.id == piece.id && !it.isPlaced } ?: return
        if (available.text != piece.text) return
        val slot = state.targetSlots.find { it.id == slotId } ?: return
        if (slot.placedPiece != null || slot.expectedText != available.text) {
            _uiState.update { it.copy(errorsCount = it.errorsCount + 1) }
            return
        }
        val updated = state.copy(
            targetSlots = state.targetSlots.map { if (it.id == slotId) it.copy(placedPiece = available) else it },
            piecesToPlace = state.piecesToPlace.map { if (it.id == available.id) it.copy(isPlaced = true) else it }
        )
        val complete = updated.targetSlots.isNotEmpty() && updated.targetSlots.all { it.placedPiece != null }
        if (!complete) {
            _uiState.value = updated
            audio.speakPiece(available.text, state.currentPhase == GamePhase.SYLLABLES)
            return
        }
        if (state.currentPhase == GamePhase.SYLLABLES) {
            // Set the phase synchronously: repeated drop callbacks cannot launch two transitions.
            _uiState.value = updated.copy(currentPhase = GamePhase.SYLLABLES_SUCCESS)
            gameJob = viewModelScope.launch {
                coroutineScope {
                    val speech = launch { audio.speakSequenceAndAwait(listOf(available.text, updated.word.orEmptyText())) }
                    delay(1000)
                    speech.join()
                }
                val current = _uiState.value
                if (current.sessionId == updated.sessionId && current.currentPhase == GamePhase.SYLLABLES_SUCCESS) {
                    readingWord?.let { _uiState.value = current.withBoard(GamePhase.LETTERS, it.letters) }
                }
            }
        } else {
            _uiState.value = updated.copy(currentPhase = GamePhase.COMPLETED)
            gameJob = viewModelScope.launch {
                audio.speakSequenceAndAwait(listOf(available.text, updated.word.orEmptyText()))
            }
            // Saving an already completed attempt is independent of replay/audio cancellation.
            viewModelScope.launch {
                try {
                    updated.word?.let { progressRepository.insertLog(it.id, updated.errorsCount, true) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (_uiState.value.sessionId == updated.sessionId) {
                        _uiState.update { it.copy(saveError = "Completaste la palabra, pero no pudimos guardar el progreso.") }
                    }
                }
            }
        }
    }

    fun leaveGame() {
        gameJob?.cancel()
        gameJob = null
        readingWord = null
        audio.stop()
        _uiState.value = GameUiState()
    }

    override fun onCleared() {
        gameJob?.cancel()
        audio.stop()
        super.onCleared()
    }

    private fun GameUiState.withBoard(phase: GamePhase, units: List<ReadingUnit>): GameUiState = copy(
        currentPhase = phase,
        targetSlots = units.mapIndexed { index, unit ->
            PieceSlot("${sessionId}_${phase}_$index", unit.text, unit.separatorBefore)
        },
        piecesToPlace = units.map { PieceItem(UUID.randomUUID().toString(), it.text) }.shuffled()
    )

    private fun com.trazavoz.domain.model.Word?.orEmptyText(): String = this?.text.orEmpty()
}
