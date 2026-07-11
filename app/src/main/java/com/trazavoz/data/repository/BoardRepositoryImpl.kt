package com.trazavoz.data.repository

import com.trazavoz.data.local.db.BoardDao
import com.trazavoz.data.local.entities.BoardEntity
import com.trazavoz.data.local.entities.WordBoardCrossRef
import com.trazavoz.domain.model.Board
import com.trazavoz.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BoardRepositoryImpl @Inject constructor(
    private val boardDao: BoardDao
) : BoardRepository {

    override fun getAllBoards(): Flow<List<Board>> =
        boardDao.getAllBoardsFlow().map { list -> list.map { it.toDomain() } }

    override fun getBoardWithWords(boardId: Int): Flow<Board?> =
        boardDao.getBoardWithWordsFlow(boardId).map { item ->
            item?.let {
                Board(
                    id = it.board.id,
                    name = it.board.name,
                    words = it.words.map { wordEntity -> wordEntity.toDomain() }
                )
            }
        }

    override suspend fun insertBoard(board: Board): Long =
        boardDao.insertBoard(board.toEntity())

    override suspend fun deleteBoard(board: Board) =
        boardDao.deleteBoard(board.toEntity())

    override suspend fun addWordToBoard(boardId: Int, wordId: Int) =
        boardDao.insertCrossRef(WordBoardCrossRef(wordId = wordId, boardId = boardId))

    override suspend fun removeWordFromBoard(boardId: Int, wordId: Int) =
        boardDao.deleteCrossRef(boardId = boardId, wordId = wordId)
}

fun BoardEntity.toDomain() = Board(id = id, name = name)
fun Board.toEntity() = BoardEntity(id = id, name = name)
