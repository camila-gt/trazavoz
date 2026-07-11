package com.trazavoz.domain.repository

import com.trazavoz.domain.model.Board
import kotlinx.coroutines.flow.Flow

interface BoardRepository {
    fun getAllBoards(): Flow<List<Board>>
    fun getBoardWithWords(boardId: Int): Flow<Board?>
    suspend fun insertBoard(board: Board): Long
    suspend fun deleteBoard(board: Board)
    suspend fun addWordToBoard(boardId: Int, wordId: Int)
    suspend fun removeWordFromBoard(boardId: Int, wordId: Int)
}
