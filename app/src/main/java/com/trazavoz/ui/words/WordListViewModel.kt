package com.trazavoz.ui.words

import androidx.lifecycle.ViewModel
import com.trazavoz.domain.model.Word
import com.trazavoz.domain.repository.BoardRepository
import com.trazavoz.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val boardRepository: BoardRepository
) : ViewModel() {

    fun getWords(filterType: String, filterValue: String): Flow<List<Word>> {
        return when (filterType) {
            "letter" -> wordRepository.getWordsByLetter(filterValue)
            "board" -> {
                val boardId = filterValue.toIntOrNull() ?: return emptyFlow()
                boardRepository.getBoardWithWords(boardId).map { it?.words ?: emptyList() }
            }
            else -> emptyFlow()
        }
    }
}
