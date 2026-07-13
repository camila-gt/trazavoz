package com.trazavoz.data.repository

import com.trazavoz.data.local.db.WordDao
import com.trazavoz.data.local.entities.WordEntity
import com.trazavoz.domain.model.Word
import com.trazavoz.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {

    override fun getAllWords(): Flow<List<Word>> =
        wordDao.getAllWordsFlow().map { list -> list.map { it.toDomain() } }

    override fun getWordsByLetter(letter: String): Flow<List<Word>> =
        wordDao.getWordsByLetterFlow(letter).map { list -> list.map { it.toDomain() } }

    override suspend fun insertWord(word: Word): Long =
        wordDao.insertWord(word.toEntity())

    override suspend fun deleteWord(word: Word) =
        wordDao.deleteWord(word.toEntity())

    override suspend fun updateWord(word: Word) =
        wordDao.updateWord(word.toEntity())

    override suspend fun getWordById(id: Int): Word? =
        wordDao.getWordById(id)?.toDomain()
}

fun WordEntity.toDomain() = Word(id, text, initialLetter, associatedSyllable, syllables, imageUrl, localImagePath)
fun Word.toEntity() = WordEntity(id, text, initialLetter, associatedSyllable, syllables, imageUrl, localImagePath)
