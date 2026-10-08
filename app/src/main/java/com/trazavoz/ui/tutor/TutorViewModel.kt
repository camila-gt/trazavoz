package com.trazavoz.ui.tutor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trazavoz.data.remote.CachingManager
import com.trazavoz.domain.model.Board
import com.trazavoz.domain.model.ProgressLog
import com.trazavoz.domain.model.Word
import com.trazavoz.domain.repository.BoardRepository
import com.trazavoz.domain.repository.ProgressRepository
import com.trazavoz.domain.repository.WordRepository
import com.trazavoz.domain.usecase.ClearProgressStatsUseCase
import com.trazavoz.domain.usecase.SaveWordUseCase
import com.trazavoz.domain.usecase.SearchArasaacUseCase
import com.trazavoz.domain.usecase.SearchResult
import com.trazavoz.domain.usecase.SplitSyllablesUseCase
import com.trazavoz.domain.usecase.UpdateTutorPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TutorViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val boardRepository: BoardRepository,
    private val progressRepository: ProgressRepository,
    private val searchArasaacUseCase: SearchArasaacUseCase,
    private val saveWordUseCase: SaveWordUseCase,
    private val splitSyllablesUseCase: SplitSyllablesUseCase,
    private val updateTutorPinUseCase: UpdateTutorPinUseCase,
    private val cachingManager: CachingManager,
    private val clearProgressStatsUseCase: ClearProgressStatsUseCase
) : ViewModel() {

    val allWords: StateFlow<List<Word>> = wordRepository.getAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBoards: StateFlow<List<Board>> = boardRepository.getAllBoards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<ProgressLog>> = progressRepository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    fun searchWord(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) return@launch
            _isSearching.value = true
            _hasSearched.value = true
            _searchResults.value = searchArasaacUseCase(query)
            _isSearching.value = false
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _hasSearched.value = false
    }

    fun getSuggestedSyllables(text: String): String {
        val list = splitSyllablesUseCase(text)
        return list.joinToString("-")
    }

    fun addWord(text: String, arasaacId: Int, syllablesInput: String, imageUrl: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            val cleanText = text.trim().uppercase()
            if (cleanText.isBlank() || syllablesInput.isBlank()) {
                onFinished(false)
                return@launch
            }

            val syllables = syllablesInput.split("-")
                .map { it.trim().uppercase() }
                .filter { it.isNotBlank() }

            val initialLetter = if (cleanText.isNotEmpty()) cleanText[0].toString() else "A"
            val associatedSyllable = syllables.firstOrNull() ?: ""

            val word = Word(
                id = 0,
                text = cleanText,
                initialLetter = initialLetter,
                associatedSyllable = associatedSyllable,
                syllables = syllables,
                imageUrl = imageUrl,
                localImagePath = null
            )

            val success = saveWordUseCase(word, arasaacId)
            onFinished(success)
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            word.localImagePath?.let {
                cachingManager.deleteCachedImage(it)
            }
            wordRepository.deleteWord(word)
        }
    }

    fun createBoard(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            boardRepository.insertBoard(Board(id = 0, name = name))
        }
    }

    fun deleteBoard(board: Board) {
        viewModelScope.launch {
            boardRepository.deleteBoard(board)
        }
    }

    fun toggleWordInBoard(boardId: Int, wordId: Int, belongs: Boolean) {
        viewModelScope.launch {
            if (belongs) {
                boardRepository.addWordToBoard(boardId, wordId)
            } else {
                boardRepository.removeWordFromBoard(boardId, wordId)
            }
        }
    }

    fun getBoardWithWords(boardId: Int): Flow<Board?> {
        return boardRepository.getBoardWithWords(boardId)
    }

    fun updatePin(newPin: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (newPin.length == 4 && newPin.all { it.isDigit() }) {
                updateTutorPinUseCase(newPin)
                onFinished(true)
            } else {
                onFinished(false)
            }
        }
    }

    fun clearStats() {
        viewModelScope.launch {
            clearProgressStatsUseCase()
        }
    }

    fun updateWord(word: Word, newText: String, newSyllablesInput: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            val cleanText = newText.trim().uppercase()
            if (cleanText.isBlank() || newSyllablesInput.isBlank()) {
                onFinished(false)
                return@launch
            }

            val syllables = newSyllablesInput.split("-")
                .map { it.trim().uppercase() }
                .filter { it.isNotBlank() }

            val initialLetter = if (cleanText.isNotEmpty()) cleanText[0].toString() else word.initialLetter
            val associatedSyllable = syllables.firstOrNull() ?: ""

            val updatedWord = word.copy(
                text = cleanText,
                initialLetter = initialLetter,
                associatedSyllable = associatedSyllable,
                syllables = syllables
            )

            wordRepository.updateWord(updatedWord)
            onFinished(true)
        }
    }
}
