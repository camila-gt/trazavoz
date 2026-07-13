package com.trazavoz.data.local.db

import androidx.room.*
import com.trazavoz.data.local.entities.BoardEntity
import com.trazavoz.data.local.entities.BoardWithWords
import com.trazavoz.data.local.entities.WordBoardCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {
    @Query("SELECT * FROM boards ORDER BY name ASC")
    fun getAllBoardsFlow(): Flow<List<BoardEntity>>

    @Transaction
    @Query("SELECT * FROM boards WHERE id = :boardId LIMIT 1")
    fun getBoardWithWordsFlow(boardId: Int): Flow<BoardWithWords?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBoard(board: BoardEntity): Long

    @Delete
    suspend fun deleteBoard(board: BoardEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: WordBoardCrossRef)

    @Query("DELETE FROM word_board_cross_ref WHERE boardId = :boardId AND wordId = :wordId")
    suspend fun deleteCrossRef(boardId: Int, wordId: Int)
}
