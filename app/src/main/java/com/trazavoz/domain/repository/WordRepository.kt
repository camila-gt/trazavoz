package com.trazavoz.domain.repository

import com.trazavoz.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getAllWords(): Flow<List<Word>>
    fun getWordsByLetter(letter: String): Flow<List<Word>>
    suspend fun insertWord(word: Word): Long
    suspend fun deleteWord(word: Word)
    suspend fun updateWord(word: Word)
    suspend fun getWordById(id: Int): Word?
}
