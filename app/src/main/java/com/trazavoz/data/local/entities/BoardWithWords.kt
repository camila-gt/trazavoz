package com.trazavoz.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BoardWithWords(
    @Embedded val board: BoardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = WordBoardCrossRef::class,
            parentColumn = "boardId",
            childColumn = "wordId"
        )
    )
    val words: List<WordEntity>
)
