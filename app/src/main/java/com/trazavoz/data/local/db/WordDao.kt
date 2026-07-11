package com.trazavoz.data.local.db

import androidx.room.*
import com.trazavoz.data.local.entities.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY text ASC")
    fun getAllWordsFlow(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE initialLetter = :letter ORDER BY text ASC")
    fun getWordsByLetterFlow(letter: String): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWord(word: WordEntity): Long

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Int): WordEntity?
}
