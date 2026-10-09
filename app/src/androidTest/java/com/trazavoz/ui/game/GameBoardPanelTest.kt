package com.trazavoz.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trazavoz.domain.model.ProgressLog
import com.trazavoz.domain.model.Word
import com.trazavoz.domain.repository.ProgressRepository
import com.trazavoz.domain.repository.WordRepository
import com.trazavoz.domain.usecase.PrepareReadingWordUseCase
import com.trazavoz.domain.usecase.SplitSyllablesUseCase
import com.trazavoz.ui.audio.GameAudio
import com.trazavoz.ui.components.DragAndDropContainer
import com.trazavoz.ui.components.DragAndDropState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameBoardPanelTest {
    @get:Rule val compose = createComposeRule()
    private val words = BoardWords()
    private val progress = BoardProgress()
    private val audio = BoardAudio()
    private val vm = GameViewModel(words, progress, audio, PrepareReadingWordUseCase(SplitSyllablesUseCase()))
    private lateinit var drag: DragAndDropState

    @After fun cleanup() { compose.runOnIdle { vm.leaveGame() } }

    private fun show(width: Int = 600, height: Int = 400) {
        compose.setContent {
            val state by vm.uiState.collectAsState()
            LiteracyGameTheme {
                Box(Modifier.size(width.dp, height.dp)) {
                    DragAndDropContainer { current ->
                        drag = current
                        GameBoardPanel(state, vm, true, { vm.startNewGame(1) }, vm::leaveGame)
                    }
                }
            }
        }
        compose.runOnIdle { vm.startNewGame(1) }
        compose.waitUntil(5000) { vm.uiState.value.currentPhase == GamePhase.SYLLABLES }
        compose.waitForIdle()
    }

    @Test fun draggingPiecesCompletesBothPhases() {
        show()
        placeBoardByTouch()
        compose.onNodeWithText("¡Muy bien! Ahora vamos con las letras.").assertIsDisplayed()
        compose.waitUntil(5000) { vm.uiState.value.currentPhase == GamePhase.LETTERS }
        compose.waitForIdle()
        placeBoardByTouch()
        compose.onNodeWithText("¡Excelente!").assertIsDisplayed()
        compose.onNodeWithText("Jugar de nuevo").assertIsDisplayed()
        compose.waitUntil(5000) { progress.saved == 1 }
        assertTrue(audio.pieces.isNotEmpty())
    }

    @Test fun destinationsRemainRegisteredAfterOutgoingAnimationIsDisposed() {
        show()
        placeBoardByTouch()
        compose.waitUntil(5000) { vm.uiState.value.currentPhase == GamePhase.LETTERS }
        compose.waitForIdle()
        vm.uiState.value.targetSlots.forEach { slot ->
            val node = compose.onNodeWithTag("slot_${slot.id}").fetchSemanticsNode()
            compose.runOnIdle {
                // Drag targets use window coordinates; the root's offset is held by the container.
                val point = node.boundsInRoot.center + drag.containerOffset
                drag.onDragStart("test", point)
                assertEquals(slot.id, drag.onDragEnd())
            }
        }
    }

    @Test fun longWordWrapsDestinationsWithoutShrinkingTouchTargets() {
        words.word = words.word.copy(text = "MARIPOSA", syllables = listOf("MA", "RI", "PO", "SA"))
        show(width = 320)
        placeBoardByTouch()
        compose.waitUntil(5000) { vm.uiState.value.currentPhase == GamePhase.LETTERS }
        compose.waitForIdle()
        val slots = vm.uiState.value.targetSlots
        val first = compose.onNodeWithTag("slot_${slots.first().id}").fetchSemanticsNode().boundsInRoot
        val last = compose.onNodeWithTag("slot_${slots.last().id}").performScrollTo().fetchSemanticsNode().boundsInRoot
        assertTrue(last.width >= 48f)
        assertTrue(last.height >= 48f)
        assertTrue(first.top != last.top)
        compose.onNodeWithTag("slot_${slots.last().id}").assertIsDisplayed()
    }

    @Test fun immediateDragPlacesAVisiblePiece() {
        show()
        val state = vm.uiState.value
        val slot = state.targetSlots.first()
        val piece = state.piecesToPlace.first { it.text == slot.expectedText }
        dragPieceToSlot(piece, slot)
        assertEquals(piece.id, vm.uiState.value.targetSlots.first().placedPiece?.id)
    }

    @Test fun tappingPieceAndDestinationDoesNotPlaceOrSpeak() {
        show()
        val state = vm.uiState.value
        val slot = state.targetSlots.first()
        val piece = state.piecesToPlace.first { it.text == slot.expectedText }
        compose.onNodeWithTag("piece_${piece.id}").assertHasNoClickAction().performTouchInput { click() }
        compose.onNodeWithTag("slot_${slot.id}").assertHasNoClickAction().performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(state, vm.uiState.value)
            assertTrue(audio.pieces.isEmpty())
        }
    }

    private fun placeBoardByTouch() {
        vm.uiState.value.targetSlots.forEach { slot ->
            val piece = vm.uiState.value.piecesToPlace.first { !it.isPlaced && it.text == slot.expectedText }
            dragPieceToSlot(piece, slot)
        }
    }

    private fun dragPieceToSlot(piece: PieceItem, slot: PieceSlot) {
        val source = compose.onNodeWithTag("piece_${piece.id}").performScrollTo()
        val target = compose.onNodeWithTag("slot_${slot.id}").performScrollTo()
        val origin = source.fetchSemanticsNode().boundsInRoot
        val destination = target.fetchSemanticsNode().boundsInRoot.center
        source.performTouchInput {
            swipe(center, destination - origin.topLeft, durationMillis = 200)
        }
        compose.waitForIdle()
    }
}

private class BoardAudio : GameAudio {
    val pieces = mutableListOf<String>()
    override fun speakWord(text: String) = Unit
    override fun speakPiece(text: String, isSyllable: Boolean) { pieces += text }
    override suspend fun speakSequenceAndAwait(parts: List<String>) = Unit
    override fun stop() = Unit
}

private class BoardWords : WordRepository {
    var word = Word(1, "MESA", "M", "ME", listOf("ME", "SA"), "", null)
    override suspend fun getWordById(id: Int) = word
    override fun getAllWords(): Flow<List<Word>> = flowOf(listOf(word))
    override fun getWordsByLetter(letter: String) = getAllWords()
    override suspend fun insertWord(word: Word) = 1L
    override suspend fun deleteWord(word: Word) = Unit
    override suspend fun updateWord(word: Word) = Unit
}

private class BoardProgress : ProgressRepository {
    var saved = 0
    override suspend fun insertLog(wordId: Int, errorsCount: Int, isCompleted: Boolean) { saved++ }
    override fun getAllLogs(): Flow<List<ProgressLog>> = flowOf(emptyList())
    override fun getLogsForWord(wordId: Int) = getAllLogs()
    override suspend fun clearAllLogs() = Unit
}
