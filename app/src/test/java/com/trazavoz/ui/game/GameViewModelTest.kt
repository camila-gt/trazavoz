package com.trazavoz.ui.game

import com.trazavoz.domain.model.ProgressLog
import com.trazavoz.domain.model.Word
import com.trazavoz.domain.repository.ProgressRepository
import com.trazavoz.domain.repository.WordRepository
import com.trazavoz.domain.usecase.PrepareReadingWordUseCase
import com.trazavoz.domain.usecase.SplitSyllablesUseCase
import com.trazavoz.ui.audio.GameAudio
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val words = FakeWords()
    private val progress = FakeProgress()
    private val audio = FakeAudio()
    private lateinit var vm: GameViewModel

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        vm = GameViewModel(words, progress, audio, PrepareReadingWordUseCase(SplitSyllablesUseCase()))
    }

    @After fun cleanup() {
        vm.leaveGame()
        Dispatchers.resetMain()
    }

    @Test fun `starts in syllables and only saves after letters with accumulated errors`() = runTest(dispatcher) {
        vm.startNewGame(1)
        runCurrent()
        assertEquals(GamePhase.SYLLABLES, vm.uiState.value.currentPhase)
        assertEquals(listOf("ME", "SA"), vm.uiState.value.targetSlots.map { it.expectedText })
        val state = vm.uiState.value
        val wrong = state.piecesToPlace.first { it.text == "SA" }
        vm.onItemDropped(wrong, state.targetSlots.first().id)
        assertEquals(1, vm.uiState.value.errorsCount)
        fillBoard()
        assertEquals(GamePhase.SYLLABLES_SUCCESS, vm.uiState.value.currentPhase)
        assertTrue(progress.logs.isEmpty())
        advanceUntilIdle()
        assertEquals(GamePhase.LETTERS, vm.uiState.value.currentPhase)
        assertEquals(4, vm.uiState.value.targetSlots.size)
        fillBoard()
        val completed = vm.uiState.value
        assertEquals(GamePhase.COMPLETED, completed.currentPhase)
        // A repeated callback after completion cannot insert a second record.
        vm.onItemDropped(completed.piecesToPlace.first(), completed.targetSlots.first().id)
        advanceUntilIdle()
        assertEquals(listOf(Triple(1, 1, true)), progress.logs)
    }

    @Test fun `duplicate repeated piece cannot fill another matching slot`() = runTest(dispatcher) {
        words.word = words.word!!.copy(text = "MAMA", syllables = listOf("MA", "MA"))
        vm.startNewGame(1)
        runCurrent()
        val state = vm.uiState.value
        val piece = state.piecesToPlace.first()
        vm.onItemDropped(piece, state.targetSlots[0].id)
        vm.onItemDropped(piece, state.targetSlots[1].id)
        assertNull(vm.uiState.value.targetSlots[1].placedPiece)
        assertEquals(1, vm.uiState.value.piecesToPlace.count { it.isPlaced })
        assertEquals(0, vm.uiState.value.errorsCount)
    }

    @Test fun `unknown forged old phase and old session pieces are ignored`() = runTest(dispatcher) {
        vm.startNewGame(1)
        runCurrent()
        val old = vm.uiState.value
        vm.onItemDropped(PieceItem("unknown", "ME"), old.targetSlots.first().id)
        vm.onItemDropped(old.piecesToPlace.first().copy(text = "FORGED"), old.targetSlots.first().id)
        assertEquals(0, vm.uiState.value.errorsCount)
        assertTrue(vm.uiState.value.targetSlots.all { it.placedPiece == null })
        fillBoard()
        advanceUntilIdle()
        vm.onItemDropped(old.piecesToPlace.first(), vm.uiState.value.targetSlots.first().id)
        assertEquals(0, vm.uiState.value.errorsCount)
        vm.startNewGame(1)
        runCurrent()
        vm.onItemDropped(old.piecesToPlace.first { it.text == "ME" }, vm.uiState.value.targetSlots.first().id)
        assertNull(vm.uiState.value.targetSlots.first().placedPiece)
        assertNotEquals(old.targetSlots.first().id, vm.uiState.value.targetSlots.first().id)
    }

    @Test fun `occupied destination counts one error but invalid destinations do not`() = runTest(dispatcher) {
        words.word = words.word!!.copy(text = "MAMA", syllables = listOf("MA", "MA"))
        vm.startNewGame(1)
        runCurrent()
        val state = vm.uiState.value
        vm.onItemDropped(state.piecesToPlace[0], state.targetSlots[0].id)
        vm.onItemDropped(state.piecesToPlace[1], state.targetSlots[0].id)
        vm.onItemDropped(state.piecesToPlace[1], "stale_slot")
        assertEquals(1, vm.uiState.value.errorsCount)
        assertFalse(vm.uiState.value.piecesToPlace.first { it.id == state.piecesToPlace[1].id }.isPlaced)
    }

    @Test fun `dropping a piece places it and speaks its sound`() = runTest(dispatcher) {
        vm.startNewGame(1)
        runCurrent()
        val state = vm.uiState.value
        val piece = state.piecesToPlace.first { it.text == "ME" }
        vm.onItemDropped(piece, state.targetSlots.first().id)
        assertEquals(piece.id, vm.uiState.value.targetSlots.first().placedPiece?.id)
        assertEquals("ME", audio.pieces.last().first)
        assertTrue(audio.pieces.last().second)
    }

    @Test fun `partial success waits for speech and disables interaction`() = runTest(dispatcher) {
        audio.completion = CompletableDeferred()
        vm.startNewGame(1)
        runCurrent()
        fillBoard()
        runCurrent()
        val success = vm.uiState.value
        vm.onItemDropped(success.piecesToPlace.first(), success.targetSlots.first().id)
        vm.speakReference()
        assertEquals(success, vm.uiState.value)
        assertEquals(1, audio.words.size)
        advanceTimeBy(2500)
        runCurrent()
        assertEquals(GamePhase.SYLLABLES_SUCCESS, vm.uiState.value.currentPhase)
        audio.completion!!.complete(Unit)
        runCurrent()
        assertEquals(GamePhase.LETTERS, vm.uiState.value.currentPhase)
        assertEquals(listOf("SA", "MESA"), audio.sequences.last())
    }

    @Test fun `quick speech still keeps one second of confirmation`() = runTest(dispatcher) {
        vm.startNewGame(1)
        runCurrent()
        fillBoard()
        runCurrent()
        advanceTimeBy(999)
        runCurrent()
        assertEquals(GamePhase.SYLLABLES_SUCCESS, vm.uiState.value.currentPhase)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(GamePhase.LETTERS, vm.uiState.value.currentPhase)
    }

    @Test fun `restart cancels old transition and resets errors`() = runTest(dispatcher) {
        audio.completion = CompletableDeferred()
        vm.startNewGame(1)
        runCurrent()
        fillBoard()
        runCurrent()
        val session = vm.uiState.value.sessionId
        vm.startNewGame(1)
        runCurrent()
        audio.completion!!.complete(Unit)
        advanceUntilIdle()
        assertNotEquals(session, vm.uiState.value.sessionId)
        assertEquals(GamePhase.SYLLABLES, vm.uiState.value.currentPhase)
        assertEquals(0, vm.uiState.value.errorsCount)
        assertTrue(progress.logs.isEmpty())
    }

    @Test fun `leaving cancels pending transition`() = runTest(dispatcher) {
        vm.startNewGame(1)
        runCurrent()
        fillBoard()
        runCurrent()
        vm.leaveGame()
        advanceUntilIdle()
        assertNull(vm.uiState.value.word)
        assertEquals(GamePhase.LOADING, vm.uiState.value.currentPhase)
        assertTrue(progress.logs.isEmpty())
    }

    @Test fun `slower previous load cannot replace a new game`() = runTest(dispatcher) {
        words.loadDelay = 2000
        vm.startNewGame(1)
        runCurrent()
        words.loadDelay = 0
        words.word = words.word!!.copy(id = 2, text = "CASA", syllables = listOf("CA", "SA"))
        vm.startNewGame(2)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.word?.id)
        assertEquals(listOf("CA", "SA"), vm.uiState.value.targetSlots.map { it.expectedText })
    }

    @Test fun `invalid missing words and repository errors show recoverable error`() = runTest(dispatcher) {
        words.word = null
        vm.startNewGame(1)
        runCurrent()
        assertEquals(GamePhase.ERROR, vm.uiState.value.currentPhase)
        words.word = Word(1, "?!", "", "", listOf(""), "", null)
        vm.startNewGame(1)
        runCurrent()
        assertEquals(GamePhase.ERROR, vm.uiState.value.currentPhase)
        words.fail = true
        vm.startNewGame(1)
        runCurrent()
        assertEquals(GamePhase.ERROR, vm.uiState.value.currentPhase)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test fun `save failure retains success and does not retry insertion`() = runTest(dispatcher) {
        progress.fail = true
        vm.startNewGame(1)
        runCurrent()
        fillBoard()
        advanceUntilIdle()
        fillBoard()
        advanceUntilIdle()
        assertEquals(GamePhase.COMPLETED, vm.uiState.value.currentPhase)
        assertNotNull(vm.uiState.value.saveError)
        assertEquals(1, progress.attempts)
    }

    @Test fun `replay does not cancel completed progress save`() = runTest(dispatcher) {
        progress.saveDelay = 3000
        vm.startNewGame(1)
        runCurrent()
        fillBoard()
        advanceUntilIdle()
        fillBoard()
        runCurrent()
        vm.startNewGame(1)
        advanceUntilIdle()
        assertEquals(GamePhase.SYLLABLES, vm.uiState.value.currentPhase)
        assertEquals(1, progress.logs.size)
    }

    private fun fillBoard() {
        vm.uiState.value.targetSlots.forEach { slot ->
            val piece = vm.uiState.value.piecesToPlace.first { !it.isPlaced && it.text == slot.expectedText }
            vm.onItemDropped(piece, slot.id)
        }
    }
}

private class FakeAudio : GameAudio {
    val words = mutableListOf<String>()
    val pieces = mutableListOf<Pair<String, Boolean>>()
    val sequences = mutableListOf<List<String>>()
    var completion: CompletableDeferred<Unit>? = null
    override fun speakWord(text: String) { words += text }
    override fun speakPiece(text: String, isSyllable: Boolean) { pieces += text to isSyllable }
    override suspend fun speakSequenceAndAwait(parts: List<String>) { sequences += parts; completion?.await() }
    override fun stop() = Unit
}

private class FakeWords : WordRepository {
    var word: Word? = Word(1, "MESA", "M", "ME", listOf("ME", "SA"), "", null)
    var loadDelay = 0L
    var fail = false
    override suspend fun getWordById(id: Int): Word? {
        val result = word
        delay(loadDelay)
        if (fail) error("load failed")
        return result
    }
    override fun getAllWords(): Flow<List<Word>> = flowOf(listOfNotNull(word))
    override fun getWordsByLetter(letter: String) = getAllWords()
    override suspend fun insertWord(word: Word) = 1L
    override suspend fun deleteWord(word: Word) = Unit
    override suspend fun updateWord(word: Word) = Unit
}

private class FakeProgress : ProgressRepository {
    val logs = mutableListOf<Triple<Int, Int, Boolean>>()
    var attempts = 0
    var fail = false
    var saveDelay = 0L
    override suspend fun insertLog(wordId: Int, errorsCount: Int, isCompleted: Boolean) {
        attempts++
        delay(saveDelay)
        if (fail) error("save failed")
        logs += Triple(wordId, errorsCount, isCompleted)
    }
    override fun getLogsForWord(wordId: Int): Flow<List<ProgressLog>> = flowOf(emptyList())
    override fun getAllLogs(): Flow<List<ProgressLog>> = flowOf(emptyList())
    override suspend fun clearAllLogs() = Unit
}
